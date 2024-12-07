package com.example.rifqi_apk.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.example.rifqi_apk.api.RetrofitInstance
import com.example.rifqi_apk.databaseLokal.AppDatabase
import com.example.rifqi_apk.databaseLokal.Cart
import com.example.rifqi_apk.databinding.ActivityMainBinding
import com.example.rifqi_apk.model.Barang
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var barangAdapter: BarangAdapter
    private lateinit var appDatabase: AppDatabase

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Inisialisasi RecyclerView
        barangAdapter = BarangAdapter { barang, jumlah ->
            addToCart(barang, jumlah)
        }
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2) // Tampilan 2 kolom
        binding.rvProducts.adapter = barangAdapter

        // Inisialisasi Room Database
        appDatabase = AppDatabase.getInstance(this)

        // Logging URL API yang akan dipanggil
        Log.d(TAG, "Request URL: ${RetrofitInstance.getInstance().baseUrl()}barang")

        fetchBarangFromApi()

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.remove("username")
            editor.apply()

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //tombol daftar keranjang
        binding.btnCart.setOnClickListener{
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchBarangFromApi() {
        val call = RetrofitInstance.api.getBarang()

        call.enqueue(object : Callback<List<Barang>> {
            override fun onResponse(call: Call<List<Barang>>, response: Response<List<Barang>>) {
                if (response.isSuccessful) {
                    Log.d("Retrofit", "Response successful: ${response.body()}")

                    // Dapatkan data barang dari response
                    val barangList = response.body() ?: emptyList() // Pastikan data tidak null

                    // Set data ke adapter
                    barangAdapter.setBarangList(barangList)
                } else {
                    Log.e("Retrofit", "Response error: Code ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Barang>>, t: Throwable) {
                Log.e("Retrofit", "API call failed: ${t.message}")
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addToCart(barang: Barang, jumlah: Int) {
        val totalHarga = barang.harga * jumlah
        val cartItem = Cart(
            namaBarang = barang.namaBarang,
            jumlah = jumlah,
            harga = barang.harga,
            totalHarga = totalHarga
        )

        // Menambahkan item ke keranjang menggunakan Room
        Thread {
            appDatabase.cartDao().addToCart(cartItem)
            runOnUiThread {
                Toast.makeText(this, "Item ${barang.namaBarang} added to cart", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}
