package ru.namerpro.nchat.ui.chat.recyclerview

import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageSentItemUnknownBinding
import ru.namerpro.nchat.domain.model.Message

class ChatMessageSentItemUnknownViewHolder(
    private val binding: ChatMessageSentItemUnknownBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        message: Message.File,
        download: (Message.File) -> Unit
    ) {
        binding.title.text = message.realName
        binding.date.text = message.date

        binding.downloadSent.setOnClickListener {
            download.invoke(message)
        }
    }

}