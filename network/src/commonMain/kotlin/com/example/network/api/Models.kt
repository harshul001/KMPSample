package com.example.network.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * JSON Placeholder API models
 */

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

/**
 * JSON serializer utility
 */
object JsonSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun <T> serialize(data: T): String = json.encodeToString(data)

    inline fun <reified T> deserialize(data: String): T = json.decodeFromString(data)
}

