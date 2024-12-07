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

class CartAdapter(private val appDatabase: AppDatabase, private val activity: CartActivity) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var cartItems: List<Cart> = emptyList()

    fun setCartItems(items: List<Cart>) {
        cartItems = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cart: Cart) {
            binding.apply {
                tvNamaBarang.text = cart.namaBarang
                tvJumlah.text = "Jumlah: ${cart.jumlah}"
                tvTotalHarga.text = "Rp ${cart.totalHarga}"

                // Button Decrease
                btnDecrease.setOnClickListener {
                    if (cart.jumlah > 1) {
                        val updatedCart = cart.copy(jumlah = cart.jumlah - 1, totalHarga = (cart.harga * (cart.jumlah - 1)))
                        updateCartItem(updatedCart)
                    } else {
                        Toast.makeText(binding.root.context, "Jumlah tidak bisa lebih kecil dari 1", Toast.LENGTH_SHORT).show()
                    }
                }

                // Button Increase
                btnIncrease.setOnClickListener {
                    val updatedCart = cart.copy(jumlah = cart.jumlah + 1, totalHarga = (cart.harga * (cart.jumlah + 1)))
                    updateCartItem(updatedCart)
                }

                // Button Delete
                btnDelete.setOnClickListener {
                    deleteCartItem(cart)
                }
            }
        }

        private fun updateCartItem(cart: Cart) {
            CoroutineScope(Dispatchers.IO).launch {
                appDatabase.cartDao().updateCart(cart)  // Menggunakan updateCart untuk memperbarui data
                loadUpdatedCartItems()  // Refresh daftar keranjang setelah update
            }
        }

        private fun deleteCartItem(cart: Cart) {
            CoroutineScope(Dispatchers.IO).launch {
                appDatabase.cartDao().delete(cart)  // Menghapus item dari keranjang
                loadUpdatedCartItems()  // Refresh daftar keranjang setelah delete
            }
        }

        private fun loadUpdatedCartItems() {
            val updatedCartItems = appDatabase.cartDao().getAllCartItems()
            CoroutineScope(Dispatchers.Main).launch {
                setCartItems(updatedCartItems)  // Update RecyclerView dengan data terbaru
                activity.updateTotalPrice(updatedCartItems)  // Memperbarui total harga
            }
        }
    }
}

