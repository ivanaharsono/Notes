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

// 📦 Model Data Paket Kiriman (Request) & Terima (Response)
data class AuthRequest(
    val email: String,
    val javaPassword: String // Sesuaikan nama variabelnya dengan skema backend/database lu
)

data class AuthRequest(
    val fullName: String? = null, // Tambahin baris ini
    val email: String,
    val javaPassword: String
)

data class FeedbackRequest(
    val email: String,
    val message: String
)

data class FeedbackResponse(
    val status: String,
    val message: String
)