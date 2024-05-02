package ru.namerpro.nchat.ui.chat.recyclerview

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import ru.namerpro.nchat.databinding.ChatMessageReceivedLoadingBinding
import kotlin.math.roundToInt

class ChatMessageReceivedLoadingViewHolder(
    private val binding: ChatMessageReceivedLoadingBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        progress: Double,
        coroutineScope: CoroutineScope?
    ) {
        binding.receiveProgress.progress = progress.roundToInt()
        binding.cancelTask.setOnClickListener {
            coroutineScope?.cancel()
        }
    }

}