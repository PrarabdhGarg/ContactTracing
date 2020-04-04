package com.example.covid_19

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), LoginListener {

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginController = LoginController(this, applicationContext)

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if(it.isSuccessful) {
                Log.d(TAG, "Sucessfully recived regToken = ${it.result!!.token}")
                val sharedPreferences = applicationContext.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    this.putString("RegTok", it.result!!.token)
                }.apply()
            } else {
                Log.e(TAG, "Failed to complete registration token reteival")
                return@addOnCompleteListener
            }
        }

        buttonLogin.setOnClickListener {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            if(editTextPhoneNumber.text == null || editTextPhoneNumber.text.toString().length != 10) {
                editTextPhoneNumber.error = "Please enter a valid phone number"
                return@setOnClickListener
            }
            if(editTextPassword.text == null) {
                editTextPassword.error = "Please enter a password"
                return@setOnClickListener
            }
            if(editTextPassword.text != null && editTextPassword.text.toString().length <= 6) {
                editTextPassword.error = "Password cannot be less than 7 characters"
                return@setOnClickListener
            }
            progressLoginActivity.visibility = View.VISIBLE
            loginController.loginUser(editTextPhoneNumber.text.toString(), editTextPassword.text.toString())
        }

        textCreateAccount.setOnClickListener {
            Log.d(TAG, "Pressed Signup Button")
            finish()
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    override fun onLoginSuccess(jwt: String) {
        // progressLoginActivity.visibility = View.INVISIBLE
        /*val serviceIntent = Intent(this, ForegroundNearbyService::class.java)
        serviceIntent.putExtra("message", "Hello World Vivo")
        ContextCompat.startForegroundService(this, serviceIntent)*/
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onLoginFailure(error: String, handleMechanism: Int) {
        progressLoginActivity.visibility = View.INVISIBLE
        when(handleMechanism) {
            0 -> {
                runOnUiThread {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
