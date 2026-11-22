package com.pynexia.resumebuilder.ui.resumes

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.R
import com.pynexia.resumebuilder.data.model.ResumeEntity
import com.pynexia.resumebuilder.databinding.ItemResumeCardBinding
import com.pynexia.resumebuilder.pdf.TemplateRegistry

class ResumeAdapter(
    private val onEditClick: (ResumeEntity) -> Unit,
    private val onPreviewClick: (ResumeEntity) -> Unit,
    private val onDuplicateClick: (ResumeEntity) -> Unit,
    private val onShareClick: (ResumeEntity) -> Unit,
    private val onDeleteClick: (ResumeEntity) -> Unit
) : ListAdapter<ResumeEntity, ResumeAdapter.ResumeViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResumeViewHolder {
        val binding = ItemResumeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResumeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResumeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ResumeViewHolder(private val binding: ItemResumeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ResumeEntity) {
            binding.tvResumeTitle.text = item.title
            binding.tvResumeTargetRole.text = item.targetJobTitle.ifBlank { "Professional" }

            val template = TemplateRegistry.getById(item.templateId)
            val colText = if (template.isTwoColumn) "2-Col" else "1-Col"
            binding.tvTemplateBadge.text = "${template.name} • $colText"

            val timeAgo = DateUtils.getRelativeTimeSpanString(
                item.updatedAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            binding.tvUpdatedTime.text = "Edited $timeAgo"

            // ATS Score Badge
            val atsScore = item.lastAtsScore
            binding.tvAtsBadge.text = "$atsScore% ATS"
            if (atsScore >= 85) {
                binding.tvAtsBadge.setTextColor(Color.parseColor("#10B981"))
            } else if (atsScore >= 70) {
                binding.tvAtsBadge.setTextColor(Color.parseColor("#F59E0B"))
            } else {
                binding.tvAtsBadge.setTextColor(Color.parseColor("#EF4444"))
            }

            // Click Handlers
            binding.root.setOnClickListener { onEditClick(item) }
            binding.btnActionEdit.setOnClickListener { onEditClick(item) }
            binding.btnActionPreview.setOnClickListener { onPreviewClick(item) }
            binding.btnActionDuplicate.setOnClickListener { onDuplicateClick(item) }
            binding.btnActionShare.setOnClickListener { onShareClick(item) }
            binding.btnActionDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ResumeEntity>() {
        override fun areItemsTheSame(oldItem: ResumeEntity, newItem: ResumeEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ResumeEntity, newItem: ResumeEntity): Boolean =
            oldItem == newItem
    }
}
