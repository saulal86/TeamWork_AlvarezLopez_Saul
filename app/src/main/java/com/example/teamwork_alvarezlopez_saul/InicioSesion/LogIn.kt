package com.example.teamwork_alvarezlopez_saul.InicioSesion

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.teamwork_alvarezlopez_saul.Notas.Notes
import com.example.teamwork_alvarezlopez_saul.Cerrar_Sesion.ProviderType
import com.example.teamwork_alvarezlopez_saul.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LogIn : AppCompatActivity() {

    // Declaración de variables
    private lateinit var loginButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var contraseñaEditText: EditText
    private lateinit var googleButton: ImageButton
    private lateinit var loginLayout: ConstraintLayout
    private lateinit var textoregistrarse: TextView
    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Inicialización de las variables
        loginButton = findViewById(R.id.logInButton)
        emailEditText = findViewById(R.id.emailEditText)
        contraseñaEditText = findViewById(R.id.contraseñaEditText)
        googleButton = findViewById(R.id.googlebutton)
        loginLayout = findViewById(R.id.LogInLayout)
        textoregistrarse = findViewById(R.id.textoregistrarse)

        // Configuración
        setup()
    }


    private fun setup(){
        title = "Autenticación"

        // Listener para el botón de registro
        loginButton.setOnClickListener{
            if (emailEditText.text.isNotEmpty() && contraseñaEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.text.toString(),
                    contraseñaEditText.text.toString()).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val user = task.result?.user
                        if (user != null && user.isEmailVerified) {
                            showHome(user.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert("Error", "Debes verificar tu correo electrónico antes de iniciar sesión")
                        }
                    } else {
                        showAlert("Error", "Se ha producido un error autenticando al usuario")
                    }
                }
            } else {
                showAlert("Error", "Hay algún campo vacío")
            }
        }

        googleButton.setOnClickListener{
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getGoogleClientId(this)).requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        textoregistrarse.setOnClickListener{
            Log.d("LogInActivity", "textoregistrarse clicked")
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }

//        textoiniciarsesionlogin.setOnClickListener{
//            val intent = Intent(this, SignUp::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun showEmailVerificationSentAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verificación de correo electrónico")
        builder.setMessage("Se ha enviado un correo de verificación. Por favor, verifica tu correo " +
                "antes de iniciar sesión. Cuando haya verificado su correo electrónico inicie sesión.")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome (email: String, provider: ProviderType){
        val homeIntent = Intent(this, Notes::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
        finish()
    }

        private fun getGoogleClientId(context: Context): String {
        try {
            val resources = context.resources
            val packageName = context.packageName
            val resourceId = resources.getIdentifier("default_web_client_id", "string", packageName)
            if (resourceId != 0) {
                return resources.getString(resourceId)
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {

                            if (it.isSuccessful) {
                                showHome(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showAlert("Error", "Ha ocurrido un error al iniciar sesión.")
                            }
                        }
                }
            }catch (e: ApiException){
                e.printStackTrace()
            }
        }
    }
}
