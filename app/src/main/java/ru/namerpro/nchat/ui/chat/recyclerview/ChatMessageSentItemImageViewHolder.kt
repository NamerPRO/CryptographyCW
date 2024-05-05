package ru.namerpro.nchat.ui.chat.recyclerview

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageSentItemImageBinding
import ru.namerpro.nchat.domain.model.Message

class ChatMessageSentItemImageViewHolder(
    private val binding: ChatMessageSentItemImageBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        image: Uri,
        message: Message.File,
        showInGallery: (Message.File) -> Unit
    ) {
        binding.date.text = message.date
        binding.image.setImageURI(image)
        binding.image.setOnClickListener {
            showInGallery(message)
        }
    }

}