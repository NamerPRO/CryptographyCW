package ru.namerpro.nchat.ui.chatlist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.namerpro.nchat.databinding.FragmentChatListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.namerpro.nchat.domain.model.ChatModel

class ChatListFragment : Fragment() {

    private var binding: FragmentChatListBinding? = null

    private val viewModel: ChatListViewModel by viewModel()

    private var chatListAdapter: ChatListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        chatListAdapter = ChatListAdapter(arrayListOf()) { chat ->
            // some code here on click
        }

        binding?.chatArea?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatListAdapter
        }

        viewModel.observeAvailableChats().observe(viewLifecycleOwner) {
            render(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun render(
        state: ChatListState
    ) {
        when (state) {
            is ChatListState.UpdateChatList -> {
                if (chatListAdapter?.chats != state.chats) {
                    chatListAdapter?.chats?.clear()
                    chatListAdapter?.chats?.addAll(state.chats)
                    chatListAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

}