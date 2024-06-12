package com.example.teamwork_alvarezlopez_saul.InicioSesion

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.Constantes
import com.example.teamwork_alvarezlopez_saul.Chat.utilities.PreferenceManager
import com.example.teamwork_alvarezlopez_saul.Home.Home
import com.example.teamwork_alvarezlopez_saul.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LogIn : AppCompatActivity() {

    // Declaración de variables
    private lateinit var botonlogin: Button
    private lateinit var emailEditText: EditText
    private lateinit var contraseñaEditText: EditText
    private lateinit var botongoogle: ImageButton
    private lateinit var botonojo: ImageButton
    private lateinit var loginLayout: ConstraintLayout
    private lateinit var textoregistrarse: TextView
    private val GOOGLE_SIGN_IN = 100

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Inicialización de las variables
        botonlogin = findViewById(R.id.logInButton)
        emailEditText = findViewById(R.id.emailEditText)
        contraseñaEditText = findViewById(R.id.contraseñaEditText)
        botongoogle = findViewById(R.id.googlebutton)
        botonojo = findViewById(R.id.eyeButton)
        loginLayout = findViewById(R.id.LogInLayout)
        textoregistrarse = findViewById(R.id.textoregistrarse)
        preferenceManager = PreferenceManager(applicationContext)

        // Configuración
        setup()

        // Toggle password visibility
        botonojo.setOnClickListener {
            if (contraseñaEditText.transformationMethod is PasswordTransformationMethod) {
                contraseñaEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                botonojo.setImageResource(R.drawable.ic_eye_off)
            } else {
                contraseñaEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                botonojo.setImageResource(R.drawable.ic_eye)
            }
            contraseñaEditText.setSelection(contraseñaEditText.text.length)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            loginLayout.visibility = View.INVISIBLE
            acceder(currentUser.email ?: "", currentUser.uid, ProviderType.BASIC)
        } else {
            loginLayout.visibility = View.VISIBLE
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun setup() {
        title = "Autenticación"

        botonlogin.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = contraseñaEditText.text.toString()

            if (validarEmail(email) && validarContraseña(password)) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            val userId = user?.uid ?: ""
                            preferenceManager.putString(Constantes.KEY_USERS_ID, userId)
                            preferenceManager.putString(Constantes.KEY_EMAIL, email)
                            acceder(email, userId, ProviderType.BASIC)
                            cogeToken()
                        } else {
                            showAlert("Error", "Se ha producido un error autenticando al usuario")
                        }
                    }
            } else {
                showAlert("Error", "Email o contraseña no válidos")
            }
        }

        // Listener para el botón de Google
        botongoogle.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getGoogleClientId(this)).requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        // Listener para el texto de registro
        textoregistrarse.setOnClickListener {
            Log.d("LogInActivity", "textoregistrarse clicked")
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignUp::class.java)
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

    private fun cogeToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val userId = preferenceManager.getString(Constantes.KEY_USERS_ID)
                    if (userId != null) {
                        actualizaToken(userId, token)
                    } else {
                        showAlert("Error", "User ID is null")
                    }
                } else {
                    showAlert("mal", "Error al obtener el token")
                }
            }
    }

    private fun actualizaToken(userId: String, token: String) {
        val documentReference = FirebaseFirestore.getInstance().collection(Constantes.KEY_COLLECTION_USERS).document(userId)
        val data: HashMap<String, Any> = HashMap()
        data[Constantes.KEY_FCM_TOKEN] = token
        data[Constantes.KEY_USERS_ID] = userId

        documentReference.update(data)
            .addOnFailureListener { exception ->
                Log.e("SignUp", "Error updating token: ${exception.message}")
                showAlert("mal", "no se ha introducido el token: ${exception.message}")
            }
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
            Log.w("LogInActivity", "La actividad no está en un estado válido para mostrar un diálogo.")
        }
    }

    private fun acceder(email: String, userId: String, provider: ProviderType) {
        val homeIntent = Intent(this, Home::class.java).apply {
            putExtra("email", email)
            putExtra("userId", userId)
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
                                val email = account.email ?: ""
                                val userId = user?.uid ?: ""
                                preferenceManager.putString(Constantes.KEY_USERS_ID, userId)
                                preferenceManager.putString(Constantes.KEY_EMAIL, email)
                                acceder(email, userId, ProviderType.GOOGLE)
                                cogeToken()
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
