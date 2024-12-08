package com.example.rifqi_apk.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.rifqi_apk.databaseLokal.AppDatabase
import com.example.rifqi_apk.databaseLokal.User
import com.example.rifqi_apk.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding  // Binding untuk mengakses elemen UI dari activity_register.xml
    private lateinit var db: AppDatabase  // Database instance untuk mengakses Room Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)  // Meng-inflate layout menggunakan ViewBinding
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)  // Menginisialisasi instance dari Room Database

        // Logika untuk tombol Register
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()  // Mengambil username yang dimasukkan pengguna
            val password = binding.etPassword.text.toString()  // Mengambil password yang dimasukkan pengguna

            if (username.isNotEmpty() && password.isNotEmpty()) {  // Memeriksa apakah field username dan password tidak kosong
                // Jalankan operasi database di thread background menggunakan Coroutine
                lifecycleScope.launch {
                    // Memindahkan operasi database ke background thread
                    withContext(Dispatchers.IO) {
                        // Menambahkan data user baru ke database menggunakan Room
                        db.userDao().registerUser(User(username = username, password = password))
                    }

                    // Kembali ke UI thread untuk menampilkan Toast dan pindah ke LoginActivity
                    Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)  // Mengarahkan pengguna ke halaman login setelah registrasi berhasil
                }
            } else {
                // Menampilkan pesan error jika ada field yang kosong
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Logika untuk TextView "Login here" yang mengarahkan pengguna ke halaman LoginActivity
        binding.tvLoginPrompt.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)  // Membuka halaman LoginActivity
        }
    }
}
