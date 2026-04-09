# Network SDK - Quick Reference Card

## Initialize (Call Once)

```kotlin
import com.example.network.initializeNetworkSDK

initializeNetworkSDK(
    baseUrl = "https://jsonplaceholder.typicode.com",
    defaultHeaders = mapOf("Accept" to "application/json")
)
```

## API Calls

### GET - All Posts
```kotlin
import com.example.network.api.PostsAPI

val response = PostsAPI.getPosts()
response.data.forEach { post -> println(post.title) }
```

### GET - Single Post
```kotlin
val response = PostsAPI.getPostById(1)
println(response.data.title)
```

### POST - Create Post
```kotlin
val response = PostsAPI.createPost(
    userId = 1,
    title = "My Post",
    body = "Content"
)
println("Created: ID ${response.data.id}")
```

### PUT - Update Post
```kotlin
val response = PostsAPI.updatePost(
    id = 1,
    userId = 1,
    title = "Updated",
    body = "Updated content"
)
```

### DELETE - Delete Post
```kotlin
val response = PostsAPI.deletePost(1)
println("Status: ${response.statusCode}")
```

## Error Handling

```kotlin
import com.example.network.NetworkException

try {
    val response = PostsAPI.getPosts()
} catch (e: NetworkException) {
    println("HTTP ${e.code}: ${e.message}")
} catch (e: Exception) {
    println("Error: ${e.message}")
}
```

## Response Structure

```kotlin
data class HttpResponse<T>(
    val data: T,              // Deserialized response data
    val statusCode: Int,      // HTTP status (200, 404, etc)
    val headers: Map<String, String>  // Response headers
)
```

## Available Models

```kotlin
@Serializable
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

@Serializable
data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String
)

@Serializable
data class Comment(
    val postId: Int,
    val id: Int,
    val name: String,
    val email: String,
    val body: String
)
```

## Custom Headers per Request

```kotlin
val response = PostsAPI.createPost(
    userId = 1,
    title = "Test",
    body = "Test",
    headers = mapOf("X-Custom-Header" to "value")
)
```

## Add Global Header

```kotlin
import com.example.network.NetworkSDK

NetworkSDK.addDefaultHeader("Authorization", "Bearer token123")
```

## Custom Logging

```kotlin
import com.example.network.NetworkLogger

class MyLogger : NetworkLogger {
    override fun log(message: String) {
        println("[NET] $message")
    }
    
    override fun error(message: String, exception: Throwable?) {
        println("[ERR] $message")
        exception?.printStackTrace()
    }
}

initializeNetworkSDK(
    baseUrl = "https://api.example.com",
    logger = MyLogger()
)
```

## Compose Integration

```kotlin
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@Composable
fun PostsScreen() {
    var posts by remember { mutableStateOf(emptyList<Post>()) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            loading = true
            try {
                val response = PostsAPI.getPosts()
                posts = response.data
            } finally {
                loading = false
            }
        }
    }) {
        Text("Load Posts")
    }

    if (loading) CircularProgressIndicator()
    
    LazyColumn {
        items(posts) { post ->
            Text(post.title)
        }
    }
}
```

## ViewModel Pattern

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PostsViewModel : ViewModel() {
    private val _posts = mutableStateOf<List<Post>>(emptyList())
    val posts: State<List<Post>> = _posts

    fun loadPosts() {
        viewModelScope.launch {
            try {
                val response = PostsAPI.getPosts()
                _posts.value = response.data
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

## Testing with Mock Client

```kotlin
import com.example.network.HttpClient
import com.example.network.NetworkSDK

class MockHttpClient : HttpClient {
    override suspend fun request<T>(request, deserializer) =
        HttpResponse(deserializer("mock"), 200)
}

@Test
fun testAPI() = runBlocking {
    NetworkSDK.setHttpClient(MockHttpClient())
    val response = PostsAPI.getPosts()
    assert(response.statusCode == 200)
}
```

## Build Commands

```bash
# Build network library
./gradlew.bat :network:build

# Run tests
./gradlew.bat :network:allTests

# Publish locally
./gradlew.bat :network:publishToMavenLocal

# Publish to Artifactory
./gradlew.bat :network:publish

# Build demo app
./gradlew.bat :composeApp:build
```

## SDK Methods

```kotlin
// Initialize
initializeNetworkSDK(baseUrl, headers?, logger?)

// Make requests
NetworkSDK.get(path, headers?, deserializer)
NetworkSDK.post(path, body, headers?, deserializer)
NetworkSDK.put(path, body, headers?, deserializer)
NetworkSDK.delete(path, headers?, deserializer)

// Configuration
NetworkSDK.addDefaultHeader(key, value)
NetworkSDK.getDefaultHeaders()
NetworkSDK.getBaseUrl()
NetworkSDK.buildUrl(path)
NetworkSDK.setHttpClient(client)
NetworkSDK.reset()
```

## Use Published Library

```kotlin
repositories {
    mavenLocal()  // or mavenCentral()
}

dependencies {
    implementation("com.example:network-sdk:1.0.0")
}
```

## Platform Implementations

| Platform | HTTP Client | File |
|---|---|---|
| Android | OkHttp3 | `network/src/androidMain/.../PlatformHttpClient.kt` |
| iOS | NSURLSession | `network/src/iosMain/.../PlatformHttpClient.kt` |
| Web (JS) | Fetch API | `network/src/jsMain/.../PlatformHttpClient.kt` |
| Web (Wasm) | Fetch API | `network/src/wasmJsMain/.../PlatformHttpClient.kt` |

## Documentation

- **README.md** - Complete API reference
- **PUBLISHING.md** - Deployment guide
- **EXAMPLES.md** - Code patterns & best practices
- **AGENTS.md** - Developer guidelines

---

Quick links:
- Demo App: `/composeApp/src/commonMain/kotlin/com/example/kmpsample/NetworkDemoScreen.kt`
- API Endpoints: `/network/src/commonMain/kotlin/com/example/network/api/APIs.kt`
- Models: `/network/src/commonMain/kotlin/com/example/network/api/Models.kt`

