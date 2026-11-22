package com.pynexia.resumebuilder.ui.jobs

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.data.model.JobApplicationEntity
import com.pynexia.resumebuilder.databinding.ItemJobApplicationBinding

class JobApplicationAdapter(
    private var items: List<JobApplicationEntity> = emptyList(),
    private val onItemClick: (JobApplicationEntity) -> Unit
) : RecyclerView.Adapter<JobApplicationAdapter.JobViewHolder>() {

    fun submitList(newList: List<JobApplicationEntity>) {
        this.items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobApplicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class JobViewHolder(private val binding: ItemJobApplicationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: JobApplicationEntity) {
            binding.tvJobCompany.text = item.companyName
            binding.tvJobTitle.text = "${item.jobTitle} • ${item.jobType}"
            binding.tvJobMeta.text = "${item.location} • ${item.salaryRange}"
            binding.tvJobNotes.text = item.notes.ifBlank { "No interview notes added yet." }

            binding.tvJobStatus.text = item.status
            when (item.status) {
                "INTERVIEW" -> binding.tvJobStatus.setTextColor(Color.parseColor("#F59E0B"))
                "OFFER" -> binding.tvJobStatus.setTextColor(Color.parseColor("#10B981"))
                "REJECTED" -> binding.tvJobStatus.setTextColor(Color.parseColor("#EF4444"))
                "APPLIED" -> binding.tvJobStatus.setTextColor(Color.parseColor("#3B82F6"))
                else -> binding.tvJobStatus.setTextColor(Color.parseColor("#38BDF8"))
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
