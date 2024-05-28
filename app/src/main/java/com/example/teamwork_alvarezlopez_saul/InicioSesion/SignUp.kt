package com.example.teamwork_alvarezlopez_saul.InicioSesion

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.teamwork_alvarezlopez_saul.Cerrar_Sesion.ProviderType
import com.example.teamwork_alvarezlopez_saul.Notas.Notes
import com.example.teamwork_alvarezlopez_saul.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignUp : AppCompatActivity() {
    private lateinit var signUpButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var contraseñaEditText: EditText
    private lateinit var confirmarcontraseñaEditText: EditText
    private lateinit var googleButton: ImageButton
    private lateinit var SignUpLayout: ConstraintLayout
    private lateinit var textoiniciarsesion: TextView
    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Inicialización de las variables
        signUpButton = findViewById(R.id.signUpbutton)
        emailEditText = findViewById(R.id.emailEditText)
        contraseñaEditText = findViewById(R.id.contraseñaEditText)
        SignUpLayout = findViewById(R.id.SignUpLayout)
        confirmarcontraseñaEditText = findViewById(R.id.confirmarcontraseñaEditText)
        textoiniciarsesion = findViewById(R.id.textoiniciarsesion)
        googleButton = findViewById(R.id.googlebutton)

        // Configuración
        setup()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                SignUpLayout.visibility = View.INVISIBLE
                showHome(currentUser.email ?: "", ProviderType.BASIC)
            } else {
                val intent = Intent(this, LogIn::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            SignUpLayout.visibility = View.VISIBLE
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun setup() {
        title = "Autenticación"

        // Listener para el botón de registro
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = contraseñaEditText.text.toString()
            val confirmPassword = confirmarcontraseñaEditText.text.toString()

            if (validateEmail(email) && validatePassword(password)) {
                if (password == confirmPassword) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                showHomeOrVerifyEmail(email, ProviderType.GOOGLE)
                            } else {
                                showAlert("Error", "Se ha producido un error autenticando al usuario")
                            }
                        }
                } else {
                    showAlert("Error", "Las contraseñas no coinciden")
                }
            } else {
                showAlert("Error", "Email o contraseña no válidos")
            }
        }

        googleButton.setOnClickListener {
            // Configuración de Google Sign-In
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getGoogleClientId(this)).requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        textoiniciarsesion.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|es)$")
        val isValid = emailPattern.matches(email)
        Log.d("SignUp", "Email validation for $email: $isValid")
        return isValid
    }

    private fun validatePassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?]).{8,}$")
        val isValid = passwordPattern.matches(password)
        Log.d("SignUp", "Password validation for $password: $isValid")
        return isValid
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHomeOrVerifyEmail(email: String, provider: ProviderType) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            // El usuario ha verificado su correo electrónico, así que procedemos a la pantalla principal
            showHome(email, provider)
        } else {
            // El usuario no ha verificado su correo electrónico, mostrar un mensaje o llevarlo a una pantalla de verificación de correo electrónico
            currentUser?.sendEmailVerification()
            showEmailNotVerifiedAlert()
        }
    }

    private fun showEmailNotVerifiedAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Correo electrónico no verificado")
        builder.setMessage("Recuerda verificar tu correo electrónico para mayor seguridad.")
        builder.setPositiveButton("Aceptar") { _, _ ->
            // Llama a showHome al presionar Aceptar
            showHome(emailEditText.text.toString(), ProviderType.GOOGLE)
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType) {
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
                                val user = FirebaseAuth.getInstance().currentUser
                                user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                                    if (verificationTask.isSuccessful) {
                                        showEmailNotVerifiedAlert()
                                    } else {
                                        showAlert("Error", "Error al enviar el correo de verificación")
                                    }
                                }
                            } else {
                                showAlert("Error", "Ha ocurrido un error al iniciar sesión.")
                            }
                        }
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }
}
