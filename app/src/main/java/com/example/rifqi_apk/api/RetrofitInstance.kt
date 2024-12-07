package com.example.rifqi_apk.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Base URL dari API
    private const val BASE_URL = "https://ppbo-api.vercel.app"

    // Logging interceptor untuk memantau request/response
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Menampilkan body dari request/response
        }
    }

    // OkHttpClient dengan interceptor
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // Fungsi untuk mendapatkan Retrofit instance
    fun getInstance(): Retrofit = retrofit

    // ApiService singleton untuk akses langsung
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
