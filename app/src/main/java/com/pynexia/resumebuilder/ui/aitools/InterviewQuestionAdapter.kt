package com.pynexia.resumebuilder.ui.aitools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pynexia.resumebuilder.ai.InterviewQuestion
import com.pynexia.resumebuilder.databinding.ItemInterviewQuestionBinding

class InterviewQuestionAdapter(
    private var items: List<InterviewQuestion> = emptyList()
) : RecyclerView.Adapter<InterviewQuestionAdapter.QuestionViewHolder>() {

    fun submitList(newList: List<InterviewQuestion>) {
        this.items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemInterviewQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class QuestionViewHolder(private val binding: ItemInterviewQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InterviewQuestion) {
            binding.tvQuestionCategory.text = item.category
            binding.tvQuestionText.text = item.question
            binding.tvQuestionFramework.text = item.sampleAnswerFramework
            binding.tvQuestionTips.text = "Expert Tip: ${item.tips}"
        }
    }
}
