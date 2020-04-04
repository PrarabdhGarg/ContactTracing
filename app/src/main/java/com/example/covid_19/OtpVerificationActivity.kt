package com.example.covid_19

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.covid_19.network.RetrofitService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.JsonObject
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_otp_verification.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONObject

class OtpVerificationActivity : AppCompatActivity() {

    private val TAG = "OtpVerificationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        val verificationId = intent.extras?.get("VER").toString()
        val body = JsonObject().apply {
            this.addProperty("PhoneNo", intent.extras?.get("Pho").toString())
            this.addProperty("Name", intent.extras?.get("Name").toString())
            this.addProperty("Password", intent.extras?.get("Password").toString())
        }

        buttonVerifyOtp.setOnClickListener {
            if(editTextOtp.text != null || editTextOtp.text.toString().length != 4) {
                editTextOtp.error = "Enter valid verification code"
                return@setOnClickListener
            }
            progressOtpVerification.visibility = View.VISIBLE
            val phoneAuth = PhoneAuthProvider.getCredential(verificationId, editTextOtp.text.toString())
            FirebaseAuth.getInstance().signInWithCredential(phoneAuth).addOnSuccessListener {
                progressOtpVerification.visibility = View.INVISIBLE
                val sharedPreferences = applicationContext.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    this.putString("Phone", body["PhoneNo"].toString())
                }.apply()
                val token = sharedPreferences.getString("RegTok", "")
                body.apply {
                    this.addProperty("RegToken", token!!)
                }
                Toast.makeText(this, "OTP Verification successful", Toast.LENGTH_LONG).show()
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
                                progressOtpVerification.visibility = View.INVISIBLE
                                /*val serviceIntent = Intent(this, ForegroundNearbyService::class.java)
                                serviceIntent.putExtra("message", "Hello World Vivo")
                                ContextCompat.startForegroundService(this, serviceIntent)*/
                                finish()
                                startActivity(Intent(this, MainActivity::class.java))
                            } else {
                                Log.e(TAG, "Login Failed")
                                Toast.makeText(this, "Unable to Login. Please try again", Toast.LENGTH_LONG).show()
                            }
                        },
                        {
                            Log.e(TAG, "Login Error ${it.message.toString()}")
                            Toast.makeText(this, "Unable to Login. Please try again", Toast.LENGTH_LONG).show()
                        })
            }.addOnFailureListener {
                progressOtpVerification.visibility = View.INVISIBLE
                editTextOtp.setText("")
                Toast.makeText(this, "OTP Verification Unsuccessful. Try again later", Toast.LENGTH_LONG).show()
            }
        }
    }
}
