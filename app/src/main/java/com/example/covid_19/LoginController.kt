package com.example.covid_19

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.covid_19.network.RetrofitService
import com.google.gson.JsonObject
import io.reactivex.schedulers.Schedulers

interface LoginListener {
    fun onLoginSuccess(jwt: String)
    fun onLoginFailure(error: String, handleMechanism: Int)
}

class LoginController(val loginListener: LoginListener, val context: Context) {

    private val HANDLE_MECHANISM_TOAST = 0
    private val TAG = "LoginController"

    @SuppressLint("CheckResult")
    fun loginUser(phoneNumber: String, password: String) {
        val sharedPreferences = context.getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            this.putString("Phone", phoneNumber)
        }.apply()
        val token = sharedPreferences.getString("RegToken", "")
        val body = JsonObject()
        body.apply {
            this.addProperty("PhoneNo", phoneNumber)
            this.addProperty("Password", password)
            this.addProperty("regToken", token!!)
        }
        RetrofitService().gerRetrofitApiService().loginUser(body).subscribeOn(Schedulers.io()).subscribe(
        { response ->
            Log.d(TAG, "Login Response recived = ${response.body()} \n ${response.code()}")
            if(response.isSuccessful) {
                Log.d(TAG, "LoginSuccessfull")
                val jwt = JsonObject().getAsJsonObject(response.body().toString())["token"].toString()
                Log.d(TAG, "JWT recived = $jwt")
                val sharedPreferences = context.getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    this.putString("JWT", jwt)
                }.apply()
                loginListener.onLoginSuccess(jwt)
            } else {
                Log.e(TAG, "Login Failed")
                loginListener.onLoginFailure("Unable to Login. Please try again", HANDLE_MECHANISM_TOAST)
            }
        },
        {
            Log.e(TAG, "Login Error ${it.message.toString()}")
            loginListener.onLoginFailure("Unable to Login. Please try again", HANDLE_MECHANISM_TOAST)
        })
    }
}