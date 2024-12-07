package com.example.rifqi_apk.databaseLokal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Cart(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaBarang: String,
    val jumlah: Int,
    val harga: Int,
    val totalHarga: Int
)
