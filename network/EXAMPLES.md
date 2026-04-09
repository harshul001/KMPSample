# Network SDK Integration Examples

This document provides practical examples of using the Network SDK in different scenarios.

## Quick Start

### 1. Basic Setup

```kotlin
import com.example.network.initializeNetworkSDK
import com.example.network.api.PostsAPI

// Initialize once, preferably in your app startup
fun initializeApp() {
    initializeNetworkSDK(
        baseUrl = "https://jsonplaceholder.typicode.com",
        defaultHeaders = mapOf(
            "Accept" to "application/json",
            "User-Agent" to "MyApp/1.0"
        )
    )
}
```

### 2. Fetch a List of Posts

```kotlin
import com.example.network.api.PostsAPI

suspend fun fetchPosts() {
    try {
        val response = PostsAPI.getPosts()
        println("Status: ${response.statusCode}")
        response.data.forEach { post ->
            println("- ${post.title}")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

### 3. Fetch a Single Item

```kotlin
suspend fun fetchSinglePost(id: Int) {
    try {
        val response = PostsAPI.getPostById(id)
        val post = response.data
        println("Title: ${post.title}")
        println("Body: ${post.body}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

### 4. Create New Item (POST)

```kotlin
suspend fun createNewPost() {
    try {
        val response = PostsAPI.createPost(
            userId = 1,
            title = "My Awesome Post",
            body = "This is the body of my awesome post"
        )
        println("Created post with ID: ${response.data.id}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

## Advanced Examples

### Custom Deserialization

```kotlin
import com.example.network.api.JsonSerializer
import com.example.network.NetworkSDK

data class CustomData(val value: String)

suspend fun customDeserialization() {
    val response = NetworkSDK.get(
        path = "/custom-endpoint",
        deserializer = { json ->
            // Custom parsing logic
            CustomData(value = json)
        }
    )
}
```

### Custom Headers per Request

```kotlin
import com.example.network.api.PostsAPI
import com.example.network.api.JsonSerializer
import com.example.network.NetworkSDK

suspend fun postWithAuthToken(token: String) {
    val response = NetworkSDK.post(
        path = "/posts",
        body = """{"userId": 1, "title": "Test", "body": "Test"}""",
        headers = mapOf(
            "Authorization" to "Bearer $token",
            "X-Custom-Header" to "value"
        ),
        deserializer = { JsonSerializer.deserialize(it) }
    )
}
```

### Error Handling

```kotlin
import com.example.network.NetworkException
import com.example.network.api.PostsAPI

suspend fun robustFetching() {
    try {
        val response = PostsAPI.getPosts()
    } catch (e: NetworkException) {
        // Handle HTTP errors
        println("HTTP Error Code: ${e.code}")
        println("Error: ${e.message}")
    } catch (e: Exception) {
        // Handle other errors
        println("Unexpected error: ${e.message}")
    }
}
```

## Platform-Specific Examples

### Android Example

```kotlin
import com.example.network.initializeNetworkSDK
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@Composable
fun PostsScreen() {
    var posts by remember { mutableStateOf(emptyList<Post>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        initializeNetworkSDK("https://jsonplaceholder.typicode.com")
    }

    Button(onClick = {
        coroutineScope.launch {
            val response = PostsAPI.getPosts()
            posts = response.data
        }
    }) {
        Text("Load Posts")
    }

    LazyColumn {
        items(posts) { post ->
            PostItem(post)
        }
    }
}
```

### iOS Example

```kotlin
import com.example.network.initializeNetworkSDK
import com.example.network.api.PostsAPI

// In your iOS Swift code, call Kotlin functions:
// NetworkSDKKt.initializeNetworkSDK(...)
// PostsAPIKt.getPosts(...)

// The framework is accessible in your SwiftUI views
// See the iosApp example for integration details
```

### Web Example

```kotlin
import com.example.network.initializeNetworkSDK
import com.example.network.api.PostsAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    initializeNetworkSDK("https://jsonplaceholder.typicode.com")

    GlobalScope.launch {
        try {
            val response = PostsAPI.getPosts()
            console.log("Posts: ${response.data}")
        } catch (e: Exception) {
            console.error("Error: ${e.message}")
        }
    }
}
```

## Compose Integration

### ViewModel with Network Calls

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.network.api.PostsAPI

class PostsViewModel : ViewModel() {
    private val _posts = mutableStateOf<List<Post>>(emptyList())
    val posts: State<List<Post>> = _posts

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun loadPosts() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = PostsAPI.getPosts()
                _posts.value = response.data
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
```

### Using ViewModel in Compose

```kotlin
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PostsListScreen() {
    val viewModel: PostsViewModel = viewModel()
    
    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    when {
        viewModel.loading.value -> {
            CircularProgressIndicator()
        }
        viewModel.error.value != null -> {
            Text("Error: ${viewModel.error.value}")
        }
        else -> {
            LazyColumn {
                items(viewModel.posts.value) { post ->
                    PostItem(post)
                }
            }
        }
    }
}
```

## Dependency Injection Example

```kotlin
import com.example.network.HttpClient
import com.example.network.NetworkSDK

// Custom dependency container
object AppContainer {
    fun initializeNetwork(
        baseUrl: String,
        customHttpClient: HttpClient? = null
    ) {
        val httpClient = customHttpClient ?: createDefaultHttpClient()
        NetworkSDK.setHttpClient(httpClient)
        NetworkSDK.initialize(baseUrl)
    }

    private fun createDefaultHttpClient(): HttpClient {
        return createHttpClient(DefaultNetworkLogger())
    }
}

// Usage
AppContainer.initializeNetwork("https://api.example.com")
```

## Testing Examples

### Mock Network Client

```kotlin
import com.example.network.HttpClient
import com.example.network.HttpRequest
import com.example.network.HttpResponse
import com.example.network.NetworkSDK

class MockHttpClient : HttpClient {
    private val responses = mutableMapOf<String, String>()

    fun addResponse(path: String, json: String) {
        responses[path] = json
    }

    override suspend fun request<T>(
        request: HttpRequest,
        deserializer: (String) -> T
    ): HttpResponse<T> {
        val responseJson = responses[request.url] 
            ?: throw Exception("No mock response for ${request.url}")
        val data = deserializer(responseJson)
        return HttpResponse(data, 200)
    }
}

// Usage in tests
@Test
fun testPostsAPI() = runBlocking {
    val mockClient = MockHttpClient()
    mockClient.addResponse(
        "https://api.example.com/posts",
        """[{"id": 1, "title": "Test"}]"""
    )

    NetworkSDK.reset()
    NetworkSDK.initialize("https://api.example.com")
    NetworkSDK.setHttpClient(mockClient)

    val response = PostsAPI.getPosts()
    assertEquals(1, response.data.size)
}
```

## Best Practices

### 1. Initialize Once

```kotlin
// Good: Initialize in app startup
fun main() {
    initializeNetworkSDK("https://api.example.com")
    // rest of app
}

// Bad: Initializing multiple times
fun fetchData() {
    initializeNetworkSDK("https://api.example.com") // Don't do this
    // ...
}
```

### 2. Handle Errors Gracefully

```kotlin
// Good
try {
    val data = PostsAPI.getPosts()
} catch (e: NetworkException) {
    logger.error("Network error: ${e.code}")
} catch (e: Exception) {
    logger.error("Unexpected error: ${e.message}")
}

// Avoid
try {
    val data = PostsAPI.getPosts()
} catch (e: Exception) {
    // Generic catch is too broad
    throw e
}
```

### 3. Use ViewModel for State Management

```kotlin
// Good: Centralized state management
class MyViewModel : ViewModel() {
    private val _data = mutableStateOf<Data?>(null)
    
    fun loadData() {
        viewModelScope.launch {
            try {
                _data.value = API.getData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

// Avoid: State in Composable
@Composable
fun Screen() {
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<Data?>(null) }
    
    // Triggers recomposition on every change
    scope.launch {
        data = API.getData()
    }
}
```

### 4. Type-Safe API Definitions

```kotlin
// Good: Strongly typed
object MyAPI {
    suspend fun getUsers(): HttpResponse<List<User>> {
        // Implementation
    }
}

// Avoid: Stringly typed
suspend fun getUsers(): HttpResponse<String> {
    // Returns JSON string, needs manual parsing
}
```

## Production Checklist

- [ ] Initialize NetworkSDK with correct base URL
- [ ] Add proper error handling
- [ ] Implement logging
- [ ] Use HTTPS for production APIs
- [ ] Add request/response timeouts
- [ ] Set up retry logic for failed requests
- [ ] Test on all target platforms
- [ ] Configure API authentication headers
- [ ] Add request/response interceptors if needed
- [ ] Monitor and log network performance
- [ ] Handle network unavailability gracefully

