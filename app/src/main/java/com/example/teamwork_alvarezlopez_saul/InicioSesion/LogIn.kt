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

enum class ProviderType{
    BASIC,
    GOOGLE
}
class LogIn : AppCompatActivity() {
    private lateinit var loginButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var contraseñaEditText: EditText
    private lateinit var googleButton: ImageButton
    private lateinit var loginLayout: ConstraintLayout
    private lateinit var textoregistrarse: TextView
    private val GOOGLE_SIGN_IN = 100

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        loginButton = findViewById(R.id.logInButton)
        emailEditText = findViewById(R.id.emailEditText)
        contraseñaEditText = findViewById(R.id.contraseñaEditText)
        loginLayout = findViewById(R.id.LogInLayout)
        textoregistrarse = findViewById(R.id.textoregistrarse)
        googleButton = findViewById(R.id.googlebutton)
        preferenceManager =
            PreferenceManager(
                applicationContext
            )

        setup()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            loginLayout.visibility = View.INVISIBLE
            showHome(currentUser.email ?: "", currentUser.uid, ProviderType.BASIC)
        } else {
            loginLayout.visibility = View.VISIBLE
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun setup() {
        title = "Autenticación"

        loginButton.setOnClickListener {
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
                            showHome(email, userId, ProviderType.BASIC)
                            getToken()
                        } else {
                            showAlert("Error", "Se ha producido un error autenticando al usuario")
                        }
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

        textoregistrarse.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            clearEditTexts()
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validarEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validarContraseña(password: String): Boolean {
        return password.length >= 6
    }

    private fun clearEditTexts() {
        emailEditText.text.clear()
        contraseñaEditText.text.clear()
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

    private fun showHome(email: String, userId: String, provider: ProviderType) {
        val homeIntent = Intent(this, Home::class.java).apply {
            putExtra("email", email)
            putExtra("userId", userId)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
        finish()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val userId = preferenceManager.getString(Constantes.KEY_USERS_ID)
                    if (userId != null) {
                        updateToken(userId, token)
                    } else {
                        showAlert("Error", "User ID is null")
                    }
                } else {
                    showAlert("mal", "Error al obtener el token")
                }
            }
    }

    private fun updateToken(userId: String, token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constantes.KEY_COLLECTION_USERS).document(userId)

        if (userId.isEmpty() || token.isEmpty()) {
            Log.e("SignUp", "Invalid userId or token")
            showAlert("mal", "User ID or token is invalid")
            return
        }

        val data: HashMap<String, Any> = HashMap()
        data[Constantes.KEY_FCM_TOKEN] = token
        data[Constantes.KEY_USERS_ID] = userId

        documentReference.update(data)
            .addOnFailureListener { exception ->
                Log.e("SignUp", "Error updating token: ${exception.message}")
                showAlert("mal", "no se ha introducido el token: ${exception.message}")
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
                                preferenceManager.putString(Constantes.KEY_USERS_ID, userId)
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
