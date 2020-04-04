package com.example.covid_19

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.covid_19.network.RetrofitService
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.JsonObject
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.editTextPassword
import kotlinx.android.synthetic.main.activity_sign_up.editTextPhoneNumber
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

    private val TAG = "SignUpActivity"

    var verification: String = ""
    var tokenResend: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val body = JsonObject()

        buttonSignUp.setOnClickListener {
            if(editTextPhoneNumber.text.isEmpty()) {
                editTextPhoneNumber.error = "Enter a valid number"
                return@setOnClickListener
            }
            if(editTextPhoneNumber.text.length != 10) {
                editTextPhoneNumber.error = "Phone Number must have 10 digits"
                return@setOnClickListener
            }
            if(editTextPassword.text.isEmpty()) {
                editTextPassword.error = "Create a valid password"
                return@setOnClickListener
            }
            if(editTextPassword.text.length <= 6) {
                editTextPassword.error = "Password must be atleast 7 characters"
                return@setOnClickListener
            }
            if(editTextPasswordConfirmation.text.isEmpty()) {
                editTextPasswordConfirmation.error = "Required"
                return@setOnClickListener
            }
//            if(editTextPasswordConfirmation.text != editTextPassword.text) {
//                editTextPasswordConfirmation.error = "Passwords don't match"
//                return@setOnClickListener
//            }
            if(editTextName.text.isEmpty()) {
                editTextName.error = "Enter a valid name"
                return@setOnClickListener
            }
            progressSignUpActivity.visibility = View.VISIBLE
            body.apply {
                this.addProperty("PhoneNo", editTextPhoneNumber.text.toString())
                this.addProperty("Name", editTextName.text.toString())
                this.addProperty("Password", editTextPasswordConfirmation.text.toString())
            }
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + editTextPhoneNumber.text.toString(),
                120,
                TimeUnit.SECONDS,
                this,
                object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @SuppressLint("CheckResult")
                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                        Toast.makeText(this@SignUpActivity, "OTP Verification Successful", Toast.LENGTH_LONG).show()
                        val sharedPreferences = applicationContext.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
                        sharedPreferences.edit().apply {
                            this.putString("Phone", body["PhoneNo"].toString())
                        }.apply()
                        val token = sharedPreferences.getString("RegTok", "")
                        body.apply {
                            this.addProperty("RegToken", token!!)
                        }
                        RetrofitService().gerRetrofitApiService().signUpUser(body).subscribeOn(Schedulers.io()).subscribe(
                            { response ->
                                Log.d(TAG, "Login Response recived = ${response.body()} \n ${response.code()}")
                                if(response.isSuccessful) {
                                    Log.d(TAG, "LoginSuccessfull")
                                    val jwt = JSONObject(response.body().toString()).getString("token")
                                    Log.d(TAG, "JWT recived = $jwt")
                                    val sharedPreferences = applicationContext.getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
                                    sharedPreferences.edit().apply {
                                        this.putString("JWT", jwt)
                                    }.apply()
                                    progressSignUpActivity.visibility = View.INVISIBLE
                                    /*val serviceIntent = Intent(this@SignUpActivity, ForegroundNearbyService::class.java)
                                    serviceIntent.putExtra("message", "Hello World Vivo")
                                    ContextCompat.startForegroundService(this@SignUpActivity, serviceIntent)*/
                                    finish()
                                    startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                                } else {
                                    Log.e(TAG, "Login Failed")
//                                    Toast.makeText(this@SignUpActivity, "Unable to Login. Please try again", Toast.LENGTH_LONG).show()
                                }
                            },
                            {
                                Log.e(TAG, "Login Error ${it.message.toString()}")
//                                Toast.makeText(this@SignUpActivity, "Unable to Login. Please try again", Toast.LENGTH_LONG).show()
                            })
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        progressSignUpActivity.visibility = View.INVISIBLE
                        Toast.makeText(this@SignUpActivity, "Failed to Verify Phone Number. Try again later", Toast.LENGTH_LONG).show()
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        verification = verificationId
                        tokenResend = token
                    }

                    override fun onCodeAutoRetrievalTimeOut(p0: String) {
                        progressSignUpActivity.visibility = View.INVISIBLE
                        Toast.makeText(this@SignUpActivity, "Auto-Retrieval Failed", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@SignUpActivity, OtpVerificationActivity::class.java)
                        intent.putExtra("VER", verification)
                        intent.putExtra("TOKEN", tokenResend)
                        intent.putExtra("Pho", body["PhoneNo"].toString())
                        intent.putExtra("Name", body["Name"].toString())
                        intent.putExtra("Password", body["Password"].toString())
                        startActivity(intent)
                    }
                }
            )
        }
    }
}
