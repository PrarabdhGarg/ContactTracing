package com.example.covid_19.network

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitService {
    private var retrofit: Retrofit? = null

    fun gerRetrofitApiService(): ApiService {
        if(retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl("https://dushyant.tech/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}