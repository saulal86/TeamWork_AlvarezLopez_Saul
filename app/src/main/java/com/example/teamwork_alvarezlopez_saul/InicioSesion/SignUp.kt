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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.teamwork_alvarezlopez_saul.CerrarSesion.ProviderType
import com.example.teamwork_alvarezlopez_saul.Notas.Notes
import com.example.teamwork_alvarezlopez_saul.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

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
            SignUpLayout.visibility = View.INVISIBLE
            showHome(currentUser.email ?: "", currentUser.uid, ProviderType.BASIC)
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
                                saveUserEmailToFirestore(email)
                                showHomeOrVerifyEmail(email, user?.uid ?: "", ProviderType.BASIC)
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
                                val email = account.email ?: ""
                                saveUserEmailToFirestore(email)
                                showHome(email, user?.uid ?: "", ProviderType.GOOGLE)
                            } else {
                                val error = it.exception?.message ?: "Error desconocido"
                                showAlert("Error", "Ha ocurrido un error al iniciar sesión: $error")
                            }
                        }
                }
            } catch (e: ApiException) {
                e.printStackTrace()
                showAlert("Error", "Error de autenticación de Google: ${e.localizedMessage}")
            }
        }
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

    private fun saveUserEmailToFirestore(email: String) {
        val database = FirebaseFirestore.getInstance()
        val data: HashMap<String, String> = HashMap()
        data["correo"] = email
        database.collection("usuarios")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "La información del usuario ha sido insertada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showHomeOrVerifyEmail(email: String, userId: String, provider: ProviderType) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (provider == ProviderType.GOOGLE || (currentUser != null && currentUser.isEmailVerified)) {
            // El usuario ha verificado su correo electrónico o está usando Google como proveedor, así que procedemos a la pantalla principal
            showHome(email, userId, provider)
        } else {
            // El usuario no ha verificado su correo electrónico, mostrar un mensaje o llevarlo a una pantalla de verificación de correo electrónico
            currentUser?.sendEmailVerification()
            showEmailNotVerifiedAlert(email, userId, provider)
        }
    }

    private fun showEmailNotVerifiedAlert(email: String, userId: String, provider: ProviderType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Correo electrónico no verificado")
        builder.setMessage("Recuerda verificar tu correo electrónico para mayor seguridad.")
        builder.setPositiveButton("Aceptar") { _, _ ->
            // Llama a showHome al presionar Aceptar
            showHome(email, userId, provider)
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, userId: String, provider: ProviderType) {
        val homeIntent = Intent(this, Notes::class.java).apply {
            putExtra("email", email)
            putExtra("userId", userId)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
        finish()
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
