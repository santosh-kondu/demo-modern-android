package com.leemazen.dogsaredogs.view

import android.content.Intent
import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.telephony.SmsManager
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.leemazen.dogsaredogs.R
import com.leemazen.dogsaredogs.databinding.FragmentDetailBinding
import com.leemazen.dogsaredogs.databinding.SendSmsDialogBinding
import com.leemazen.dogsaredogs.model.DogBreed
import com.leemazen.dogsaredogs.model.DogPalette
import com.leemazen.dogsaredogs.model.SmsInfo
import com.leemazen.dogsaredogs.viewmodel.DetailViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {
    private lateinit var viewModel:DetailViewModel
    private var dogUuid = 0

    private lateinit var dataBinding: FragmentDetailBinding

    private var sendSMSStarted = false
    private var currentDog: DogBreed? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        dataBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_detail,container,false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)


        arguments?.let {
            dogUuid = DetailFragmentArgs.fromBundle(it).dogUuid
        }

        viewModel.getDogDetail(dogUuid)


        observeViewModel()
    }

    private fun observeViewModel() {

        viewModel.dog.observe(this, Observer { dog ->
            currentDog = dog
            dog?.let {
                dataBinding.dogDetails = dog

                it.imageUrl?.let {
                    setupBackgroundColor(it)
                }
            }
        })
    }

    private fun setupBackgroundColor(url: String){
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>(){
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource)
                        .generate{ palette ->
                            val intcolor = palette?.vibrantSwatch?.rgb ?:0
                            val myPalette = DogPalette(intcolor)
                            dataBinding.palatte = myPalette
                        }
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.details_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_send_sms -> {
                sendSMSStarted = true
                (activity as MainActivity).checkSMSPermission()
            }

            R.id.action_share ->{
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT,"Checkout this dog breed")
                intent.putExtra(Intent.EXTRA_TEXT,"${currentDog?.dogBreed} red for ${currentDog?.bredFor}")
                intent.putExtra(Intent.EXTRA_STREAM,currentDog?.imageUrl)
                startActivity(Intent.createChooser(intent,"Share with"))
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun onPermissionResolved(permissionGranted: Boolean){
        if(sendSMSStarted && permissionGranted){
            context?.let { 
                val smsInfo = SmsInfo("","${currentDog?.dogBreed} is for ${currentDog?.bredFor}"
                    ,currentDog?.imageUrl)
                
                val dialogBuilding = DataBindingUtil.inflate<SendSmsDialogBinding>(
                    LayoutInflater.from(it),
                    R.layout.send_sms_dialog,
                    null,
                    false
                )
                
                AlertDialog.Builder(it)
                    .setView(dialogBuilding.root)
                    .setPositiveButton("Send SMS"){dialog, which ->
                        if (!dialogBuilding.smsDestination.text.isNullOrEmpty()){
                            smsInfo.to = dialogBuilding.smsDestination.toString()
                            sendSms(smsInfo)
                        }
                    }
                    .setNegativeButton("Cancel"){dialog,which->}
                    .show()

                dialogBuilding.smsInfo = smsInfo
            }


        }
    }

    private fun sendSms(smsInfo: SmsInfo) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context,0,intent,0)
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(smsInfo.to,null,smsInfo.text,pi,null)
    }
}