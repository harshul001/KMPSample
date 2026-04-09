package com.example.network

/**
 * Platform-specific HTTP client factory
 * Each platform provides its own implementation
 */
expect fun createHttpClient(logger: NetworkLogger): HttpClient

/**
 * Initialize NetworkSDK with platform-specific HTTP client
 */
fun initializeNetworkSDK(
    baseUrl: String,
    defaultHeaders: Map<String, String> = emptyMap(),
    logger: NetworkLogger = DefaultNetworkLogger()
) {
    val httpClient = createHttpClient(logger)
    NetworkSDK.setHttpClient(httpClient)
    NetworkSDK.initialize(baseUrl, defaultHeaders, logger)
}

