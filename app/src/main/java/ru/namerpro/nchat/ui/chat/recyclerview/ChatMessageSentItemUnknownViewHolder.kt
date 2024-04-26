package ru.namerpro.nchat.ui.chat.recyclerview

import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageSentItemUnknownBinding

class ChatMessageSentItemUnknownViewHolder(
    private val binding: ChatMessageSentItemUnknownBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        title: String,
        date: String
    ) {
        binding.title.text = title
        binding.date.text = date
    }

}