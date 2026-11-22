package com.pynexia.resumebuilder.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.data.model.EducationItem
import com.pynexia.resumebuilder.databinding.ItemEducationBinding

class EducationAdapter(
    private val items: MutableList<EducationItem>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<EducationAdapter.EduViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EduViewHolder {
        val binding = ItemEducationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EduViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EduViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class EduViewHolder(private val binding: ItemEducationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EducationItem, position: Int) {
            binding.tvItemEduDegree.text = "${item.degree} - ${item.fieldOfStudy}"
            binding.tvItemEduSchool.text = item.institution
            val dateText = "${item.startDate} - ${item.endDate} ${if (item.grade.isNotBlank()) " | " + item.grade else ""}"
            binding.tvItemEduDates.text = dateText

            binding.btnDeleteEdu.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }
}
