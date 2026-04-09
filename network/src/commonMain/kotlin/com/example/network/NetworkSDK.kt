package com.example.network

/**
 * Network SDK manager - configurable entry point for all network operations
 */
object NetworkSDK {
    private var httpClient: HttpClient? = null
    private var logger: NetworkLogger = DefaultNetworkLogger()
    private var baseUrl: String = ""
    private var defaultHeaders: MutableMap<String, String> = mutableMapOf()

    /**
     * Initialize the NetworkSDK with configuration
     */
    fun initialize(
        baseUrl: String,
        defaultHeaders: Map<String, String> = emptyMap(),
        logger: NetworkLogger = DefaultNetworkLogger()
    ) {
        this.baseUrl = baseUrl
        this.defaultHeaders = defaultHeaders.toMutableMap()
        this.logger = logger
        logger.log("NetworkSDK initialized with baseUrl: $baseUrl")
    }

    /**
     * Set or replace the HTTP client implementation
     */
    fun setHttpClient(client: HttpClient) {
        this.httpClient = client
        logger.log("HTTP client set: ${client::class.simpleName}")
    }

    /**
     * Get the current HTTP client (or create a default one if needed)
     */
    fun getHttpClient(): HttpClient {
        if (httpClient == null) {
            throw IllegalStateException(
                "HTTP client not initialized. Call setHttpClient() first or initialize with initialize()"
            )
        }
        return httpClient!!
    }

    /**
     * Add or override a default header
     */
    fun addDefaultHeader(key: String, value: String) {
        defaultHeaders[key] = value
        logger.log("Added default header: $key")
    }

    /**
     * Get all default headers
     */
    fun getDefaultHeaders(): Map<String, String> = defaultHeaders.toMap()

    /**
     * Get the base URL
     */
    fun getBaseUrl(): String = baseUrl

    /**
     * Combine base URL with path
     */
    fun buildUrl(path: String): String {
        val cleanBase = baseUrl.trimEnd('/')
        val cleanPath = path.trimStart('/')
        return "$cleanBase/$cleanPath"
    }

    /**
     * Make a GET request
     */
    suspend inline fun <reified T> get(
        path: String,
        headers: Map<String, String> = emptyMap(),
        noinline deserializer: (String) -> T
    ): HttpResponse<T> {
        val url = buildUrl(path)
        val mergedHeaders = defaultHeaders + headers
        logger.log("GET request to: $url")
        return try {
            getHttpClient().get(url, mergedHeaders, deserializer)
        } catch (e: Exception) {
            logger.error("GET request failed: $url", e)
            throw e
        }
    }

    /**
     * Make a POST request
     */
    suspend inline fun <reified T> post(
        path: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        noinline deserializer: (String) -> T
    ): HttpResponse<T> {
        val url = buildUrl(path)
        val mergedHeaders = defaultHeaders + headers
        logger.log("POST request to: $url")
        return try {
            getHttpClient().post(url, body, mergedHeaders, deserializer)
        } catch (e: Exception) {
            logger.error("POST request failed: $url", e)
            throw e
        }
    }

    /**
     * Make a PUT request
     */
    suspend inline fun <reified T> put(
        path: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        noinline deserializer: (String) -> T
    ): HttpResponse<T> {
        val url = buildUrl(path)
        val mergedHeaders = defaultHeaders + headers
        logger.log("PUT request to: $url")
        return try {
            getHttpClient().put(url, body, mergedHeaders, deserializer)
        } catch (e: Exception) {
            logger.error("PUT request failed: $url", e)
            throw e
        }
    }

    /**
     * Make a DELETE request
     */
    suspend inline fun <reified T> delete(
        path: String,
        headers: Map<String, String> = emptyMap(),
        noinline deserializer: (String) -> T
    ): HttpResponse<T> {
        val url = buildUrl(path)
        val mergedHeaders = defaultHeaders + headers
        logger.log("DELETE request to: $url")
        return try {
            getHttpClient().delete(url, mergedHeaders, deserializer)
        } catch (e: Exception) {
            logger.error("DELETE request failed: $url", e)
            throw e
        }
    }

    /**
     * Reset the SDK to initial state
     */
    fun reset() {
        httpClient = null
        baseUrl = ""
        defaultHeaders.clear()
        logger.log("NetworkSDK reset")
    }
}

