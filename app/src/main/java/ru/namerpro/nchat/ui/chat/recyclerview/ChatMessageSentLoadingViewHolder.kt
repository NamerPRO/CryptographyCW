package ru.namerpro.nchat.ui.chat.recyclerview

import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import ru.namerpro.nchat.databinding.ChatMessageSentLoadingBinding
import kotlin.math.roundToInt

class ChatMessageSentLoadingViewHolder(
    private val binding: ChatMessageSentLoadingBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        progress: Double,
        coroutineScope: CoroutineScope?
    ) {
        binding.sendProgress.progress = progress.roundToInt()
        binding.cancelTask.setOnClickListener {
            coroutineScope?.cancel()
        }
    }

}