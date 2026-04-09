package com.example.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.Foundation.*

/**
 * iOS implementation of HttpClient using NSURLSession (platform native)
 */
class IosHttpClient(
    private val logger: NetworkLogger = DefaultNetworkLogger()
) : HttpClient {

    override suspend fun request<T>(
        request: HttpRequest,
        deserializer: (String) -> T
    ): HttpResponse<T> {
        return try {
            val url = NSURL.URLWithString(request.url)
                ?: throw NetworkException("Invalid URL: ${request.url}")

            val urlRequest = NSMutableURLRequest(uRL = url).apply {
                HTTPMethod = request.method.name
                request.headers.forEach { (key, value) ->
                    setValue(value, forHTTPHeaderField = key)
                }
                request.body?.let {
                    HTTPBody = it.encodeToByteArray().toNSData()
                }
            }

            logger.log("${request.method.name} request to: ${request.url}")

            val session = NSURLSession.sharedSession()
            val response = session.sendSynchronousRequest(urlRequest)
            val httpResponse = response.component2() as? NSHTTPURLResponse
                ?: throw NetworkException("Failed to get HTTP response")

            val statusCode = httpResponse.statusCode.toInt()
            if (statusCode !in 200..299) {
                throw NetworkException(
                    "HTTP Error $statusCode: ${httpResponse.statusCode}",
                    code = statusCode
                )
            }

            val responseData = response.component1() as? NSData
                ?: throw NetworkException("Empty response body")

            val responseBody = responseData.toKString()
            val headers = (httpResponse.allHeaderFields as? Map<String, String>)?.toMap() ?: emptyMap()
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

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return NSData(bytes = this.toCValues(), length = this.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toKString(): String {
    val bytes = ByteArray(length.toInt())
    bytes.forEachIndexed { index, _ ->
        bytes[index] = (this.bytes?.get(index) as? NSNumber)?.charValue?.code?.toByte() ?: 0
    }
    return bytes.decodeToString()
}

/**
 * Factory for creating platform-specific HTTP client
 */
actual fun createHttpClient(logger: NetworkLogger): HttpClient {
    return IosHttpClient(logger)
}

