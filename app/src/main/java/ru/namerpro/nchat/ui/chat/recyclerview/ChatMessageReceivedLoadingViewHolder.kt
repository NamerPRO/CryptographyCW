package ru.namerpro.nchat.ui.chat.recyclerview

import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageReceivedLoadingBinding
import ru.namerpro.nchat.domain.model.Task
import kotlin.math.roundToInt

class ChatMessageReceivedLoadingViewHolder(
    private val binding: ChatMessageReceivedLoadingBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        progress: Double,
        task: Task?
    ) {
        binding.receiveProgress.progress = progress.roundToInt()
        binding.cancelTask.setOnClickListener {
            task?.isCancelled = true
        }
    }

}