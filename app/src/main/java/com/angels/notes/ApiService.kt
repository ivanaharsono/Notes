package com.angels.notes

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("signup")
    suspend fun registerUser(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("login")
    suspend fun loginUser(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("feedback")
    suspend fun sendFeedback(
        @Body request: FeedbackRequest
    ): FeedbackResponse
}

data class AuthRequest(
    val fullName: String? = null,
    val email: String,
    val javaPassword: String
)

data class AuthResponse(
    val status: String,
    val message: String,
    val token: String? = null
)

data class FeedbackRequest(
    val email: String,
    val message: String
)

data class FeedbackResponse(
    val status: String,
    val message: String
)