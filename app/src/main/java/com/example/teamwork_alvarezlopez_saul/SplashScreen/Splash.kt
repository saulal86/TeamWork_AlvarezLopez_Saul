package com.example.teamwork_alvarezlopez_saul.SplashScreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teamwork_alvarezlopez_saul.Cerrar_Sesion.HomeActivity
import com.example.teamwork_alvarezlopez_saul.InicioSesion.AuthActivity
import com.example.teamwork_alvarezlopez_saul.Notas.EditorTextos
import com.example.teamwork_alvarezlopez_saul.R
import com.google.firebase.auth.FirebaseAuth

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // No se necesita setContentView ya que la pantalla splash se maneja por tema

        screenSplash.setKeepOnScreenCondition { true }
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}