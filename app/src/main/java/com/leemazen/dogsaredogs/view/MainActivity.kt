package com.leemazen.dogsaredogs.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.leemazen.dogsaredogs.R
import com.leemazen.dogsaredogs.util.PERMISSION_SEND_SMS
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var navController:NavController
    var isPlayingAsset = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this,R.id.fragment)
        NavigationUI.setupActionBarWithNavController(this,navController)

        /// init audio engine
        createEngine()


        var sampleRate = 0
        var bufSize = 0
        /*
         * retrieve fast audio path sample rate and buf size; if we have it, we pass to native
         * side to create a player with fast audio enabled [ fast audio == low latency audio ];
         * IF we do not have a fast audio path, we pass 0 for sampleRate, which will force native
         * side to pick up the 8Khz sample rate.
         */
        /*
         * retrieve fast audio path sample rate and buf size; if we have it, we pass to native
         * side to create a player with fast audio enabled [ fast audio == low latency audio ];
         * IF we do not have a fast audio path, we pass 0 for sampleRate, which will force native
         * side to pick up the 8Khz sample rate.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val myAudioMgr =
                getSystemService(Context.AUDIO_SERVICE) as AudioManager
            var nativeParam =
                myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            sampleRate = nativeParam.toInt()
            nativeParam = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
            bufSize = nativeParam.toInt()
        }
        createBufferQueueAudioPlayer(sampleRate, bufSize)

        var created = false


        if (!created) {
            created = createAssetAudioPlayer(assets,
                "effect.mp3"
            )
        }
        if (created) {
            isPlayingAsset = !isPlayingAsset
            setPlayingAssetAudioPlayer(isPlayingAsset)
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController,null)
    }

    fun checkSMSPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS)){
                AlertDialog.Builder(this)
                    .setTitle("Send SMS Permission")
                    .setMessage("This app requires to access SMS permission")
                    .setPositiveButton("Ask me"){dialog,which ->
                        requestPermission()
                    }
                    .setNegativeButton("No"){dialog,which ->
                        notifyDetailFragment(false)
                    }
                    .show()
            }
            else{
                requestPermission()
            }
        }
        else{
            notifyDetailFragment(true)
        }
    }
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.SEND_SMS),
            PERMISSION_SEND_SMS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_SEND_SMS ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    notifyDetailFragment(true)
                }
                else{
                    notifyDetailFragment(false)
                }
            }
        }
    }

    private fun notifyDetailFragment(permissionGranted: Boolean){
        val activeFragment = fragment.childFragmentManager.primaryNavigationFragment
        if(activeFragment is DetailFragment){
            (activeFragment as DetailFragment).onPermissionResolved(permissionGranted)
        }
    }

    override fun onDestroy() {
        shutdown()
        super.onDestroy()
    }

    /** Native methods, implemented in jni folder  */
    external fun createEngine()
    external fun createBufferQueueAudioPlayer(sampleRate: Int, samplesPerBuf: Int)
    external fun createAssetAudioPlayer(
        assetManager: AssetManager?,
        filename: String?
    ): Boolean

    // true == PLAYING, false == PAUSED
    external fun setPlayingAssetAudioPlayer(isPlaying: Boolean)
    external fun createUriAudioPlayer(uri: String?): Boolean
    external fun setPlayingUriAudioPlayer(isPlaying: Boolean)
    external fun setLoopingUriAudioPlayer(isLooping: Boolean)
    external fun setChannelMuteUriAudioPlayer(chan: Int, mute: Boolean)
    external fun setChannelSoloUriAudioPlayer(chan: Int, solo: Boolean)
    external fun getNumChannelsUriAudioPlayer(): Int
    external fun setVolumeUriAudioPlayer(millibel: Int)
    external fun setMuteUriAudioPlayer(mute: Boolean)
    external fun enableStereoPositionUriAudioPlayer(enable: Boolean)
    external fun setStereoPositionUriAudioPlayer(permille: Int)
    external fun selectClip(which: Int, count: Int): Boolean
    external fun enableReverb(enabled: Boolean): Boolean
    external fun createAudioRecorder(): Boolean
    external fun startRecording()
    external fun shutdown()

    /** Load jni .so on initialization */
    companion object {
        init {
            System.loadLibrary("audio-jni")
        }
    }


}