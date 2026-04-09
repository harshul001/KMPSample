# Network SDK - Architecture & Design

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Compose Multiplatform App                  │
│  (composeApp/src/commonMain/NetworkDemoScreen.kt)       │
└──────────────┬──────────────────────────────────────────┘
               │ calls
               ▼
┌─────────────────────────────────────────────────────────┐
│          API Layer (PostsAPI, UsersAPI, ...)            │
│  (network/src/commonMain/kotlin/com/example/network/api)│
└──────────────┬──────────────────────────────────────────┘
               │ uses
               ▼
┌─────────────────────────────────────────────────────────┐
│           NetworkSDK (Singleton Manager)                │
│  (network/src/commonMain/NetworkSDK.kt)                 │
│  - Configurable base URL                                │
│  - Default headers                                      │
│  - Logging                                              │
└──────────────┬──────────────────────────────────────────┘
               │ delegates to
               ▼
┌─────────────────────────────────────────────────────────┐
│          HttpClient Interface (expect/actual)           │
│  (network/src/commonMain/HttpClient.kt)                 │
│  - get(), post(), put(), delete()                       │
└─────────┬──────────────┬─────────────┬──────────────────┘
          │              │             │
          │ Android      │ iOS         │ Web
          ▼              ▼             ▼
    ┌──────────┐  ┌────────────┐  ┌──────────┐
    │ OkHttp3  │  │NSURLSession│  │Fetch API │
    └──────────┘  └────────────┘  └──────────┘
```

## Module Dependencies

```
composeApp
  └── network (HTTP SDK library)
        ├── commonMain
        │   ├── HttpClient (interface)
        │   ├── NetworkSDK (manager)
        │   ├── Models (@Serializable)
        │   └── APIs (PostsAPI, UsersAPI, CommentsAPI)
        ├── androidMain
        │   └── OkHttp implementation
        ├── iosMain
        │   └── NSURLSession implementation
        ├── jsMain
        │   └── Fetch API implementation
        ├── wasmJsMain
        │   └── Fetch API implementation
        └── commonTest
            └── Mock implementation for testing
```

## Data Flow: GET Request Example

```
User clicks "Load Posts"
        │
        ▼
NetworkDemoScreen.kt (Compose UI)
        │
        ├─ coroutineScope.launch
        │
        ▼
PostsAPI.getPosts()
        │
        ├─ NetworkSDK.get("/posts")
        │
        ▼
NetworkSDK (Singleton)
        │
        ├─ Merge headers (default + custom)
        ├─ Build full URL
        ├─ Log request
        │
        ▼
HttpClient.get() ◄──── expect/actual selection happens
        │
        ├─ For Android: AndroidHttpClient (OkHttp)
        ├─ For iOS:    IosHttpClient (NSURLSession)
        ├─ For Web:    JsHttpClient (Fetch API)
        │
        ▼
HTTP Request to https://jsonplaceholder.typicode.com/posts
        │
        ▼
HTTP Response
        │
        ├─ Parse body JSON string
        ├─ Deserialize to List<Post>
        ├─ Wrap in HttpResponse<List<Post>>
        │
        ▼
Return to PostsAPI.getPosts()
        │
        ▼
Return to NetworkDemoScreen
        │
        ├─ Update UI state
        ├─ Display posts in LazyColumn
        │
        ▼
User sees posts on screen
```

## Exception Flow

```
Network Error
        │
        ├─ No internet
        ├─ Invalid URL
        ├─ 404 Not Found
        ├─ 500 Server Error
        │
        ▼
Platform HTTP Client catches
        │
        ▼
Throws NetworkException
  │
  ├─ message: Error description
  ├─ code: HTTP status code
  ├─ cause: Original exception
        │
        ▼
Caller catches NetworkException
        │
        ├─ Log error
        ├─ Update UI
        ├─ Show error message to user
```

## Platform-Specific Implementation Pattern

### Common (expect)
```kotlin
// network/src/commonMain/PlatformHttpClient.kt
expect fun createHttpClient(logger: NetworkLogger): HttpClient
```

### Android (actual)
```kotlin
// network/src/androidMain/PlatformHttpClient.kt
actual fun createHttpClient(logger: NetworkLogger): HttpClient {
    return AndroidHttpClient(logger)  // Uses OkHttp
}
```

### iOS (actual)
```kotlin
// network/src/iosMain/PlatformHttpClient.kt
actual fun createHttpClient(logger: NetworkLogger): HttpClient {
    return IosHttpClient(logger)  // Uses NSURLSession
}
```

### Web (actual)
```kotlin
// network/src/jsMain/PlatformHttpClient.kt
actual fun createHttpClient(logger: NetworkLogger): HttpClient {
    return JsHttpClient(logger)  // Uses Fetch API
}
```

## Class Hierarchy

```
HttpClient (interface)
  ├── AndroidHttpClient (Android implementation)
  ├── IosHttpClient (iOS implementation)
  ├── JsHttpClient (Web/JS implementation)
  ├── WasmJsHttpClient (Web/Wasm implementation)
  └── MockHttpClient (For testing)

HttpRequest (data class)
  ├── url: String
  ├── method: HttpMethod
  ├── headers: Map<String, String>
  └── body: String?

HttpResponse<T> (data class)
  ├── data: T
  ├── statusCode: Int
  └── headers: Map<String, String>

NetworkException (exception)
  ├── message: String
  ├── code: Int?
  └── cause: Throwable?

NetworkLogger (interface)
  ├── log(message: String)
  └── error(message: String, exception: Throwable?)

