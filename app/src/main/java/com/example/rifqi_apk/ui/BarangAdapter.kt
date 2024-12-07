package com.example.rifqi_apk.ui

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.rifqi_apk.R
import com.example.rifqi_apk.databinding.ItemBarangBinding
import com.example.rifqi_apk.model.Barang
import com.squareup.picasso.Picasso

class BarangAdapter(
    private val barangList: MutableList<Barang> = mutableListOf(),
    private val onAddToCart: (Barang, Int) -> Unit // Callback untuk menambah ke keranjang
) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    fun setBarangList(newList: List<Barang>) {
        barangList.clear()
        barangList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val binding = ItemBarangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BarangViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        holder.bind(barangList[position])
    }

    override fun getItemCount(): Int = barangList.size

    inner class BarangViewHolder(private val binding: ItemBarangBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(barang: Barang) {
            binding.apply {
                tvNamaBarang.text = barang.namaBarang
                tvHarga.text = "Rp ${barang.harga}"
                tvStok.text = "Stok: ${barang.stok}"

                // Menggunakan Picasso untuk memuat gambar
                Picasso.get()
                    .load(barang.gambar)
                    .placeholder(R.drawable.placeholder) // Gambar placeholder
//                  .error(R.drawable.error) // Gambar error
                    .fit() // Menyesuaikan ukuran
                    .centerCrop() // Memotong gambar jika perlu
                    .into(ivBarang)

                // Tombol "Keranjang Plus"
                btnAddToCart.setOnClickListener {
                    val context = binding.root.context
                    val input = EditText(context).apply {
                        hint = "Masukkan jumlah"
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    }

                    // Menampilkan dialog input jumlah
                    AlertDialog.Builder(context)
                        .setTitle("Tambah ke Keranjang")
                        .setMessage("Masukkan jumlah untuk ${barang.namaBarang}")
                        .setView(input)
                        .setPositiveButton("Tambah") { _, _ ->
                            val jumlah = input.text.toString().toIntOrNull() ?: 0
                            if (jumlah > 0) {
                                onAddToCart(barang, jumlah)
                            } else {
                                Toast.makeText(context, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }
            }
        }
    }
}
