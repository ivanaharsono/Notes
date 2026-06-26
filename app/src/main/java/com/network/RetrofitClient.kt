package com.angels.notes.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // KONEKSI UTAMA: Mengarahkan Android lu ke server Hugging Face
    private const val BASE_URL = "https://notes-backend-rust-five.vercel.app/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}