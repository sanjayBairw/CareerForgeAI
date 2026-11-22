package com.pynexia.resumebuilder.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.data.model.ExperienceItem
import com.pynexia.resumebuilder.databinding.ItemExperienceBinding

class ExperienceAdapter(
    private val items: MutableList<ExperienceItem>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ExperienceAdapter.ExpViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpViewHolder {
        val binding = ItemExperienceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class ExpViewHolder(private val binding: ItemExperienceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExperienceItem, position: Int) {
            binding.tvItemExpRole.text = item.jobTitle
            binding.tvItemExpCompany.text = "${item.company} • ${item.employmentType}"
            val dateText = "${item.startDate} - ${if (item.isCurrent) "Present" else item.endDate} | ${item.location}"
            binding.tvItemExpDates.text = dateText
            binding.tvItemExpResp.text = item.responsibilities.ifBlank { item.measurableResults }

            binding.btnDeleteExp.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }
}
