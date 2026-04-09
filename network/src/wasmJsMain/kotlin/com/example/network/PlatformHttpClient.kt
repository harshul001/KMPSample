package com.example.network

import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.browser.window

/**
 * Wasm/JS implementation of HttpClient using Fetch API
 */
class WasmJsHttpClient(
    private val logger: NetworkLogger = DefaultNetworkLogger()
) : HttpClient {

    override suspend fun request<T>(
        request: HttpRequest,
        deserializer: (String) -> T
    ): HttpResponse<T> {
        return try {
            logger.log("${request.method.name} request to: ${request.url}")

            val requestInit = RequestInit(
                method = request.method.name,
                headers = request.headers.toJsMap(),
                body = request.body
            )

            val response = window.fetch(request.url, requestInit).await() as Response

            if (!response.ok) {
                throw NetworkException(
                    "HTTP Error ${response.status}: ${response.statusText}",
                    code = response.status.toInt()
                )
            }

            val responseBody = response.text().await()
            val statusCode = response.status.toInt()
            val headers = extractHeaders(response)
            val data = deserializer(responseBody)

            HttpResponse(
                data = data,
                statusCode = statusCode,
                headers = headers
            )
        } catch (e: Exception) {
            if (e is NetworkException) {
                throw e
            }
            logger.error("Request failed", e)
            throw NetworkException("Request failed: ${e.message}", cause = e)
        }
    }
}

private fun Map<String, String>.toJsMap(): dynamic {
    val jsMap = js("({})")
    forEach { (key, value) ->
        jsMap[key] = value
    }
    return jsMap
}

private fun extractHeaders(response: Response): Map<String, String> {
    val headers = mutableMapOf<String, String>()
    try {
        response.headers.forEach { key, value ->
            headers[key] = value
        }
    } catch (e: Exception) {
        // Headers might not be accessible in all cases
    }
    return headers
}

/**
 * Factory for creating platform-specific HTTP client
 */
actual fun createHttpClient(logger: NetworkLogger): HttpClient {
    return WasmJsHttpClient(logger)
}

