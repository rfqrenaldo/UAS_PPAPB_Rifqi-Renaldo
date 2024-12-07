package com.example.rifqi_apk.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rifqi_apk.R
import com.example.rifqi_apk.databaseLokal.AppDatabase
import com.example.rifqi_apk.databaseLokal.Cart
import com.example.rifqi_apk.databinding.ActivityCartBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var appDatabase: AppDatabase
    private lateinit var cartAdapter: CartAdapter

    // Variabel untuk menyimpan data input dari Popup
    private var namaPenerima: String? = null
    private var nomorTelepon: String? = null
    private var alamat: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Room Database
        appDatabase = AppDatabase.getInstance(this)

        // Inisialisasi RecyclerView dan Adapter
        cartAdapter = CartAdapter(appDatabase, this)  // Menyertakan CartActivity untuk update total harga
        binding.rvCart.layoutManager = LinearLayoutManager(this)
        binding.rvCart.adapter = cartAdapter

        // Ambil data keranjang dari database
        loadCartItems()

        // Tombol Back
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Tombol Checkout
        binding.btnCheckout.setOnClickListener {
            if (namaPenerima != null && nomorTelepon != null && alamat != null) {
                val intent = Intent(this, InvoiceActivity::class.java).apply {
                    putExtra("NAMA_PENERIMA", namaPenerima)
                    putExtra("NOMOR_TELEPON", nomorTelepon)
                    putExtra("ALAMAT", alamat)
                }
                startActivity(intent)
            } else {
                // Informasi tidak lengkap, tampilkan pesan
                AlertDialog.Builder(this)
                    .setMessage("Silakan isi informasi pengiriman terlebih dahulu.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // Tombol Add Home
        binding.btnAddhome.setOnClickListener {
            // Menampilkan Popup untuk Input Data
            val dialog = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.popup_input_form, null)

            val etNamaPenerima = dialogView.findViewById<EditText>(R.id.etNamaPenerima)
            val etNomorTelepon = dialogView.findViewById<EditText>(R.id.etNomorTelepon)
            val etAlamat = dialogView.findViewById<EditText>(R.id.etAlamat)

            dialog.setView(dialogView)
            dialog.setPositiveButton("OK") { _, _ ->
                // Mengambil data yang diinputkan
                namaPenerima = etNamaPenerima.text.toString()
                nomorTelepon = etNomorTelepon.text.toString()
                alamat = etAlamat.text.toString()

                // Menyimpan data yang diinputkan untuk digunakan saat Checkout
                // Data ini tidak dikirimkan ke InvoiceActivity sampai tombol Checkout ditekan
            }
            dialog.setNegativeButton("Batal", null)
            dialog.show()
        }
    }

    private fun loadCartItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Ambil data keranjang dari database di thread IO
                val cartItems = appDatabase.cartDao().getAllCartItems()

                // Kembali ke thread utama untuk memperbarui UI
                withContext(Dispatchers.Main) {
                    cartAdapter.setCartItems(cartItems)
                    updateTotalPrice(cartItems)

                    if (cartItems.isEmpty()) {
                        // Menampilkan pesan jika keranjang kosong
                        binding.tvEmptyCart.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyCart.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // Menangani error jika terjadi masalah saat mengambil data
                withContext(Dispatchers.Main) {
                    binding.tvEmptyCart.visibility = View.VISIBLE
                }
            }
        }
    }

    // Fungsi untuk menghitung total harga
    fun updateTotalPrice(cartItems: List<Cart>) {
        val totalPrice = cartItems.sumOf { it.totalHarga }
        binding.tvTotalHarga.text = "Total Harga: Rp $totalPrice"

        // Menampilkan atau menyembunyikan pesan "Keranjang kosong"
        if (cartItems.isEmpty()) {
            binding.tvEmptyCart.visibility = View.VISIBLE
        } else {
            binding.tvEmptyCart.visibility = View.GONE
        }
    }
}

