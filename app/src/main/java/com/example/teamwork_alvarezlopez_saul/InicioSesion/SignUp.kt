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
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.Constantes
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.PreferenceManager
import com.example.teamwork_alvarezlopez_saul.Home.Home
import com.example.teamwork_alvarezlopez_saul.Notas.Notes
import com.example.teamwork_alvarezlopez_saul.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class SignUp : AppCompatActivity() {
    private lateinit var signUpButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var contraseñaEditText: EditText
    private lateinit var confirmarcontraseñaEditText: EditText
    private lateinit var googleButton: ImageButton
    private lateinit var SignUpLayout: ConstraintLayout
    private lateinit var textoiniciarsesion: TextView
    private val GOOGLE_SIGN_IN = 100

    private lateinit var preferenceManager: PreferenceManager

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
        preferenceManager =
            PreferenceManager(
                applicationContext
            )

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

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = contraseñaEditText.text.toString()
            val confirmPassword = confirmarcontraseñaEditText.text.toString()

            if (validarEmail(email) && validarContraseña(password)) {
                if (password == confirmPassword) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                val userId = user?.uid ?: ""
                                preferenceManager.putString(Constantes.KEY_USERS_ID, userId)
                                preferenceManager.putString(Constantes.KEY_EMAIL, email)
                                showHomeOrVerifyEmail(email, userId, ProviderType.BASIC)
                                getToken()
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
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getGoogleClientId(this)).requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        textoiniciarsesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            clearEditTexts()
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validarEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|es)$")
        val isValid = emailPattern.matches(email)
        Log.d("SignUp", "Email validation for $email: $isValid")
        return isValid
    }

    private fun validarContraseña(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?]).{8,}$")
        val isValid = passwordPattern.matches(password)
        Log.d("SignUp", "Password validation for $password: $isValid")
        return isValid
    }

    private fun clearEditTexts() {
        emailEditText.text.clear()
        contraseñaEditText.text.clear()
        confirmarcontraseñaEditText.text.clear()
    }

    private fun showAlert(title: String, message: String) {
        if (!isFinishing && !isDestroyed) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            Log.w("SignUpActivity", "La actividad no está en un estado válido para mostrar un diálogo.")
        }
    }

    private fun showHome(email: String, userId: String, provider: ProviderType) {
        val homeIntent = Intent(this, Home::class.java).apply {
            putExtra("email", email)
            putExtra("userId", userId)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
        finish()
    }

    private fun showHomeOrVerifyEmail(email: String, userId: String, provider: ProviderType) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showAlert("Verificación", "Se ha enviado un correo de verificación a tu email.")
            } else {
                showAlert("Error", "No se pudo enviar el correo de verificación.")
            }
        }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("SignUp", "FCM token: $token")
                    updateToken(token)
                } else {
                    Log.w("SignUp", "Fetching FCM registration token failed", task.exception)
                }
            }
    }

    private fun updateToken(token: String) {
        val userId = preferenceManager.getString(Constantes.KEY_USERS_ID)
        Log.d("SignUp", "Updating token for user ID: $userId")

        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constantes.KEY_COLLECTION_USERS).document(userId)

        documentReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // El documento existe, actualiza el token
                    documentReference.update(Constantes.KEY_FCM_TOKEN, token)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Token actualizado correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            showAlert("Error", "Token no actualizado: ${exception.localizedMessage}")
                        }
                } else {
                    // El documento no existe, créalo y luego actualiza el token
                    val userData = mapOf(
                        Constantes.KEY_USERS_ID to userId,
                        Constantes.KEY_EMAIL to preferenceManager.getString(Constantes.KEY_EMAIL),
                        Constantes.KEY_FCM_TOKEN to token
                    )
                    documentReference.set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Token actualizado correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            showAlert("Error", "Token no actualizado: ${exception.localizedMessage}")
                        }
                }
            } else {
                showAlert("Error", "Error al obtener el documento del usuario: ${task.exception?.localizedMessage}")
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
                                val userId = user?.uid ?: ""
                                preferenceManager.putString(Constantes.KEY_EMAIL, email)
                                showHome(email, userId, ProviderType.GOOGLE)
                                getToken()
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
}
