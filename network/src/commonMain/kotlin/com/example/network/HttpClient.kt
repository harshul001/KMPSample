package com.example.network

/**
 * Exception thrown when an HTTP request fails
 */
class NetworkException(message: String, val code: Int? = null, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * HTTP response wrapper
 */
data class HttpResponse<T>(
    val data: T,
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap()
)

/**
 * HTTP request configuration
 */
data class HttpRequest(
    val url: String,
    val method: HttpMethod = HttpMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

/**
 * HTTP methods
 */
enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH
}

/**
 * Network client interface
 */
interface HttpClient {
    suspend fun request<T>(
        request: HttpRequest,
        deserializer: (String) -> T
    ): HttpResponse<T>

    suspend fun get<T>(
        url: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): HttpResponse<T> {
        return request(
            HttpRequest(url = url, method = HttpMethod.GET, headers = headers),
            deserializer
        )
    }

    suspend fun post<T>(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): HttpResponse<T> {
        return request(
            HttpRequest(url = url, method = HttpMethod.POST, headers = headers, body = body),
            deserializer
        )
    }

    suspend fun put<T>(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): HttpResponse<T> {
        return request(
            HttpRequest(url = url, method = HttpMethod.PUT, headers = headers, body = body),
            deserializer
        )
    }

    suspend fun delete<T>(
        url: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): HttpResponse<T> {
        return request(
            HttpRequest(url = url, method = HttpMethod.DELETE, headers = headers),
            deserializer
        )
    }
}

/**
 * Logger interface for network operations
 */
interface NetworkLogger {
    fun log(message: String)
    fun error(message: String, exception: Throwable? = null)
}

/**
 * Default logger implementation
 */
class DefaultNetworkLogger : NetworkLogger {
    override fun log(message: String) {
        println("[Network] $message")
    }

    override fun error(message: String, exception: Throwable?) {
        println("[Network ERROR] $message")
        exception?.printStackTrace()
    }
}

