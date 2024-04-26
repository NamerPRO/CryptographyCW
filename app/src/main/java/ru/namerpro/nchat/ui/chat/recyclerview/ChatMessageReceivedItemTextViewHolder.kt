package ru.namerpro.nchat.ui.chat.recyclerview

import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemTextBinding

class ChatMessageReceivedItemTextViewHolder(
    private val binding: ChatMessageReceivedItemTextBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        text: String,
        date: String
    ) {
        binding.text.text = text
        binding.date.text = date
    }

}