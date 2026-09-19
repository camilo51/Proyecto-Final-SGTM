package com.example.myapplication.data.api

import com.example.myapplication.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://enginesjds.onrender.com/api/"
    @Volatile
    private var accessToken: String? = null

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
        .addInterceptor(loggingInterceptor)
        .build()

    fun setAuthorizationToken(token: String?) {
        accessToken = token?.trim()?.takeIf { it.isNotEmpty() }
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

    private class BearerAuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val hasAuthorization = request.header("Authorization") != null
            val isPublicAuth = request.url.encodedPath.endsWith("/auth/login") ||
                request.url.encodedPath.endsWith("/auth/register") ||
                request.url.encodedPath.endsWith("/auth/forgot-password") ||
                request.url.encodedPath.endsWith("/auth/reset-password")

            if (hasAuthorization || isPublicAuth) return chain.proceed(request)

            val authorization = accessToken?.let { "Bearer $it" }
                ?: return chain.proceed(request)

            return chain.proceed(
                request.newBuilder()
                    .header("Authorization", authorization)
                    .build()
            )
        }
    }
}
