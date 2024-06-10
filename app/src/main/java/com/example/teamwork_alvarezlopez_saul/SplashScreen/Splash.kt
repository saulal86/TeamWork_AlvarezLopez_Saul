package com.example.teamwork_alvarezlopez_saul.SplashScreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.teamwork_alvarezlopez_saul.InicioSesion.LogIn

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)

        screenSplash.setKeepOnScreenCondition { true }
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
        finish()
    }
}