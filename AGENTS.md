# AGENTS Guide for KMPSample

## Big Picture
- This is a Kotlin Multiplatform + Compose Multiplatform app with two Gradle modules: `:composeApp` (main app) and `:network` (reusable HTTP SDK).
- Shared UI lives in `composeApp/src/commonMain/kotlin/com/example/kmpsample/App.kt` and is used by Android, iOS, JS, and Wasm.
- Platform-specific behavior is intentionally tiny and routed through `expect/actual` (`Platform.kt` + `Platform.*.kt`).
- iOS has a separate Swift host app in `iosApp/` that embeds the Kotlin framework and shows Compose in SwiftUI (`ContentView.swift`).
- The `:network` module is a publishable Kotlin Multiplatform library for HTTP networking across all platforms.

## Source Set Responsibilities

### composeApp
- `commonMain`: shared UI/state/domain code (example: `App()`, `Greeting`, `NetworkDemoScreen`).
- `androidMain`: Android entrypoint and platform actuals (`MainActivity.kt`, `Platform.android.kt`).
- `iosMain`: iOS Compose bridge and platform actuals (`MainViewController.kt`, `Platform.ios.kt`).
- `jsMain` and `wasmJsMain`: web platform labels (`Platform.js.kt`, `Platform.wasmJs.kt`).
- `webMain`: browser entrypoint/resources (`main.kt`, `resources/index.html`, `styles.css`).

### network (HTTP SDK Library)
- `commonMain`: HTTP interfaces, SDK manager, models, and sample APIs (`HttpClient.kt`, `NetworkSDK.kt`, `Models.kt`, `APIs.kt`).
- `androidMain`: OkHttp implementation (`PlatformHttpClient.kt`).
- `iosMain`: NSURLSession implementation (`PlatformHttpClient.kt`).
- `jsMain`: Fetch API implementation (`PlatformHttpClient.kt`).
- `wasmJsMain`: Fetch API implementation for Wasm (`PlatformHttpClient.kt`).
- `commonTest`: unit tests using mock clients (`NetworkSDKTest.kt`).

## Runtime Flow (Cross-Component)

### App Launch
1. Android: launcher activity in `AndroidManifest.xml` -> `MainActivity.setContent { App() }`.
2. iOS: SwiftUI `ContentView` -> `MainViewControllerKt.MainViewController()` -> `App()`.
3. Web: `main()` in `webMain` calls `ComposeViewport { App() }`.

### Network Flow
1. Call `initializeNetworkSDK(baseUrl, headers)` once during app init.
2. NetworkSDK instantiates platform-specific HTTP client (OkHttp, NSURLSession, Fetch API).
3. Call API methods (e.g., `PostsAPI.getPosts()`), which use `NetworkSDK.get()`.
4. Platform-specific client handles the request and returns `HttpResponse<T>`.

### Shared UI uses NetworkDemoScreen
- Tabs in `App()` show "Home" (original demo) and "Network Demo" (API calls).
- `NetworkDemoScreen.kt` demonstrates GET, POST, and DELETE operations to JSON Placeholder.

## Build and Run Workflows
- Android debug APK (Windows): `./gradlew.bat :composeApp:assembleDebug` (from `README.md`).
- Web (Wasm dev server): `./gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun`.
- Web (JS dev server): `./gradlew.bat :composeApp:jsBrowserDevelopmentRun`.
- iOS app run: open `iosApp/` in Xcode and run the `iosApp` target (`README.md`).
- iOS framework integration: `:composeApp:embedAndSignAppleFrameworkForXcode` (`iosApp.xcodeproj/project.pbxproj`).
- Common tests: `./gradlew.bat :composeApp:allTests` and `./gradlew.bat :network:allTests`.

## Publishing and Deployment

### Network SDK Publishing
- Local: `./gradlew.bat :network:publishToMavenLocal` -> `~/.m2/repository/com/example/network-sdk/1.0.0/`.
- JFrog Artifactory: Configure credentials in `gradle.properties`, then `./gradlew.bat :network:publish`.
- Maven Central: Requires GPG signing and Sonatype account.
- See `network/PUBLISHING.md` for full configuration and details.

