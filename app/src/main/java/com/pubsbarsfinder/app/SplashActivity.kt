package com.pubsbarsfinder.app

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.pubsbarsfinder.app.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = android.os.Handler(Looper.getMainLooper())
    private val openMainScreen = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler.postDelayed(openMainScreen, SPLASH_DELAY_MS)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(openMainScreen)
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1500L
    }
}