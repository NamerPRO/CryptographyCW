package ru.namerpro.nchat.ui.chat.recyclerview

import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemUnknownBinding
import ru.namerpro.nchat.domain.model.Message

class ChatMessageReceivedItemUnknownViewHolder(
    private val binding: ChatMessageReceivedItemUnknownBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        message: Message.File,
        download: (Message.File) -> Unit
    ) {
        binding.title.text = message.realName
        binding.date.text = message.date

        binding.downloadReceived.setOnClickListener {
            download(message)
        }
    }

}