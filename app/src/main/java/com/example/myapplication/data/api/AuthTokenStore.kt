package com.example.myapplication.data.api

import okhttp3.Interceptor
import okhttp3.Response

/** Keeps the access token only for the lifetime of the current app process. */
object AuthTokenStore {
    @Volatile
    private var accessToken: String? = null

    fun set(token: String?) {
        accessToken = token?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun clear() {
        accessToken = null
    }

    fun authorizationHeader(): String? = accessToken?.let { "Bearer $it" }
}

class BearerAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val hasAuthorization = request.header("Authorization") != null
        val isPublicAuth = request.url.encodedPath.endsWith("/auth/login") ||
            request.url.encodedPath.endsWith("/auth/register") ||
            request.url.encodedPath.endsWith("/auth/forgot-password") ||
            request.url.encodedPath.endsWith("/auth/reset-password")

        if (hasAuthorization || isPublicAuth) return chain.proceed(request)

        val authorization = AuthTokenStore.authorizationHeader()
            ?: return chain.proceed(request)

        return chain.proceed(
            request.newBuilder()
                .header("Authorization", authorization)
                .build()
        )
    }
}
