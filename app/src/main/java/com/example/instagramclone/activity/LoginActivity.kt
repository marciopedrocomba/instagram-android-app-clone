package com.example.instagramclone.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.instagramclone.R
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var progressBar: ProgressBar

    private var user: User? = null

    private val auth = FirebaseConfig.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        this.initializeComponents()
        progressBar.visibility = View.GONE
        val loginButton: Button = findViewById(R.id.buttonLogin)
        loginButton.setOnClickListener {

            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if(email.isEmpty()) {
                Toast.makeText(
                    LoginActivity@this,
                    "Please enter email",
                    Toast.LENGTH_SHORT
                ).show()
            }else if (password.isEmpty()) {
                Toast.makeText(
                    LoginActivity@this,
                    "Please enter password",
                    Toast.LENGTH_SHORT
                ).show()
            }else {

                user = User()
                user?.email = email
                user?.password = password
                login(user!!)

            }

        }

    }

    override fun onStart() {
        super.onStart()
        verifyLoggedUser()
    }

    private fun verifyLoggedUser() {
        if (auth.currentUser != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    private fun login(user: User) {
        progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(
            user.email.toString(),
            user.password.toString()
        ).addOnCompleteListener {

            if(it.isSuccessful) {

                progressBar.visibility = View.GONE
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()

            }else {

                progressBar.visibility = View.GONE
                var exception = ""

                try {
                    throw it.exception!!
                }catch (e: FirebaseAuthInvalidCredentialsException) {
                    exception = "E-mail or Password wrong"
                }catch (e: Exception) {
                    exception = "Error: ${e.message}"
                    e.printStackTrace()
                }

                Toast.makeText(
                    LoginActivity@this,
                    exception,
                    Toast.LENGTH_SHORT
                ).show()

            }

        }
    }

    fun initializeComponents() {

        // init config
        editEmail = findViewById(R.id.editTextLoginEmail)
        editPassword = findViewById(R.id.editTextLoginPassword)
        progressBar = findViewById(R.id.progressBarLogin)
        editEmail.requestFocus()

    }

    fun openRegisterActivity(view: View) {
        val intent = Intent(LoginActivity@this, RegisterActivity::class.java)
        startActivity(intent)
    }

}