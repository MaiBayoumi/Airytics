package com.example.airytics

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.postOnAnimationDelayed
import com.example.airytics.databinding.ActivityMainBinding
import com.example.airytics.hostedactivity.view.HostedActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieAnimation.postOnAnimationDelayed(4000){
            val intent = Intent(this, HostedActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

}