package com.example.rifqi_apk.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rifqi_apk.R
import com.example.rifqi_apk.databaseLokal.AppDatabase
import com.example.rifqi_apk.databaseLokal.Cart
import com.example.rifqi_apk.databinding.ActivityInvoiceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceBinding
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Room Database
        appDatabase = AppDatabase.getInstance(this)

        // Ambil data penerima yang dikirimkan melalui Intent
        val namaPenerima = intent.getStringExtra("NAMA_PENERIMA")
        val nomorTelepon = intent.getStringExtra("NOMOR_TELEPON")
        val alamat = intent.getStringExtra("ALAMAT")

        // Tampilkan data penerima
        binding.etNamaPenerima.setText(namaPenerima)
        binding.etNomorTelepon.setText(nomorTelepon)
        binding.etAlamat.setText(alamat)



        binding.btnBackHome.setOnClickListener {
            // Menghapus semua item dari keranjang
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Menghapus semua item dari tabel Cart
                    appDatabase.cartDao().deleteAllCartItems()

                    // Kembali ke thread utama setelah menghapus
                    withContext(Dispatchers.Main) {

                        // Arahkan pengguna kembali ke MainActivity
                        val intent = Intent(this@InvoiceActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Mengakhiri InvoiceActivity agar tidak kembali ke activity ini
                    }
                } catch (e: Exception) {
                    // Menangani error jika ada masalah saat menghapus
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@InvoiceActivity, "Gagal menghapus data keranjang", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        // Menampilkan daftar barang dan total harga
        loadCartItems()
    }

    private fun loadCartItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Ambil data keranjang dari database di thread IO
                val cartItems = appDatabase.cartDao().getAllCartItems()

                // Kembali ke thread utama untuk memperbarui UI
                withContext(Dispatchers.Main) {
                    if (cartItems.isEmpty()) {
                        binding.tvDaftarBarangList.text = "Keranjang kosong"
                    } else {
                        // Tampilkan daftar barang dengan jumlah
                        val cartList = cartItems.joinToString("\n") { item ->
                            "${item.namaBarang} - ${item.jumlah} x Rp ${item.harga} = Rp ${item.totalHarga}"
                        }
                        binding.tvDaftarBarangList.text = cartList
                    }

                    // Hitung total harga dan tampilkan
                    val totalHarga = cartItems.sumOf { it.totalHarga }
                    binding.tvTotalHarga.text = "Total Harga: Rp $totalHarga"
                }
            } catch (e: Exception) {
                // Tangani error jika terjadi masalah saat mengambil data
                withContext(Dispatchers.Main) {
                    binding.tvDaftarBarangList.text = "Error: Tidak dapat mengambil data"
                }
            }
        }
    }

}
