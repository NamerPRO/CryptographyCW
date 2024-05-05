package ru.namerpro.nchat.ui.chat.recyclerview

import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageSentLoadingBinding
import ru.namerpro.nchat.domain.model.Task
import kotlin.math.roundToInt

class ChatMessageSentLoadingViewHolder(
    private val binding: ChatMessageSentLoadingBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        progress: Double,
        task: Task?
    ) {
        binding.sendProgress.progress = progress.roundToInt()
        binding.cancelTask.setOnClickListener {
            task?.isCancelled = true
        }
    }

}