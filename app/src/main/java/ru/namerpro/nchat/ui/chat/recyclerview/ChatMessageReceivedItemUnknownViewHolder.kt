package ru.namerpro.nchat.ui.chat.recyclerview

import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemUnknownBinding

class ChatMessageReceivedItemUnknownViewHolder(
    private val binding: ChatMessageReceivedItemUnknownBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bing(
        title: String,
        date: String
    ) {
        binding.title.text = title
        binding.date.text = date
    }

}