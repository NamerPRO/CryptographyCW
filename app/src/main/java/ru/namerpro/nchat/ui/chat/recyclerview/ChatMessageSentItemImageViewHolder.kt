package ru.namerpro.nchat.ui.chat.recyclerview

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatMessageSentItemImageBinding

class ChatMessageSentItemImageViewHolder(
    private val binding: ChatMessageSentItemImageBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        image: Uri,
        date: String
    ) {
        binding.date.text = date
        binding.image.setImageURI(image)
    }

}