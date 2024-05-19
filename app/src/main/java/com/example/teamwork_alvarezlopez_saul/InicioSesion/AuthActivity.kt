package com.example.teamwork_alvarezlopez_saul.InicioSesion

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.example.teamwork_alvarezlopez_saul.Notas.EditorTextos
import com.example.teamwork_alvarezlopez_saul.Cerrar_Sesion.ProviderType
import com.example.teamwork_alvarezlopez_saul.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    // Declaración de variables
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var contraseñaEditText: EditText
    private lateinit var authLayout: LinearLayout
    private lateinit var googleButton: Button
    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Inicialización de las variables
        signUpButton = findViewById(R.id.signUpbutton)
        loginButton = findViewById(R.id.logInButton)
        emailEditText = findViewById(R.id.emailEditText)
        contraseñaEditText = findViewById(R.id.contraseñaEditText)
        authLayout = findViewById(R.id.authLayout)
        googleButton = findViewById(R.id.googlebutton)

        // Configuración
        setup()

        // Comprobar si hay una sesión activa
        session()
    }

    override fun onStart() {
        super.onStart()
        authLayout.visibility = View.VISIBLE
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup(){
        title = "Autenticación"

        // Listener para el botón de registro
        signUpButton.setOnClickListener{
            // Verifica si los campos de correo electrónico y contraseña no están vacíos
            if (emailEditText.text.isNotEmpty() && contraseñaEditText.text.isNotEmpty()) {
                // Intenta crear un usuario con correo electrónico y contraseña
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.text.toString(),
                    contraseñaEditText.text.toString()).addOnCompleteListener {
                    // Verifica si la operación de creación de usuario fue exitosa
                    if(it.isSuccessful){
                        // Muestra la actividad principal con el correo electrónico del usuario
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    }else{
                        // Muestra una alerta en caso de error
                        showAlert("Error", "Ha ocurrido un error al iniciar sesión.")
                    }
                }
            }
        }

        loginButton.setOnClickListener{
            if (emailEditText.text.isNotEmpty() && contraseñaEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.text.toString(),
                    contraseñaEditText.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful){
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    }else{
                        showAlert("Error", "Ha ocurrido un error al iniciar sesión.")
                    }
                }
            }
        }

        googleButton.setOnClickListener{
            // Configuración de Google Sign-In
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getGoogleClientId(this)).requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)


            // Continúa con el código para iniciar sesión con Google
        }
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome (email: String, provider: ProviderType){
        val homeIntent = Intent(this, EditorTextos::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    // Método para obtener el default_web_client_id desde google-services.json
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
