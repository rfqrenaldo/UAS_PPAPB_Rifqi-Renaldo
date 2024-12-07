package com.example.rifqi_apk.api



import com.example.rifqi_apk.model.Barang
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("JI119/barang") // Disesuaikan dengan struktur API
    fun getBarang(): Call<List<Barang>>
}
