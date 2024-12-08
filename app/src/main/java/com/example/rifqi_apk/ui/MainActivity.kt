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

    private lateinit var binding: ActivityMainBinding  // Binding untuk mengakses elemen UI dari activity_main.xml
    private lateinit var sharedPreferences: SharedPreferences  // SharedPreferences untuk menyimpan status login
    private lateinit var barangAdapter: BarangAdapter  // Adapter untuk RecyclerView yang menampilkan daftar barang
    private lateinit var appDatabase: AppDatabase  // Database lokal untuk menyimpan data keranjang belanja

    companion object {
        private const val TAG = "MainActivity"  // TAG untuk logging
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)  // Meng-inflate layout menggunakan ViewBinding
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)  // Mengakses SharedPreferences untuk status login

        // Inisialisasi RecyclerView dengan adapter yang memiliki callback untuk menambahkan barang ke keranjang
        barangAdapter = BarangAdapter { barang, jumlah ->
            addToCart(barang, jumlah)  // Menambahkan barang ke keranjang ketika dipilih
        }
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)  // Menampilkan barang dalam 2 kolom
        binding.rvProducts.adapter = barangAdapter  // Menghubungkan RecyclerView dengan adapter

        // Inisialisasi Room Database untuk mengakses keranjang belanja
        appDatabase = AppDatabase.getInstance(this)

        // Logging URL API yang akan dipanggil untuk mengambil data barang
        Log.d(TAG, "Request URL: ${RetrofitInstance.getInstance().baseUrl()}barang")

        // Mengambil data barang dari API
        fetchBarangFromApi()

        // Tombol logout
        binding.btnLogout.setOnClickListener {
            // Menghapus status login dari SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)  // Mengubah status login menjadi false
            editor.remove("username")  // Menghapus username yang tersimpan
            editor.apply()

            // Menampilkan toast sebagai konfirmasi logout
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Mengarahkan pengguna ke halaman login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Menutup MainActivity
        }

        // Tombol untuk membuka daftar keranjang
        binding.btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)  // Membuka CartActivity untuk menampilkan keranjang belanja
        }
    }

    // Fungsi untuk mengambil data barang dari API menggunakan Retrofit
    private fun fetchBarangFromApi() {
        val call = RetrofitInstance.api.getBarang()  // Memanggil API untuk mendapatkan daftar barang

        call.enqueue(object : Callback<List<Barang>> {
            override fun onResponse(call: Call<List<Barang>>, response: Response<List<Barang>>) {
                if (response.isSuccessful) {
                    Log.d("Retrofit", "Response successful: ${response.body()}")

                    // Menyimpan daftar barang yang diterima dari response
                    val barangList = response.body() ?: emptyList()  // Jika data kosong, kirimkan list kosong

                    // Menampilkan data barang ke RecyclerView melalui adapter
                    barangAdapter.setBarangList(barangList)
                } else {
                    // Menampilkan error jika response gagal
                    Log.e("Retrofit", "Response error: Code ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Barang>>, t: Throwable) {
                // Menampilkan error jika API call gagal
                Log.e("Retrofit", "API call failed: ${t.message}")
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk menambahkan barang ke keranjang
    private fun addToCart(barang: Barang, jumlah: Int) {
        val totalHarga = barang.harga * jumlah  // Menghitung total harga berdasarkan jumlah yang dipilih
        val cartItem = Cart(
            namaBarang = barang.namaBarang,  // Menyimpan nama barang
            jumlah = jumlah,  // Menyimpan jumlah barang
            harga = barang.harga,  // Menyimpan harga barang per item
            totalHarga = totalHarga  // Menyimpan total harga
        )

        // Menambahkan item ke keranjang menggunakan Room Database
        Thread {
            appDatabase.cartDao().addToCart(cartItem)  // Menambahkan item ke dalam database lokal
            runOnUiThread {
                // Menampilkan toast setelah item berhasil ditambahkan ke keranjang
                Toast.makeText(this, "Item ${barang.namaBarang} added to cart", Toast.LENGTH_SHORT).show()
            }
        }.start()  // Menjalankan operasi di background thread agar tidak mengganggu UI thread
    }
}
