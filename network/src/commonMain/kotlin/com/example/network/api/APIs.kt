package com.example.network.api

import com.example.network.NetworkSDK
import com.example.network.HttpResponse

/**
 * API endpoint manager using JSON Placeholder (https://jsonplaceholder.typicode.com)
 */
object PostsAPI {
    private const val POSTS_PATH = "/posts"

    /**
     * Fetch all posts
     */
    suspend fun getPosts(): HttpResponse<List<Post>> {
        val json = JsonSerializer
        return NetworkSDK.get(
            path = POSTS_PATH,
            deserializer = { responseBody ->
                json.deserialize<List<Post>>(responseBody)
            }
        )
    }

    /**
     * Fetch a single post by ID
     */
    suspend fun getPostById(id: Int): HttpResponse<Post> {
        val json = JsonSerializer
        return NetworkSDK.get(
            path = "$POSTS_PATH/$id",
            deserializer = { responseBody ->
                json.deserialize<Post>(responseBody)
            }
        )
    }

    /**
     * Fetch posts by user ID
     */
    suspend fun getPostsByUserId(userId: Int): HttpResponse<List<Post>> {
        val json = JsonSerializer
        return NetworkSDK.get(
            path = "$POSTS_PATH?userId=$userId",
            deserializer = { responseBody ->
                json.deserialize<List<Post>>(responseBody)
            }
        )
    }

    /**
     * Create a new post
     */
    suspend fun createPost(
        userId: Int,
        title: String,
        body: String
    ): HttpResponse<Post> {
        val json = JsonSerializer
        val newPost = Post(userId = userId, id = 0, title = title, body = body)
        val requestBody = json.serialize(newPost)

        return NetworkSDK.post(
            path = POSTS_PATH,
            body = requestBody,
            headers = mapOf("Content-Type" to "application/json"),
            deserializer = { responseBody ->
                json.deserialize<Post>(responseBody)
            }
        )
    }

    /**
     * Update an existing post
     */
    suspend fun updatePost(
        id: Int,
        userId: Int,
        title: String,
        body: String
    ): HttpResponse<Post> {
        val json = JsonSerializer
        val updatedPost = Post(userId = userId, id = id, title = title, body = body)
        val requestBody = json.serialize(updatedPost)

        return NetworkSDK.put(
            path = "$POSTS_PATH/$id",
            body = requestBody,
            headers = mapOf("Content-Type" to "application/json"),
            deserializer = { responseBody ->
                json.deserialize<Post>(responseBody)
            }
        )
    }

    /**
     * Delete a post
     */
    suspend fun deletePost(id: Int): HttpResponse<String> {
        return NetworkSDK.delete(
            path = "$POSTS_PATH/$id",
            deserializer = { responseBody ->
                responseBody
            }
        )
    }
}

/**
 * Users API endpoint manager
 */
object UsersAPI {
    private const val USERS_PATH = "/users"

    /**
     * Fetch all users
     */
    suspend fun getUsers(): HttpResponse<List<User>> {
        val json = JsonSerializer
        return NetworkSDK.get(
            path = USERS_PATH,
            deserializer = { responseBody ->
                json.deserialize<List<User>>(responseBody)
            }
        )
    }

    /**
     * Fetch a single user by ID
     */
    suspend fun getUserById(id: Int): HttpResponse<User> {
        val json = JsonSerializer
        return NetworkSDK.get(
            path = "$USERS_PATH/$id",
            deserializer = { responseBody ->
                json.deserialize<User>(responseBody)
            }
        )
    }
}

/**
 * Comments API endpoint manager
 */
object CommentsAPI {
    private const val COMMENTS_PATH = "/comments"

    /**
     * Fetch comments for a post
     */
    suspend fun getCommentsByPostId(postId: Int): HttpResponse<List<Comment>> {
        val json = JsonSerializer
        return NetworkSDK.get(
            path = "$COMMENTS_PATH?postId=$postId",
            deserializer = { responseBody ->
                json.deserialize<List<Comment>>(responseBody)
            }
        )
    }

    /**
     * Fetch a single comment by ID
     */
    suspend fun getCommentById(id: Int): HttpResponse<Comment> {
        val json = JsonSerializer
        return NetworkSDK.get(
            path = "$COMMENTS_PATH/$id",
            deserializer = { responseBody ->
                json.deserialize<Comment>(responseBody)
            }
        )
    }
}

