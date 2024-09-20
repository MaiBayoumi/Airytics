package com.example.airytics

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.postOnAnimationDelayed
import com.example.airytics.databinding.ActivityMainBinding
import com.example.airytics.hostedactivity.view.HostedActivity
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.example.airytics.utilities.Functions

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onStart() {
        super.onStart()
        forceLightMode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setDefaultLanguage()

        binding.lottieAnimation.postOnAnimationDelayed(4000){
            val intent = Intent(this, HostedActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun setDefaultLanguage() {
        if (SettingSharedPref.getInstance(this).readStringFromSettingSP(Constants.LANGUAGE) == Constants.ARABIC) {
            Functions.changeLanguage(this, "ar")
        } else {
            Functions.changeLanguage(this, "en")
        }
    }

    private fun forceLightMode(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
