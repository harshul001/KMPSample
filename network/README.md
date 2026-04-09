# Network SDK - Kotlin Multiplatform Library

A cross-platform HTTP network SDK for Kotlin Multiplatform projects, providing a unified API for Android, iOS, and Web (JS/Wasm).

## Features

- ✅ **Unified API** across all platforms (Android, iOS, Web)
- ✅ **Platform-specific implementations** (OkHttp for Android, NSURLSession for iOS, Fetch API for Web)
- ✅ **Common HTTP methods**: GET, POST, PUT, DELETE, PATCH
- ✅ **Configurable base URL and headers**
- ✅ **Built-in error handling and logging**
- ✅ **JSON serialization support** (kotlinx-serialization)
- ✅ **Sample APIs** for JSON Placeholder testing
- ✅ **Maven/JFrog publishing support**

## Architecture

The library is structured as a Kotlin Multiplatform module with the following source sets:

```
network/
├── src/
│   ├── commonMain/        # Shared interfaces and logic
│   │   ├── HttpClient.kt       # HTTP client interface
│   │   ├── NetworkSDK.kt       # Main SDK manager
│   │   ├── PlatformHttpClient.kt   # Platform factory (expect/actual)
│   │   └── api/
│   │       ├── Models.kt       # Data classes (Post, User, Comment)
│   │       └── APIs.kt         # API endpoints (PostsAPI, UsersAPI, CommentsAPI)
│   ├── androidMain/
│   │   └── PlatformHttpClient.kt   # OkHttp implementation
│   ├── iosMain/
│   │   └── PlatformHttpClient.kt   # NSURLSession implementation
│   ├── jsMain/
│   │   └── PlatformHttpClient.kt   # Fetch API implementation
│   ├── wasmJsMain/
│   │   └── PlatformHttpClient.kt   # Fetch API implementation for Wasm
│   └── commonTest/
│       └── NetworkSDKTest.kt      # Common tests
```

## Installation

### As a dependency in your project

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":network"))
}
```

### Publishing to Maven

The library supports publishing to Maven Central or JFrog Artifactory.

#### Publish locally:

```bash
./gradlew :network:publishToMavenLocal
```

#### Publish to JFrog Artifactory:

1. Configure your credentials in `gradle.properties`:

```properties
artifactoryUsername=your_username
artifactoryPassword=your_api_key
```

2. Update the Artifactory URL in `network/build.gradle.kts`

3. Run:

```bash
./gradlew :network:publish
```

## Usage

### 1. Initialize the SDK

```kotlin
import com.example.network.initializeNetworkSDK

// In your app initialization code
initializeNetworkSDK(
    baseUrl = "https://jsonplaceholder.typicode.com",
    defaultHeaders = mapOf(
        "Accept" to "application/json",
        "User-Agent" to "MyApp/1.0"
    )
)
```

### 2. Make API Calls

#### GET Request (Fetch Posts)

```kotlin
import com.example.network.api.PostsAPI

val response = PostsAPI.getPosts()
println("Got ${response.data.size} posts")
println("Status: ${response.statusCode}")

response.data.forEach { post ->
    println("${post.id}: ${post.title}")
}
```

#### GET Request by ID

```kotlin
val response = PostsAPI.getPostById(id = 1)
println("Post: ${response.data.title}")
println("Body: ${response.data.body}")
```

#### POST Request (Create Post)

```kotlin
val response = PostsAPI.createPost(
    userId = 1,
    title = "My New Post",
    body = "This is the post body"
)
println("Created post with ID: ${response.data.id}")
```

#### PUT Request (Update Post)

```kotlin
val response = PostsAPI.updatePost(
    id = 1,
    userId = 1,
    title = "Updated Title",
    body = "Updated body content"
)
println("Updated post: ${response.data.title}")
```

#### DELETE Request

```kotlin
val response = PostsAPI.deletePost(id = 1)
println("Deleted. Status: ${response.statusCode}")
```

### 3. Custom Logging

```kotlin
import com.example.network.NetworkLogger
import com.example.network.NetworkSDK

class CustomLogger : NetworkLogger {
    override fun log(message: String) {
        println("[CUSTOM LOG] $message")
    }

