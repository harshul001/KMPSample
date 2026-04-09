package com.example.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Android implementation of HttpClient using OkHttp
 */
class AndroidHttpClient(
    private val logger: NetworkLogger = DefaultNetworkLogger()
) : HttpClient {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor { message ->
            logger.log(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    override suspend fun request<T>(
        request: HttpRequest,
        deserializer: (String) -> T
    ): HttpResponse<T> {
        val requestBuilder = when (request.method) {
            HttpMethod.GET -> Request.Builder()
                .url(request.url)
                .get()

            HttpMethod.POST -> {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = request.body?.toRequestBody(mediaType)
                Request.Builder()
                    .url(request.url)
                    .post(body ?: "".toRequestBody(mediaType))
            }

            HttpMethod.PUT -> {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = request.body?.toRequestBody(mediaType)
                Request.Builder()
                    .url(request.url)
                    .put(body ?: "".toRequestBody(mediaType))
            }

            HttpMethod.DELETE -> Request.Builder()
                .url(request.url)
                .delete()

            HttpMethod.PATCH -> {
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = request.body?.toRequestBody(mediaType)
                Request.Builder()
                    .url(request.url)
                    .patch(body ?: "".toRequestBody(mediaType))
            }
        }

        // Add headers
        request.headers.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        val okHttpRequest = requestBuilder.build()

        return try {
            val response = okHttpClient.newCall(okHttpRequest).execute()

            if (!response.isSuccessful) {
                throw NetworkException(
                    "HTTP Error ${response.code}: ${response.message}",
                    code = response.code
                )
            }

            val responseBody = response.body?.string()
                ?: throw NetworkException("Empty response body", code = response.code)

            val headers = response.headers.toMap()
            val data = deserializer(responseBody)

            HttpResponse(
                data = data,
                statusCode = response.code,
                headers = headers
            )
        } catch (e: Exception) {
            if (e is NetworkException) {
                throw e
            }
            throw NetworkException("Request failed: ${e.message}", cause = e)
        }
    }
}

/**
 * Factory for creating platform-specific HTTP client
 */
actual fun createHttpClient(logger: NetworkLogger): HttpClient {
    return AndroidHttpClient(logger)
}

