package ru.namerpro.nchat.ui.chat.recyclerview

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.databinding.ChatMessageChatEndedBinding
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemImageBinding
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemNotReceivedBinding
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemTextBinding
import ru.namerpro.nchat.databinding.ChatMessageReceivedItemUnknownBinding
import ru.namerpro.nchat.databinding.ChatMessageReceivedLoadingBinding
import ru.namerpro.nchat.databinding.ChatMessageSentItemImageBinding
import ru.namerpro.nchat.databinding.ChatMessageSentItemNotSentBinding
import ru.namerpro.nchat.databinding.ChatMessageSentItemTextBinding
import ru.namerpro.nchat.databinding.ChatMessageSentItemUnknownBinding
import ru.namerpro.nchat.databinding.ChatMessageSentLoadingBinding
import ru.namerpro.nchat.domain.model.Message

class ChatAdapter(
    val messages: ArrayList<Message>,
    private val toUri: (Message.File) -> Uri,
    private val download: (Message.File) -> Unit
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
            SENT_LOADING -> ChatMessageSentLoadingViewHolder(ChatMessageSentLoadingBinding.inflate(layoutInflater, parent, false))
            RECEIVED_LOADING -> ChatMessageReceivedLoadingViewHolder(ChatMessageReceivedLoadingBinding.inflate(layoutInflater, parent, false))
            SENT_CANCELLED -> ChatMessageSentItemNotSentViewHolder(ChatMessageSentItemNotSentBinding.inflate(layoutInflater, parent, false))
            RECEIVED_CANCELLED -> ChatMessageReceivedItemNotReceivedViewHolder(ChatMessageReceivedItemNotReceivedBinding.inflate(layoutInflater, parent, false))
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

                holder.bind(message, download)
            }
            RECEIVED_TEXT -> {
                holder as ChatMessageReceivedItemTextViewHolder
                message as Message.Data

                holder.bind(message.text, message.date)
            }
            RECEIVED_IMAGE -> {
                holder as ChatMessageReceivedItemImageViewHolder
                message as Message.File

                holder.bind(toUri(message), message.date)
            }
            RECEIVED_UNKNOWN -> {
                holder as ChatMessageReceivedItemUnknownViewHolder
                message as Message.File

                holder.bind(message, download)
            }
            SENT_LOADING -> {
                holder as ChatMessageSentLoadingViewHolder
                message as Message.File

                holder.bind(message.progress, message.coroutineScope)
            }
            RECEIVED_LOADING -> {
                holder as ChatMessageReceivedLoadingViewHolder
                message as Message.File

                holder.bind(message.progress, message.coroutineScope)
            }
            RECEIVED_CANCELLED -> { /* EMPTY */ }
            SENT_CANCELLED -> { /* EMPTY */ }
        }
    }

    override fun getItemViewType(
        position: Int
    ): Int {
        val item = messages[position]
        return when (item.type) {
            Message.MESSAGE_TEXT_CODE -> if (item.isMessageReceived) RECEIVED_TEXT else SENT_TEXT
            Message.MESSAGE_IMAGE_CODE -> {
                item as Message.File
                if (item.isMessageReceived)
                    when (item.progress) {
                        Constants.END_PROGRESS -> RECEIVED_IMAGE
                        Constants.FAILED_TO_LOAD_PROGRESS -> RECEIVED_CANCELLED
                        else -> RECEIVED_LOADING
                    }
                else
                    when (item.progress) {
                        Constants.END_PROGRESS -> SENT_IMAGE
                        Constants.FAILED_TO_LOAD_PROGRESS -> SENT_CANCELLED
                        else -> SENT_LOADING
                    }
            }
            Message.MESSAGE_FILE_CODE -> {
                item as Message.File
                if (item.isMessageReceived)
                    when (item.progress) {
                        Constants.END_PROGRESS -> RECEIVED_UNKNOWN
                        Constants.FAILED_TO_LOAD_PROGRESS -> RECEIVED_CANCELLED
                        else -> RECEIVED_LOADING
                    }
                else
                    when (item.progress) {
                        Constants.END_PROGRESS -> SENT_UNKNOWN
                        Constants.FAILED_TO_LOAD_PROGRESS -> SENT_CANCELLED
                        else -> SENT_LOADING
                    }
            }
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
        const val SENT_LOADING = 7
        const val RECEIVED_LOADING = 8
        const val SENT_CANCELLED = 9
        const val RECEIVED_CANCELLED = 10
    }

}