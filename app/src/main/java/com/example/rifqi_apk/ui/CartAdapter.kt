package com.example.rifqi_apk.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.rifqi_apk.databinding.ItemCartBinding
import com.example.rifqi_apk.databaseLokal.AppDatabase
import com.example.rifqi_apk.databaseLokal.Cart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Adapter untuk RecyclerView yang menampilkan item keranjang belanja
class CartAdapter(private val appDatabase: AppDatabase, private val activity: CartActivity) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Daftar item keranjang yang akan ditampilkan
    private var cartItems: List<Cart> = emptyList()

    // Fungsi untuk mengubah data keranjang yang akan ditampilkan pada RecyclerView
    fun setCartItems(items: List<Cart>) {
        cartItems = items
        notifyDataSetChanged() // Memberi tahu adapter bahwa data telah berubah
    }

    // Membuat ViewHolder yang akan memegang item keranjang
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    // Mengikat data item keranjang dengan tampilan pada ViewHolder
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    // Mengembalikan jumlah item keranjang yang ada
    override fun getItemCount(): Int = cartItems.size

    // ViewHolder untuk menampilkan item keranjang dalam RecyclerView
    inner class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk mengikat data Cart ke dalam tampilan item
        fun bind(cart: Cart) {
            binding.apply {
                // Menampilkan data pada elemen UI
                tvNamaBarang.text = cart.namaBarang
                tvJumlah.text = "Jumlah: ${cart.jumlah}"
                tvTotalHarga.text = "Rp ${cart.totalHarga}"

                // Tombol Decrease: Mengurangi jumlah barang jika lebih dari 1
                btnDecrease.setOnClickListener {
                    if (cart.jumlah > 1) {
                        val updatedCart = cart.copy(jumlah = cart.jumlah - 1, totalHarga = (cart.harga * (cart.jumlah - 1)))
                        updateCartItem(updatedCart) // Memperbarui item keranjang
                    } else {
                        Toast.makeText(binding.root.context, "Jumlah tidak bisa lebih kecil dari 1", Toast.LENGTH_SHORT).show()
                    }
                }

                // Tombol Increase: Menambah jumlah barang
                btnIncrease.setOnClickListener {
                    val updatedCart = cart.copy(jumlah = cart.jumlah + 1, totalHarga = (cart.harga * (cart.jumlah + 1)))
                    updateCartItem(updatedCart) // Memperbarui item keranjang
                }

                // Tombol Delete: Menghapus item dari keranjang
                btnDelete.setOnClickListener {
                    deleteCartItem(cart) // Menghapus item dari keranjang
                }
            }
        }

        // Fungsi untuk memperbarui item keranjang dalam database
        private fun updateCartItem(cart: Cart) {
            CoroutineScope(Dispatchers.IO).launch {
                appDatabase.cartDao().updateCart(cart)  // Memperbarui data item keranjang di database
                loadUpdatedCartItems()  // Memuat ulang item keranjang setelah update
            }
        }

        // Fungsi untuk menghapus item keranjang dari database
        private fun deleteCartItem(cart: Cart) {
            CoroutineScope(Dispatchers.IO).launch {
                appDatabase.cartDao().delete(cart)  // Menghapus item dari database
                loadUpdatedCartItems()  // Memuat ulang item keranjang setelah delete
            }
        }

        // Fungsi untuk memuat ulang item keranjang dan memperbarui RecyclerView
        private fun loadUpdatedCartItems() {
            val updatedCartItems = appDatabase.cartDao().getAllCartItems() // Mengambil semua item keranjang
            CoroutineScope(Dispatchers.Main).launch {
                setCartItems(updatedCartItems)  // Memperbarui data RecyclerView dengan data terbaru
                activity.updateTotalPrice(updatedCartItems)  // Memperbarui total harga di activity
            }
        }
    }
}