PostsAPI (object)
  ├── getPosts(): HttpResponse<List<Post>>
  ├── getPostById(id): HttpResponse<Post>
  ├── getPostsByUserId(userId): HttpResponse<List<Post>>
  ├── createPost(...): HttpResponse<Post>
  ├── updatePost(...): HttpResponse<Post>
  └── deletePost(id): HttpResponse<String>
```

## Configuration & Initialization Flow

```
Application Start
        │
        ▼
MainActivity.onCreate() or main()
        │
        ▼
initializeNetworkSDK(
    baseUrl = "...",
    defaultHeaders = {...},
    logger = DefaultNetworkLogger()
)
        │
        ├─ createHttpClient() [platform-specific]
        │  │
        │  ├─ Android: new AndroidHttpClient()
        │  ├─ iOS: new IosHttpClient()
        │  └─ Web: new JsHttpClient()
        │
        ├─ NetworkSDK.setHttpClient(client)
        ├─ NetworkSDK.initialize(baseUrl, headers, logger)
        │
        ▼
SDK ready for API calls
```

## Header Merging Strategy

```
Default Headers (Global)
    │
    ├─ "Accept": "application/json"
    ├─ "User-Agent": "MyApp/1.0"
    │
    └─ Per-Request Headers
        │
        ├─ "Authorization": "Bearer ..."
        ├─ "X-Custom": "value"
        │
        ▼
    Merged Map (Request Headers override Defaults)
        │
        ├─ "Accept": "application/json" (from default)
        ├─ "User-Agent": "MyApp/1.0" (from default)
        ├─ "Authorization": "Bearer ..." (from request)
        ├─ "X-Custom": "value" (from request)
```

## Serialization Architecture

```
JSON String (from HTTP response)
        │
        ▼
deserializer: (String) -> T
        │
        ├─ Parse JSON
        ├─ Validate structure
        ├─ Map to Kotlin data class
        │
        ▼
Type-Safe Kotlin Object (Post, User, etc.)
        │
        ▼
Return in HttpResponse<T>
```

## Testing Architecture

```
Unit Tests (commonTest)
        │
        ├─ MockHttpClient (implements HttpClient)
        │  │
        │  └─ Predefined responses
        │
        ├─ NetworkSDKTest.kt
        │  ├─ Test initialization
        │  ├─ Test URL building
        │  ├─ Test header management
        │  └─ Test mock client integration
        │
        └─ Integration tests (in each platform)
```

## Publishing & Distribution Flow

```
Source Code (network module)
        │
        ├─ ./gradlew :network:build
        │
        ▼
Compiled JAR/Framework
        │
        ├─ ./gradlew :network:publishToMavenLocal
        │
        ▼
Maven Local Repository (~/.m2/repository)
        │
        └─ com/example/network-sdk/1.0.0/
            ├─ network-sdk-1.0.0.jar
            ├─ network-sdk-1.0.0.pom
            └─ network-sdk-1.0.0-sources.jar

Alternative Targets:
├─ JFrog Artifactory (./gradlew :network:publish)
├─ Maven Central (requires GPG signing)
└─ GitHub Packages (with GH credentials)
```

## Deployment to Other Projects

```
Other Project (build.gradle.kts)
        │
        ├─ repositories {
        │  └─ mavenLocal()  // or mavenCentral()
        │  }
        │
        ├─ dependencies {
        │  └─ implementation("com.example:network-sdk:1.0.0")
        │  }
        │
        ▼
Gradle resolves from Maven repository
        │
        ▼
Downloads network-sdk-1.0.0.jar
        │
        ▼
Includes in classpath
        │
        ▼
Project can use:
        ├─ initializeNetworkSDK(...)
        ├─ PostsAPI.getPosts()
        ├─ NetworkSDK.get/post/put/delete()
        └─ Custom APIs built on NetworkSDK
```

## Error Handling Architecture

```
HTTP Request
        │
        ├─ Success (200-299)
        │  │
        │  └─ Parse & Return HttpResponse<T>
        │
        └─ Failure (400-599, timeout, etc.)
           │
           ├─ Platform HTTP Client catches
           │
           ├─ Logs error via NetworkLogger
           │
           └─ Throws NetworkException
              │
              ├─ code: HTTP status
              ├─ message: Description
              ├─ cause: Original exception
              │
              ▼
           Caller catches NetworkException
              │
              ├─ e.code == 404? Not found
              ├─ e.code == 500? Server error
              ├─ e.code == null? Network error
              │
              └─ Display to user / Retry / Handle
```

## Thread/Coroutine Model

```
UI Thread / Coroutine
        │
        ├─ User action
        │
        ▼
coroutineScope.launch
        │
        ├─ Dispatchers.Main (default)
        │
        ▼
suspend fun getPosts()
        │
        ├─ Switches to IO context (behind scenes)
        │
        ▼
Platform HTTP Client (async)
        │
        ├─ Android: OkHttp (blocking, dispatched to IO)
        ├─ iOS: NSURLSession (synchronous wrapper)
        ├─ Web: Fetch API (native async)
        │
        ▼
HTTP Request
        │
        ▼
Response
        │
        ├─ Switches back to Main dispatcher
        │
        ▼
Update UI state
        │
        ▼
Recompose & display
```

---

**Key Design Principles:**
- ✅ **Separation of Concerns**: API logic vs. Transport layer
- ✅ **Platform Abstraction**: Same API across all platforms
- ✅ **Type Safety**: Kotlinx Serialization for model mapping
- ✅ **Error Handling**: Unified exception model
- ✅ **Testability**: Mock implementations included
- ✅ **Extensibility**: Easy to add new API endpoints or logging

