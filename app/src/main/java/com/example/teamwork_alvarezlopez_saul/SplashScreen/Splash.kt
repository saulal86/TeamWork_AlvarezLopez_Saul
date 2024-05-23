package com.example.teamwork_alvarezlopez_saul.SplashScreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.teamwork_alvarezlopez_saul.InicioSesion.SignUp

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // No se necesita setContentView ya que la pantalla splash se maneja por tema

        screenSplash.setKeepOnScreenCondition { true }
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
        finish()
    }
}