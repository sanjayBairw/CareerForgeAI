package com.pynexia.resumebuilder.ui.templates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.databinding.ItemTemplateCardBinding
import com.pynexia.resumebuilder.pdf.ResumeTemplate

class TemplateAdapter(
    private val templates: List<ResumeTemplate>,
    private val onUseTemplateClick: (ResumeTemplate, String) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val binding = ItemTemplateCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TemplateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount(): Int = templates.size

    inner class TemplateViewHolder(private val binding: ItemTemplateCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var selectedColor: String = "#2563EB"

        fun bind(template: ResumeTemplate) {
            selectedColor = template.defaultColor
            binding.tvTemplateName.text = template.name
            binding.tvTemplateAtsBadge.text = template.atsRating
            binding.tvTemplateDesc.text = template.description

            binding.colorDotBlue.setOnClickListener { selectedColor = "#2563EB" }
            binding.colorDotEmerald.setOnClickListener { selectedColor = "#059669" }
            binding.colorDotPurple.setOnClickListener { selectedColor = "#7C3AED" }
            binding.colorDotCrimson.setOnClickListener { selectedColor = "#DC2626" }
            binding.colorDotSlate.setOnClickListener { selectedColor = "#0F172A" }

            binding.btnUseTemplate.setOnClickListener {
                onUseTemplateClick(template, selectedColor)
            }

            binding.root.setOnClickListener {
                onUseTemplateClick(template, selectedColor)
            }
        }
    }
}
