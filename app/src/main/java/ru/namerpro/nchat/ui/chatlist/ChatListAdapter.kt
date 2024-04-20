package ru.namerpro.nchat.ui.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.namerpro.nchat.databinding.ChatItemBinding
import ru.namerpro.nchat.domain.model.ChatModel

class ChatListAdapter(
    val chats: ArrayList<ChatModel>,
    private val itemClickListener: ((ChatModel) -> Unit)
) : RecyclerView.Adapter<ChatListViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatListViewHolder {
        val layoutInspector = LayoutInflater.from(parent.context)
        return ChatListViewHolder(ChatItemBinding.inflate(layoutInspector, parent, false))
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(
        holder: ChatListViewHolder,
        position: Int
    ) {
        val chat = chats[position]
        holder.bind(chat.name)
        holder.itemView.setOnClickListener {
            itemClickListener.invoke(chat)
        }
    }

}