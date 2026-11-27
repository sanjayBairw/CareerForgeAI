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
            binding.tvTemplateCategory.text = template.category
            binding.tvTemplateLayoutTag.text = template.layoutTag
            binding.tvTemplateDesc.text = template.description

            val colorDots = listOf(
                binding.colorDotBlue to "#2563EB",
                binding.colorDotEmerald to "#059669",
                binding.colorDotPurple to "#7C3AED",
                binding.colorDotCrimson to "#DC2626",
                binding.colorDotSlate to "#0F172A",
                binding.colorDotAmber to "#D97706"
            )

            fun updateDotSelection(activeColor: String) {
                selectedColor = activeColor
                colorDots.forEach { (view, color) ->
                    view.alpha = if (color.equals(activeColor, ignoreCase = true)) 1.0f else 0.5f
                    view.scaleX = if (color.equals(activeColor, ignoreCase = true)) 1.25f else 1.0f
                    view.scaleY = if (color.equals(activeColor, ignoreCase = true)) 1.25f else 1.0f
                }
            }

            updateDotSelection(selectedColor)

            colorDots.forEach { (view, color) ->
                view.setOnClickListener {
                    updateDotSelection(color)
                }
            }

            binding.btnUseTemplate.setOnClickListener {
                onUseTemplateClick(template, selectedColor)
            }

            binding.root.setOnClickListener {
                onUseTemplateClick(template, selectedColor)
            }
        }
    }
}
