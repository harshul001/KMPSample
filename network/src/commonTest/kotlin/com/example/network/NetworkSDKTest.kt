package com.example.network

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NetworkSDKTest {

    @Test
    fun testNetworkSDKInitialization() {
        NetworkSDK.initialize(
            baseUrl = "https://jsonplaceholder.typicode.com",
            defaultHeaders = mapOf("Accept" to "application/json")
        )

        assertEquals("https://jsonplaceholder.typicode.com", NetworkSDK.getBaseUrl())
        assertEquals(mapOf("Accept" to "application/json"), NetworkSDK.getDefaultHeaders())
    }

    @Test
    fun testBuildUrl() {
        NetworkSDK.initialize(baseUrl = "https://api.example.com/")
        assertEquals("https://api.example.com/users", NetworkSDK.buildUrl("/users"))
        
        NetworkSDK.initialize(baseUrl = "https://api.example.com")
        assertEquals("https://api.example.com/users", NetworkSDK.buildUrl("/users"))
    }

    @Test
    fun testAddDefaultHeader() {
        NetworkSDK.reset()
        NetworkSDK.initialize(baseUrl = "https://api.example.com")
        
        NetworkSDK.addDefaultHeader("Authorization", "Bearer token123")
        NetworkSDK.addDefaultHeader("Custom-Header", "value")

        val headers = NetworkSDK.getDefaultHeaders()
        assertEquals("Bearer token123", headers["Authorization"])
        assertEquals("value", headers["Custom-Header"])
    }

    @Test
    fun testHttpClientNotInitializedError() {
        NetworkSDK.reset()
        
        assertFailsWith<IllegalStateException> {
            NetworkSDK.getHttpClient()
        }
    }

    @Test
    fun testNetworkSDKReset() {
        NetworkSDK.initialize(baseUrl = "https://api.example.com")
        NetworkSDK.addDefaultHeader("Test", "header")
        
        NetworkSDK.reset()
        
        assertEquals("", NetworkSDK.getBaseUrl())
        assertEquals(emptyMap(), NetworkSDK.getDefaultHeaders())
        
        assertFailsWith<IllegalStateException> {
            NetworkSDK.getHttpClient()
        }
    }

    @Test
    fun testMockHttpClient() = runBlocking {
        val mockClient = MockHttpClient()
        NetworkSDK.reset()
        NetworkSDK.initialize(baseUrl = "https://api.example.com")
        NetworkSDK.setHttpClient(mockClient)

        val response = NetworkSDK.get(
            path = "/users",
            deserializer = { it }
        )

        assertEquals(200, response.statusCode)
        assertEquals("mock response", response.data)
    }
}

/**
 * Mock HTTP client for testing
 */
class MockHttpClient : HttpClient {
    override suspend fun request<T>(
        request: HttpRequest,
        deserializer: (String) -> T
    ): HttpResponse<T> {
        val mockData = deserializer("mock response")
        return HttpResponse(
            data = mockData,
            statusCode = 200,
            headers = mapOf("Content-Type" to "application/json")
        )
    }
}

