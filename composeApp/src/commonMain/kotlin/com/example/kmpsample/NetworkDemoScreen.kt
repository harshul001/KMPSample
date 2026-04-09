package com.example.kmpsample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.network.api.PostsAPI
import com.example.network.api.Post
import com.example.network.initializeNetworkSDK
import kotlinx.coroutines.launch

@Composable
fun NetworkDemoScreen(
    viewModel: NetworkDemoViewModel = viewModel()
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Initialize the network SDK on first composition
        if (!viewModel.isInitialized) {
            initializeNetworkSDK(
                baseUrl = "https://jsonplaceholder.typicode.com",
                defaultHeaders = mapOf("Accept" to "application/json")
            )
            viewModel.isInitialized = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Network SDK Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Fetch Posts Button
        Button(
            onClick = {
                coroutineScope.launch {
                    loading = true
                    error = null
                    try {
                        val response = PostsAPI.getPosts()
                        posts = response.data.take(5) // Show first 5 posts
                    } catch (e: Exception) {
                        error = e.message ?: "Unknown error occurred"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !loading
        ) {
            Text("Fetch Posts (GET)")
        }

        // Fetch Single Post Button
        Button(
            onClick = {
                coroutineScope.launch {
                    loading = true
                    error = null
                    try {
                        val response = PostsAPI.getPostById(1)
                        posts = listOf(response.data)
                    } catch (e: Exception) {
                        error = e.message ?: "Unknown error occurred"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !loading
        ) {
            Text("Fetch Post #1 (GET by ID)")
        }

        // Create Post Button
        Button(
            onClick = {
                coroutineScope.launch {
                    loading = true
                    error = null
                    try {
                        val response = PostsAPI.createPost(
                            userId = 1,
                            title = "Test Post from KMP App",
                            body = "This is a test post created via the Network SDK"
                        )
                        posts = listOf(response.data)
                    } catch (e: Exception) {
                        error = e.message ?: "Unknown error occurred"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !loading
        ) {
            Text("Create Post (POST)")
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }

        if (error != null) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        if (posts.isNotEmpty()) {
            Text(
                "Results (${posts.size} posts)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            posts.forEach { post ->
                PostCard(post)
            }
        }
    }
}

@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ID: ${post.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

