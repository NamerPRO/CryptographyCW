package ru.namerpro.nchat.ui.chat.recyclerview

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.namerpro.nchat.databinding.ChatMessageChatEndedBinding
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemImageBinding
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemTextBinding
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemUnknownBinding
import ru.namerpro.nchat.databinding.ChatMessageSentItemImageBinding
import ru.namerpro.nchat.databinding.ChatMessageSentItemTextBinding
import ru.namerpro.nchat.databinding.ChatMessageSentItemUnknownBinding
import ru.namerpro.nchat.domain.model.Message

class ChatAdapter(
    val messages: ArrayList<Message>,
    private val toUri: (Message.File) -> Uri
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SENT_TEXT -> ChatMessageSentItemTextViewHolder(ChatMessageSentItemTextBinding.inflate(layoutInflater, parent, false))
            SENT_IMAGE -> ChatMessageSentItemImageViewHolder(ChatMessageSentItemImageBinding.inflate(layoutInflater, parent, false))
            SENT_UNKNOWN -> ChatMessageSentItemUnknownViewHolder(ChatMessageSentItemUnknownBinding.inflate(layoutInflater, parent, false))
            RECEIVED_TEXT -> ChatMessageReceivedItemTextViewHolder(ChatMessageReceivedItemTextBinding.inflate(layoutInflater, parent, false))
            RECEIVED_IMAGE -> ChatMessageReceivedItemImageViewHolder(ChatMessageReceivedItemImageBinding.inflate(layoutInflater, parent, false))
            RECEIVED_UNKNOWN -> ChatMessageReceivedItemUnknownViewHolder(ChatMessageReceivedItemUnknownBinding.inflate(layoutInflater, parent, false))
            CHAT_ENDED -> ChatMessageChatEndedViewHolder(ChatMessageChatEndedBinding.inflate(layoutInflater, parent, false))
            else -> error("Never thrown. Added to make compiler happy.")
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val message = messages[position]
        when (holder.itemViewType) {
            SENT_TEXT -> {
                holder as ChatMessageSentItemTextViewHolder
                message as Message.Data

                holder.bind(message.text, message.date)
            }
            SENT_IMAGE -> {
                holder as ChatMessageSentItemImageViewHolder
                message as Message.File

                holder.bind(toUri(message), message.date)
            }
            SENT_UNKNOWN -> {
                holder as ChatMessageSentItemUnknownViewHolder
                message as Message.File

//                holder.bind(toUri(message).toFile().name, message.date) // !!!
            }
            RECEIVED_TEXT -> {
                holder as ChatMessageReceivedItemTextViewHolder
                message as Message.Data

                holder.bind(message.text, message.date)
            }
            RECEIVED_IMAGE -> {
                // добавить проверку, если пришел запрос FailedToLoadImage, то выводитьизображение заглушку
                holder as ChatMessageReceivedItemImageViewHolder
                message as Message.File

                holder.bind(toUri(message), message.date)
            }
            RECEIVED_UNKNOWN -> {
                holder as ChatMessageReceivedItemUnknownViewHolder
                message as Message.File

//                holder.bing(toUri(message).toFile().name, message.date) // !!!
            }
        }
    }

    override fun getItemViewType(
        position: Int
    ): Int {
        val item = messages[position]
        return when (item.contentType) {
            Message.MESSAGE_TEXT_CODE -> if (item.isMessageReceived) RECEIVED_TEXT else SENT_TEXT
            Message.MESSAGE_IMAGE_CODE -> if (item.isMessageReceived) RECEIVED_IMAGE else SENT_IMAGE
            Message.MESSAGE_FILE_CODE -> if (item.isMessageReceived) RECEIVED_UNKNOWN else SENT_UNKNOWN
            Message.MESSAGE_CONVERSATION_END_CODE -> CHAT_ENDED
            else -> error("Never thrown! Added to make compiler happy.")
        }
    }

    companion object {
        const val SENT_TEXT = 0
        const val SENT_IMAGE = 1
        const val SENT_UNKNOWN = 2
        const val RECEIVED_TEXT = 3
        const val RECEIVED_IMAGE = 4
        const val RECEIVED_UNKNOWN = 5
        const val CHAT_ENDED = 6
    }

}