package com.example.rifqi_apk.databaseLokal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//di file ini kita membuat database dengan room bernama aapp_database
//kita mendefinisikan 2 tabel yaitu user dan cart

@Database(entities = [User::class, Cart::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    //di bawah ini adalah akses atau fungsi" yang ada di dao
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" //inisialisasi nama database
                ).build()
                INSTANCE = instance
                instance  //memastikan hanya 1 instance database aktif dengan singleton
            }
        }
    }
}