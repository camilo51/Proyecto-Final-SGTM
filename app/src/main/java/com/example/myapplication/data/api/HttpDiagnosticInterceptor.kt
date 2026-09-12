package com.example.myapplication.data.api

import android.util.Log
import com.example.myapplication.BuildConfig
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/** Debug-only network diagnostics that never print credentials or response bodies in release. */
class HttpDiagnosticInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "REQUEST ${request.method} ${request.url} | " +
                    "Authorization presente: ${!request.header("Authorization").isNullOrBlank()}"
            )
        }

        return try {
            val response = chain.proceed(request)
            if (BuildConfig.DEBUG) {
                val safeErrorBody = if (!response.isSuccessful) {
                    runCatching { response.peekBody(MAX_ERROR_BODY_BYTES).string() }
                        .getOrNull()
                        ?.let(::safeErrorMessage)
                } else {
                    null
                }
                Log.d(
                    TAG,
                    "RESPONSE ${request.method} ${request.url.encodedPath} | " +
                        "HTTP ${response.code}" +
                        (safeErrorBody?.let { " | error=$it" } ?: "")
                )
            }
            response
        } catch (exception: IOException) {
            if (BuildConfig.DEBUG) {
                Log.e(
                    TAG,
                    "NETWORK ${request.method} ${request.url.encodedPath} | " +
                        "${exception::class.simpleName}: ${exception.message}"
                )
            }
            throw exception
        }
    }

    private fun safeErrorMessage(body: String): String {
        val message = MESSAGE_VALUE.find(body)?.groupValues?.getOrNull(1)
        return message?.let { "message=${it.take(MAX_LOG_CHARS)}" } ?: "body_present=true"
    }

    private companion object {
        const val TAG = "SgtmHttp"
        const val MAX_ERROR_BODY_BYTES = 8_192L
        const val MAX_LOG_CHARS = 1_000
        val MESSAGE_VALUE = Regex(
            """(?i)\"message\"\s*:\s*\"([^\"]*)\""""
        )
    }
}
