package com.example.covid_19.network

import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @POST("sign_in")
    fun loginUser(@Body body: JsonObject): Single<Response<Any>>

    @POST("sign_up")
    fun signUpUser(@Body body: JsonObject): Single<Response<Any>>

    @POST("met_user")
    fun meetUser(@Body body: JsonObject, @Header("Authorization") jwt: String): Single<Response<Any>>
}