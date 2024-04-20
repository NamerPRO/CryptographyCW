package ru.namerpro.nchat.ui.chatlist

import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatItemBinding

class ChatListViewHolder(
    private val binding: ChatItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        chatName: String
    ) {
        binding.chatName.text = chatName
    }

}