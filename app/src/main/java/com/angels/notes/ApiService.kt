package com.angels.notes

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("signup")
    suspend fun signup(@Body request: SignupRequest): MessageResponse

    @POST("verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): MessageResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): MessageResponse

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): MessageResponse

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): MessageResponse

    @POST("change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): MessageResponse

    @POST("feedback")
    suspend fun sendFeedback(@Body request: FeedbackRequest): MessageResponse
}

// =========================== REQUEST ===========================
// @SerializedName dipakai kalau nama field di JSON beda dengan nama
// variabel Kotlin. Backend butuh "full_name" (snake_case).
data class SignupRequest(
    @SerializedName("full_name") val fullName: String,
    val email: String,
    val javaPassword: String
)

data class LoginRequest(
    val email: String,
    val javaPassword: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

data class ChangePasswordRequest(
    val email: String,
    val oldPassword: String,
    val newPassword: String
)

data class FeedbackRequest(
    val email: String,
    val message: String,
    val name: String? = null
)

// =========================== RESPONSE ===========================
// Semua endpoint balikin format yang sama: { status, message }.
// token disiapkan kalau nanti backend pakai JWT.
data class MessageResponse(
    val status: String,
    val message: String,
    val token: String? = null
)