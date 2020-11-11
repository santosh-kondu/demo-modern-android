package com.leemazen.dogsaredogs.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.leemazen.dogsaredogs.model.DogBreed
import com.leemazen.dogsaredogs.model.DogDatabase
import com.leemazen.dogsaredogs.model.DogsApiService
import com.leemazen.dogsaredogs.util.NotificationsHelper
import com.leemazen.dogsaredogs.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

class ListViewModel(application: Application): BaseViewModel(application) {
    private var prefHelper = SharedPreferencesHelper(getApplication())
    private var refreshTime = 5 * 60 * 1000 * 1000 * 1000L

    private val dogService = DogsApiService()
    private val disposable = CompositeDisposable()

    val dogs = MutableLiveData<List<DogBreed>>()
    val dogLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh(){
        checkCacheDuration()
        val updateTime = prefHelper.getUpdateTime()
        if(updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refreshTime)
        {
            fetchFromDatabase()
        }
        else {
            fetchFromRemote()
            Toast.makeText(getApplication(),"Dogs retried from end point",Toast.LENGTH_LONG).show()
        }
    }

    private fun checkCacheDuration() {
        val cachedPreference = prefHelper.getCacheDuration()

        try {
            val cachePreferenceInt = cachedPreference?.toInt() ?: 5 *60
            refreshTime = cachePreferenceInt.times(1000*1000*1000L)
        }catch (e:NumberFormatException){
            e.printStackTrace()
        }
    }

    fun refreshBypassCache(){
        fetchFromRemote()
        Toast.makeText(getApplication(),"Dogs retried from end point",Toast.LENGTH_LONG).show()
    }

    private fun fetchFromDatabase() {
        loading.value = true
        launch {
            val dogs = DogDatabase(getApplication()).dogDao().getALLDogs()
            dogRetrieved(dogs)
            Toast.makeText(getApplication(),"Dogs retried from database",Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchFromRemote(){
        loading.value = true
        disposable.add(
            dogService.getDogs()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<DogBreed>>(){
                    override fun onSuccess(dogList: List<DogBreed>) {
                        storeDogsLocally(dogList)
                        NotificationsHelper(getApplication()).createNotification()
                    }

                    override fun onError(e: Throwable) {
                        dogLoadError.value = true
                        loading.value = false
                        e.printStackTrace()
                    }
                })
        )
    }

    private fun  dogRetrieved(dogList: List<DogBreed>){
        dogs.value = dogList
        dogLoadError.value =false
        loading.value=false
    }

    private fun storeDogsLocally(list: List<DogBreed>){
        launch {
            val dao = DogDatabase(getApplication()).dogDao()
                dao.deleteAllDogs()
            val result = dao.insertAll(*list.toTypedArray())
            var i =0
            while (i < list.size){
                list[i].uuid = result[i].toInt()
                i++
            }
            dogRetrieved(list)
        }
        prefHelper.saveUpdateTime(System.nanoTime())
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}