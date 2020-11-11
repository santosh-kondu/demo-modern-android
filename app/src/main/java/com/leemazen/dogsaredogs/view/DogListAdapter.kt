package com.leemazen.dogsaredogs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.leemazen.dogsaredogs.R
import com.leemazen.dogsaredogs.databinding.ItemDogBinding
import com.leemazen.dogsaredogs.model.DogBreed
import com.leemazen.dogsaredogs.util.getProgressDrawable
import com.leemazen.dogsaredogs.util.loadImage
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_dog.view.*

class DogListAdapter(val dogList: ArrayList<DogBreed>) : RecyclerView.Adapter<DogListAdapter.DogViewHolder>(),DogClickListener{

    fun updateDogList(newDogList: List<DogBreed>){
        dogList.clear()
        dogList.addAll(newDogList)
        notifyDataSetChanged()
    }

    class  DogViewHolder(var view: ItemDogBinding) : RecyclerView.ViewHolder(view.root)

    override fun getItemCount() = dogList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        //val view = inflater.inflate(R.layout.item_dog,parent,false)
        val view = DataBindingUtil.inflate<ItemDogBinding>(inflater, R.layout.item_dog,parent,false)
        return  DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.view.dog = dogList[position]
        holder.view.listener = this
//        holder.view.name.text = dogList[position].dogBreed
//        holder.view.lifespan.text = dogList[position].lifeSpan
//        holder.view.setOnClickListener{
//            val action = ListFragmentDirections.actionDetailFragment()
//            action.dogUuid = dogList[position].uuid
//            Navigation.findNavController(it).navigate(action)
//        }
//        holder.view.imageView.loadImage(dogList[position].imageUrl, getProgressDrawable(holder.view.imageView.context))
    }

    override fun onDogClicked(v: View) {
            val action = ListFragmentDirections.actionDetailFragment()
            action.dogUuid = v.dogId.text.toString().toInt()
            Navigation.findNavController(v).navigate(action)
    }
}
