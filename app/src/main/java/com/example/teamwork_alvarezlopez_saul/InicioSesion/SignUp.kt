package com.example.teamwork_alvarezlopez_saul.InicioSesion

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
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

enum class ProviderType {
    BASIC,
    GOOGLE
}

class SignUp : AppCompatActivity() {
    private lateinit var botonregistrar: Button
    private lateinit var editTextemail: EditText
    private lateinit var contraseñaEditText: EditText
    private lateinit var confirmarcontraseñaEditText: EditText
    private lateinit var botongoogle: ImageButton
    private lateinit var SignUpLayout: ConstraintLayout
    private lateinit var textoiniciarsesion: TextView
    private lateinit var ojocontraseña: ImageButton
    private lateinit var ojoconfirmarcontraseña: ImageButton
    private var contravisible = false
    private var contraconfirvisible = false
    private val GOOGLE_SIGN_IN = 100

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Inicialización de las variables
        botonregistrar = findViewById(R.id.signUpbutton)
        editTextemail = findViewById(R.id.emailEditText)
        contraseñaEditText = findViewById(R.id.contraseñaEditText)
        SignUpLayout = findViewById(R.id.SignUpLayout)
        confirmarcontraseñaEditText = findViewById(R.id.confirmarcontraseñaEditText)
        textoiniciarsesion = findViewById(R.id.textoiniciarsesion)
        botongoogle = findViewById(R.id.googlebutton)
        ojocontraseña = findViewById(R.id.togglePasswordVisibilityButton)
        ojoconfirmarcontraseña = findViewById(R.id.toggleConfirmPasswordVisibilityButton)
        preferenceManager = PreferenceManager(applicationContext)

        ojocontraseña.setOnClickListener {
            if (contravisible) {
                contraseñaEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                ojocontraseña.setImageResource(R.drawable.ic_eye)
            } else {
                contraseñaEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ojocontraseña.setImageResource(R.drawable.ic_eye_off)
            }
            contravisible = !contravisible
            contraseñaEditText.setSelection(contraseñaEditText.text.length) // Para mover el cursor al final del texto
        }

        ojoconfirmarcontraseña.setOnClickListener {
            if (contraconfirvisible) {
                confirmarcontraseñaEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                ojoconfirmarcontraseña.setImageResource(R.drawable.ic_eye)
            } else {
                confirmarcontraseñaEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ojoconfirmarcontraseña.setImageResource(R.drawable.ic_eye_off)
            }
            contraconfirvisible = !contraconfirvisible
            confirmarcontraseñaEditText.setSelection(confirmarcontraseñaEditText.text.length) // Para mover el cursor al final del texto
        }

        // Configuración
        setup()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            SignUpLayout.visibility = View.INVISIBLE
            acceder(currentUser.email ?: "", currentUser.uid, ProviderType.BASIC)
        } else {
            SignUpLayout.visibility = View.VISIBLE
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun setup() {
        title = "Autenticación"

        botonregistrar.setOnClickListener {
            val email = editTextemail.text.toString()
            val password = contraseñaEditText.text.toString()
            val confirmPassword = confirmarcontraseñaEditText.text.toString()

            if (validaremail(email) && validarcontraseña(password)) {
                if (password == confirmPassword) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                val userId = user?.uid ?: ""
                                preferenceManager.putString(Constantes.KEY_USERS_ID, userId)
                                preferenceManager.putString(Constantes.KEY_EMAIL, email)
                                Log.d("SignUp", "User ID saved: $userId")
                                guardaUserFirebase(userId, email) // Guarda el userId y el email en Firestore
                                correoVerificacion(email, userId, ProviderType.BASIC)
                                cogeToken()
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

        botongoogle.setOnClickListener {
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
                                val userId = user?.uid ?: ""
                                preferenceManager.putString(Constantes.KEY_USERS_ID, userId)
                                guardaUserFirebase(userId, email) // Guarda el userId y el email en Firestore
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

    private fun validaremail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|es)$")
        val isValid = emailPattern.matches(email)
        Log.d("SignUp", "Email validation for $email: $isValid")
        return isValid
    }

    private fun validarcontraseña(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\|,.<>\\/?]).{8,}$")
        val isValid = passwordPattern.matches(password)
        Log.d("SignUp", "Password validation for $password: $isValid")
        return isValid
    }

    private fun guardaUserFirebase(userId: String, email: String) {
        val database = FirebaseFirestore.getInstance()
        val data: HashMap<String, Any> = HashMap()
        data[Constantes.KEY_EMAIL] = email
        data[Constantes.KEY_USERS_ID] = userId
        val documentReference = database.collection(Constantes.KEY_COLLECTION_USERS).document(userId)

        documentReference.set(data)
            .addOnSuccessListener {
                Log.d("Firestore", "User data successfully written!")
                Toast.makeText(applicationContext, "La información del usuario ha sido insertada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error writing document", exception)
                Toast.makeText(applicationContext, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun correoVerificacion(email: String, userId: String, provider: ProviderType) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (provider == ProviderType.GOOGLE || (currentUser != null && currentUser.isEmailVerified)) {
            acceder(email, userId, provider)
        } else {
            currentUser?.sendEmailVerification()
            alertanoverificado(email, userId, provider)
        }
    }

    private fun alertanoverificado(email: String, userId: String, provider: ProviderType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Correo electrónico no verificado")
        builder.setMessage("Recuerda verificar tu correo electrónico para mayor seguridad.")
        builder.setPositiveButton("Aceptar") { _, _ ->
            acceder(email, userId, provider)
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
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

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
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
                    showAlert("Error", "Error al obtener el token")
                }
            }
    }

    private fun actualizaToken(userId: String, token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constantes.KEY_COLLECTION_USERS).document(userId)

        if (userId.isEmpty() || token.isEmpty()) {
            Log.e("SignUp", "Invalid userId or token")
            showAlert("Error", "User ID or token is invalid")
            return
        }

        val data: HashMap<String, Any> = HashMap()
        data[Constantes.KEY_FCM_TOKEN] = token
        data[Constantes.KEY_USERS_ID] = userId

        documentReference.update(data)
            .addOnFailureListener { exception ->
                Log.e("SignUp", "Error updating token: ${exception.message}")
                showAlert("Error", "no se ha introducido el token: ${exception.message}")
            }
    }
}
