package com.example.myapplication.data.api

import com.example.myapplication.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://enginesjds.onrender.com/api/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("Set-Cookie")
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(BearerAuthInterceptor())
        .addInterceptor(HttpDiagnosticInterceptor())
        .addInterceptor(loggingInterceptor)
        .build()

    fun setAuthorizationToken(token: String?) {
        AuthTokenStore.set(token)
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit
            .create(ApiService::class.java)
    }
}
