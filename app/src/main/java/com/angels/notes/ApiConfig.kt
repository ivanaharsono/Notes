package com.angels.notes

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {

    // =====================================================================
    // 👇👇👇  GANTI BASE_URL DI BARIS INI  👇👇👇
    //
    // Testing pakai EMULATOR + backend jalan LOKAL di PC (uvicorn port 7860):
    private const val BASE_URL = "https://notes-backend-rust-five.vercel.app/"
    //
    // Kalau sudah deploy ke Hugging Face / pakai HP asli, ganti jadi:
    // private const val BASE_URL = "nhttps://notes-backend-rust-five.vercel.app/"
    //
    // CATATAN: 10.0.2.2 itu alamat khusus emulator untuk "localhost" PC kamu.
    // Karena pakai http:// (bukan https), WAJIB aktifkan cleartext traffic
    // di AndroidManifest.xml (lihat instruksi di chat).
    // =====================================================================

    fun getApiService(): ApiService {
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)   // kadang "tidur", kasih waktu
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}