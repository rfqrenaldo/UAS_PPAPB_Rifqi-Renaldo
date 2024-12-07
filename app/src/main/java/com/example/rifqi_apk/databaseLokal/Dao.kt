package com.example.rifqi_apk.databaseLokal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    fun registerUser(user: User)

    @Query("SELECT * FROM User WHERE username = :username AND password = :password")
    fun loginUser(username: String, password: String): User?
}

@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)  // Menghindari duplikasi ID
    fun addToCart(cart: Cart)

    @Update  // Fungsi untuk mengupdate item
    fun updateCart(cart: Cart)

    @Delete
    fun delete(cart: Cart)

    @Query("SELECT * FROM Cart")
    fun getAllCartItems(): List<Cart>

    @Query("DELETE FROM Cart")
    suspend fun deleteAllCartItems()
}

