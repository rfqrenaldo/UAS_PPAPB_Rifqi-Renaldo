package com.example.rifqi_apk.databaseLokal

import androidx.room.Entity
import androidx.room.PrimaryKey
//untuk menyimpan data user :tabel user
@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String

)
