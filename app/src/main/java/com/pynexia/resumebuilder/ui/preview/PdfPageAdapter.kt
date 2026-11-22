package com.pynexia.resumebuilder.ui.preview

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.databinding.ItemPdfPageBinding

class PdfPageAdapter(
    private var pages: List<Bitmap> = emptyList()
) : RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

    fun setPages(newPages: List<Bitmap>) {
        this.pages = newPages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemPdfPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class PageViewHolder(private val binding: ItemPdfPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bitmap: Bitmap) {
            binding.ivPdfPage.setImageBitmap(bitmap)
        }
    }
}
