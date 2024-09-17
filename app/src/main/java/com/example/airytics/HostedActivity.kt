package com.example.airytics

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.example.airytics.databinding.ActivityHostedBinding

class HostedActivity : AppCompatActivity() {

    lateinit var binding: ActivityHostedBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHostedBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}