package com.pynexia.resumebuilder.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.data.model.ProjectItem
import com.pynexia.resumebuilder.databinding.ItemProjectBinding

class ProjectAdapter(
    private val items: MutableList<ProjectItem>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjViewHolder {
        val binding = ItemProjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProjViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class ProjViewHolder(private val binding: ItemProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProjectItem, position: Int) {
            binding.tvItemProjTitle.text = item.title
            binding.tvItemProjStack.text = item.techStack
            binding.tvItemProjDesc.text = item.description

            binding.btnDeleteProj.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }
}
