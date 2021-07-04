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
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.model.User
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var progressBar: ProgressBar

    private var user: User? = null

    private val auth = FirebaseConfig.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeComponents()

        // register user
        progressBar.visibility = View.GONE
        val buttonRegister: Button = findViewById(R.id.buttonRegister)
        buttonRegister.setOnClickListener {

            val name = editName.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(
                    RegisterActivity@this,
                    "Please enter name",
                    Toast.LENGTH_SHORT
                ).show()
            }else if(email.isEmpty()) {
                Toast.makeText(
                    RegisterActivity@this,
                    "Please enter email",
                    Toast.LENGTH_SHORT
                ).show()
            }else if (password.isEmpty()) {
                Toast.makeText(
                    RegisterActivity@this,
                    "Please enter password",
                    Toast.LENGTH_SHORT
                ).show()
            }else {

                user = User()
                user?.name = name
                user?.email = email
                user?.password = password
                register(user!!)

            }


        }

    }

    fun initializeComponents() {

        // init config
        editName = findViewById(R.id.editTextRegisterName)
        editEmail = findViewById(R.id.editTextRegisterEmail)
        editPassword = findViewById(R.id.editTextRegisterPassword)
        progressBar = findViewById(R.id.progressBarRegister)
        editName.requestFocus()

    }

    fun register(user: User) {

        progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(
            user.email.toString(),
            user.password.toString()
        ).addOnCompleteListener {

            if (it.isSuccessful) {

                try {

                    progressBar.visibility = View.VISIBLE

                    // save user data into firebase realtime database
                    val userId: String? = it.result?.user?.uid
                    user.id = userId
                    user.save()

                    // save user firebase profile data
                    FirebaseUser.updateUserName(user.name.toString())

                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()

                }catch (e: Exception) {
                    e.printStackTrace()
                }

            }else {

                progressBar.visibility = View.GONE
                var exception = ""

                try {
                    throw it.exception!!
                }catch (e: FirebaseAuthWeakPasswordException) {
                    exception = "Enter a strong password"
                }catch (e: FirebaseAuthInvalidCredentialsException) {
                    exception = "Please Enter a valid e-mail"
                }catch (e: FirebaseAuthUserCollisionException) {
                    exception = "Account already exists"
                }catch (e: Exception) {
                    exception = "Error: ${e.message}"
                    e.printStackTrace()
                }

                Toast.makeText(
                    RegisterActivity@this,
                    exception,
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    }

}