### Using Published SDK
```kotlin
// In another project
repositories {
    mavenLocal() // or mavenCentral()
}
dependencies {
    implementation("com.example:network-sdk:1.0.0")
}
```

## Project Conventions to Follow

### Shared Code
- Keep platform divergence behind `expect/actual` rather than branching in shared UI.
- Add shared dependencies in respective `sourceSets`; use platform-specific only for platform APIs.

### Naming and Structure
- Version catalog is authoritative (`gradle/libs.versions.toml`); reference via `libs.*` aliases.
- Source set naming follows Kotlin Multiplatform conventions (`commonMain`, `androidMain`, `iosMain`, `jsMain`, `wasmJsMain`, `webMain`).

### HTTP Client Usage
- Always initialize `NetworkSDK` once per app startup via `initializeNetworkSDK(baseUrl, headers)`.
- Define platform-specific APIs in API objects (e.g., `PostsAPI`, `UsersAPI`) under `com.example.network.api`.
- Use `expect/actual` pattern for `createHttpClient()` to switch implementations per platform.
- Add custom logging by implementing `NetworkLogger` interface.

### Version Targets
- JVM target is Java 11 for Android/Kotlin compilation.
- iOS supports arm64 and simulator arm64.
- Web targets: JS and Wasm.

## Integration and Change Hotspots

### Adding New API Endpoints
1. Define models in `network/src/commonMain/kotlin/com/example/network/api/Models.kt` (using `@Serializable`).
2. Create API object (e.g., `ProductsAPI`) in `network/src/commonMain/kotlin/com/example/network/api/APIs.kt`.
3. Use `NetworkSDK.get/post/put/delete()` with `JsonSerializer.deserialize/serialize()`.

### Changing Platform Implementations
1. Edit platform-specific `PlatformHttpClient.kt` (Android uses OkHttp, iOS uses NSURLSession, Web uses Fetch API).
2. Ensure `createHttpClient()` factory function is implemented in each platform.
3. Test platform-specific features (Android: `okhttp3.OkHttpClient`, iOS: `NSURLSession`, Web: `window.fetch`).

### iOS Framework Integration
- If changing iOS framework name/static behavior, update `baseName = "NetworkSDK"` and validate Swift imports.

### Publishing Configuration
- Update version in `network/build.gradle.kts` before publishing.
- Configure repository credentials in `~/.gradle/gradle.properties` or environment variables.
- Reference `network/PUBLISHING.md` for repository-specific setup.

### Current Repo State
- No CI/lint config files exist; prefer local Gradle/Xcode verification.
- Network library is configured for multi-platform publishing (Maven Local, JFrog, Maven Central, GitHub Packages).
- Sample app integrates network library and demonstrates API usage with JSON Placeholder.

## Key Files and Their Roles

| File/Directory | Purpose |
|---|---|
| `network/build.gradle.kts` | Library build config, publishing setup, dependencies |
| `network/src/commonMain/kotlin/com/example/network/` | HTTP interfaces, SDK manager, data models |
| `network/src/androidMain/kotlin/com/example/network/PlatformHttpClient.kt` | OkHttp implementation |
| `network/src/iosMain/kotlin/com/example/network/PlatformHttpClient.kt` | NSURLSession implementation |
| `network/src/jsMain/kotlin/com/example/network/PlatformHttpClient.kt` | Fetch API implementation |
| `network/README.md` | SDK usage guide and API reference |
| `network/PUBLISHING.md` | Publishing to Maven/Artifactory guide |
| `network/EXAMPLES.md` | Code examples and best practices |
| `composeApp/src/commonMain/kotlin/com/example/kmpsample/NetworkDemoScreen.kt` | Integration example UI |
| `gradle/libs.versions.toml` | Dependency versions (OkHttp 4.11.0, kotlinx-serialization 1.6.2) |

