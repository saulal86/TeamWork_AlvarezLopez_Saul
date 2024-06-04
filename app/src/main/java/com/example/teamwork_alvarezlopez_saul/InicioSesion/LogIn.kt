package com.example.teamwork_alvarezlopez_saul.InicioSesion

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.teamwork_alvarezlopez_saul.Notas.Notes
import com.example.teamwork_alvarezlopez_saul.CerrarSesion.ProviderType
import com.example.teamwork_alvarezlopez_saul.Chat.Constantes
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

    private fun setup() {
        title = "Autenticación"

        // Listener para el botón de inicio de sesión
        loginButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && contraseñaEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    emailEditText.text.toString(),
                    contraseñaEditText.text.toString()
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null && user.isEmailVerified) {
                            val correo = emailEditText.text.toString()
                            saveUserEmailToFirestore(user.uid, correo)
                            showHome(user.email ?: "", user.uid ?: "", ProviderType.BASIC)
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

        // Listener para el botón de Google
        googleButton.setOnClickListener {
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

    private fun saveUserEmailToFirestore(userId: String, email: String) {
        val database = FirebaseFirestore.getInstance()
        val data: HashMap<String, Any> = HashMap()
        data[Constantes.KEY_EMAIL] = email

        val documentReference = database.collection(Constantes.KEY_COLLECTION_USERS).document(userId)

        documentReference.set(data)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "La información del usuario ha sido insertada", Toast.LENGTH_SHORT).show()
                getToken(userId)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getToken(userId: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    updateToken(userId, token)
                } else {
                    showAlert("Error", "Error al obtener el token")
                }
            }
    }

    private fun updateToken(userId: String, token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constantes.KEY_COLLECTION_USERS).document(userId)

        val data: HashMap<String, Any> = HashMap()
        data[Constantes.KEY_FCM_TOKEN] = token

        documentReference.update(data)
            .addOnSuccessListener {
                showAlert("Bien", "El token se ha actualizado correctamente")
            }
            .addOnFailureListener { exception ->
                showAlert("Error", "Error al actualizar el token: ${exception.message}")
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

    private fun showHome(email: String, userId: String, provider: ProviderType) {
        val homeIntent = Intent(this, Notes::class.java).apply {
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
                                if (user != null) {
                                    saveUserEmailToFirestore(user.uid, email)
                                    showHome(email, user.uid, ProviderType.GOOGLE)
                                }
                            } else {
                                showAlert("Error", "Ha ocurrido un error al iniciar sesión.")
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