    override fun error(message: String, exception: Throwable?) {
        println("[CUSTOM ERROR] $message")
        exception?.printStackTrace()
    }
}

NetworkSDK.initialize(
    baseUrl = "https://api.example.com",
    logger = CustomLogger()
)
```

### 4. Custom HTTP Client (Advanced)

```kotlin
import com.example.network.HttpClient
import com.example.network.NetworkSDK

class MyCustomHttpClient : HttpClient {
    override suspend fun request<T>(request, deserializer): HttpResponse<T> {
        // Your custom implementation
    }
}

NetworkSDK.setHttpClient(MyCustomHttpClient())
```

## API Reference

### Core Classes

#### `NetworkSDK` - Main entry point

```kotlin
NetworkSDK.initialize(baseUrl, defaultHeaders, logger)
NetworkSDK.get(path, headers, deserializer)
NetworkSDK.post(path, body, headers, deserializer)
NetworkSDK.put(path, body, headers, deserializer)
NetworkSDK.delete(path, headers, deserializer)
NetworkSDK.addDefaultHeader(key, value)
NetworkSDK.getDefaultHeaders()
NetworkSDK.buildUrl(path)
NetworkSDK.reset()
```

#### `HttpClient` - HTTP client interface

```kotlin
interface HttpClient {
    suspend fun request<T>(request: HttpRequest, deserializer: (String) -> T): HttpResponse<T>
    suspend fun get<T>(url: String, headers, deserializer): HttpResponse<T>
    suspend fun post<T>(url: String, body: String, headers, deserializer): HttpResponse<T>
    suspend fun put<T>(url: String, body: String, headers, deserializer): HttpResponse<T>
    suspend fun delete<T>(url: String, headers, deserializer): HttpResponse<T>
}
```

#### `HttpResponse<T>` - Response wrapper

```kotlin
data class HttpResponse<T>(
    val data: T,
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap()
)
```

#### `NetworkException` - Error handling

```kotlin
class NetworkException(
    message: String,
    val code: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)
```

### API Endpoints

#### `PostsAPI`
- `getPosts()` - Get all posts
- `getPostById(id)` - Get a single post
- `getPostsByUserId(userId)` - Get posts by user
- `createPost(userId, title, body)` - Create a new post
- `updatePost(id, userId, title, body)` - Update a post
- `deletePost(id)` - Delete a post

#### `UsersAPI`
- `getUsers()` - Get all users
- `getUserById(id)` - Get a single user

#### `CommentsAPI`
- `getCommentsByPostId(postId)` - Get comments for a post
- `getCommentById(id)` - Get a single comment

## Testing

Run common tests:

```bash
./gradlew :network:commonTest
```

Run all tests:

```bash
./gradlew :network:allTests
```

## Platform-Specific Details

### Android
- Uses OkHttp3 for HTTP requests
- Supports HTTP logging interceptor
- 30-second timeout for all operations

### iOS
- Uses native NSURLSession
- Supports iOS platform SDK methods
- Synchronous wrapper for coroutine compatibility

### Web (JS/Wasm)
- Uses Fetch API
- Browser-compatible
- Supports CORS-enabled endpoints

## Building the Library

```bash
# Build all targets
./gradlew :network:build

# Build specific target
./gradlew :network:assembleDebug                 # Android
./gradlew :network:linkDebugFrameworkIos         # iOS
./gradlew :network:browserDevelopmentWebpack     # Web
./gradlew :network:wasmJsBrowserDevelopmentRun   # Wasm

# Build and publish
./gradlew :network:publishToMavenLocal
```

## Troubleshooting

### "HTTP client not initialized" error
Make sure to call `initializeNetworkSDK()` or `NetworkSDK.setHttpClient()` before making any requests.

### CORS errors (Web)
Ensure the API endpoint supports CORS and has the appropriate headers.

### SSL/TLS errors (Android)
Verify that your Android app has internet permissions in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### iOS framework not found
Run `./gradlew :network:embedAndSignAppleFrameworkForXcode` to regenerate the framework.

## License

MIT License

## Contributing

Contributions are welcome! Please follow the coding standards and add tests for new features.

## Support

For issues or questions, please check the project's AGENTS.md for development guidelines.

