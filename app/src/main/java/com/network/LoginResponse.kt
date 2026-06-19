package com.angels.notes.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("token")
    val token: String? = null
)