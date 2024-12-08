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

// Kelas untuk aktivitas keranjang belanja (CartActivity)
class CartActivity : AppCompatActivity() {

    // View binding untuk mengakses elemen UI secara langsung
    private lateinit var binding: ActivityCartBinding

    // Instance database lokal Room
    private lateinit var appDatabase: AppDatabase

    // Adapter untuk RecyclerView yang menampilkan data keranjang
    private lateinit var cartAdapter: CartAdapter

    // Variabel untuk menyimpan data input dari Popup (nama penerima, nomor telepon, alamat)
    private var namaPenerima: String? = null
    private var nomorTelepon: String? = null
    private var alamat: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater) // Inisialisasi ViewBinding
        setContentView(binding.root)

        // Inisialisasi database Room
        appDatabase = AppDatabase.getInstance(this)

        // Inisialisasi RecyclerView dan Adapter
        cartAdapter = CartAdapter(appDatabase, this)  // Menggunakan adapter yang terhubung dengan database
        binding.rvCart.layoutManager = LinearLayoutManager(this) // Mengatur tata letak RecyclerView secara vertikal
        binding.rvCart.adapter = cartAdapter // Menghubungkan adapter ke RecyclerView

        // Memuat data keranjang dari database
        loadCartItems()

        // Tombol kembali ke halaman utama
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // Intent ke MainActivity
            startActivity(intent)
        }

        // Tombol checkout untuk mengarahkan ke halaman Invoice
        binding.btnCheckout.setOnClickListener {
            if (namaPenerima != null && nomorTelepon != null && alamat != null) {
                // Jika data pengiriman sudah diisi, lanjut ke InvoiceActivity
                val intent = Intent(this, InvoiceActivity::class.java).apply {
                    putExtra("NAMA_PENERIMA", namaPenerima)
                    putExtra("NOMOR_TELEPON", nomorTelepon)
                    putExtra("ALAMAT", alamat)
                }
                startActivity(intent)
            } else {
                // Jika data belum lengkap, tampilkan pesan peringatan
                AlertDialog.Builder(this)
                    .setMessage("Silakan isi informasi pengiriman terlebih dahulu.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // Tombol untuk menambahkan data pengiriman (popup input form)
        binding.btnAddhome.setOnClickListener {
            // Menampilkan Popup Input
            val dialog = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.popup_input_form, null)

            val etNamaPenerima = dialogView.findViewById<EditText>(R.id.etNamaPenerima)
            val etNomorTelepon = dialogView.findViewById<EditText>(R.id.etNomorTelepon)
            val etAlamat = dialogView.findViewById<EditText>(R.id.etAlamat)

            dialog.setView(dialogView)
            dialog.setPositiveButton("OK") { _, _ ->
                // Menyimpan data input pengguna
                namaPenerima = etNamaPenerima.text.toString()
                nomorTelepon = etNomorTelepon.text.toString()
                alamat = etAlamat.text.toString()
            }
            dialog.setNegativeButton("Batal", null)
            dialog.show()
        }
    }

    // Fungsi untuk memuat data keranjang dari database
    private fun loadCartItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Ambil data keranjang di thread IO (untuk operasi database)
                val cartItems = appDatabase.cartDao().getAllCartItems()

                // Kembali ke thread utama untuk memperbarui UI
                withContext(Dispatchers.Main) {
                    cartAdapter.setCartItems(cartItems) // Mengisi data pada adapter
                    updateTotalPrice(cartItems) // Menghitung dan menampilkan total harga

                    // Menampilkan pesan jika keranjang kosong
                    if (cartItems.isEmpty()) {
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

    // Fungsi untuk menghitung dan memperbarui total harga
    fun updateTotalPrice(cartItems: List<Cart>) {
        val totalPrice = cartItems.sumOf { it.totalHarga } // Menghitung total harga semua item
        binding.tvTotalHarga.text = "Total Harga: Rp $totalPrice" // Menampilkan total harga

        // Menampilkan atau menyembunyikan pesan "Keranjang kosong"
        if (cartItems.isEmpty()) {
            binding.tvEmptyCart.visibility = View.VISIBLE
        } else {
            binding.tvEmptyCart.visibility = View.GONE
        }
    }
}
