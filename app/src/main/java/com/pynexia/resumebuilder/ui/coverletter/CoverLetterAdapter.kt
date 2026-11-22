package com.pynexia.resumebuilder.ui.coverletter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.data.model.CoverLetterEntity
import com.pynexia.resumebuilder.databinding.ItemCoverLetterBinding

class CoverLetterAdapter(
    private var items: List<CoverLetterEntity> = emptyList(),
    private val onEditClick: (CoverLetterEntity) -> Unit,
    private val onShareClick: (CoverLetterEntity) -> Unit,
    private val onDeleteClick: (CoverLetterEntity) -> Unit
) : RecyclerView.Adapter<CoverLetterAdapter.ClViewHolder>() {

    fun submitList(newList: List<CoverLetterEntity>) {
        this.items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClViewHolder {
        val binding = ItemCoverLetterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ClViewHolder(private val binding: ItemCoverLetterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CoverLetterEntity) {
            binding.tvClItemTitle.text = item.title
            binding.tvClItemMeta.text = "For ${item.companyName} • ${item.jobTitle}"
            binding.tvClItemSnippet.text = item.bodyText

            binding.btnEditCl.setOnClickListener { onEditClick(item) }
            binding.btnShareCl.setOnClickListener { onShareClick(item) }
            binding.btnDeleteCl.setOnClickListener { onDeleteClick(item) }
            binding.root.setOnClickListener { onEditClick(item) }
        }
    }
}
