package com.leemazen.dogsaredogs.viewmodel

import androidx.lifecycle.MutableLiveData
import android.app.Application
import com.leemazen.dogsaredogs.model.DogBreed
import com.leemazen.dogsaredogs.model.DogDatabase
import kotlinx.coroutines.launch

class DetailViewModel(application: Application):BaseViewModel(application) {
    val dog = MutableLiveData<DogBreed>()

    fun getDogDetail(dogUuid: Int) {
        //val dogDetails = DogBreed("1","Corgi","15 years","breedGroup","breedFor","temperament","")
        launch {
            dog.value = DogDatabase(getApplication()).dogDao().getDog(dogUuid)
        }

    }
}