package com.example.rifqi_apk.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.rifqi_apk.databaseLokal.AppDatabase
import com.example.rifqi_apk.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi SharedPreferences untuk menyimpan status login
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Inisialisasi database Room untuk validasi user
        appDatabase = AppDatabase.getInstance(applicationContext)

        // Mengecek apakah user sudah login sebelumnya
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            // Jika sudah login, langsung ke halaman utama
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Logika untuk tombol Login
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()  // Mengambil input username
            val password = binding.etPassword.text.toString()  // Mengambil input password

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Validasi user dari database lokal menggunakan Room
                lifecycleScope.launch(Dispatchers.IO) {  // Menjalankan proses di background thread
                    val user = appDatabase.userDao().loginUser(username, password)

                    withContext(Dispatchers.Main) {
                        // Kembali ke thread utama untuk memperbarui UI
                        if (user != null) {
                            // Jika user ditemukan, login berhasil
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", true)  // Tandai pengguna sudah login
                            editor.putString("username", username)  // Simpan username
                            editor.apply()

                            // Beralih ke halaman utama setelah login
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Jika tidak ditemukan, tampilkan pesan error
                            Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // Jika username atau password kosong
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
