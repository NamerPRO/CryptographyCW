package ru.namerpro.nchat.ui.chatlist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.namerpro.nchat.R
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.commons.debounce
import ru.namerpro.nchat.databinding.FragmentChatListBinding
import ru.namerpro.nchat.domain.model.Chat
import ru.namerpro.nchat.ui.chat.ChatFragment

class ChatListFragment : Fragment() {

    private var binding: FragmentChatListBinding? = null

    private val viewModel: ChatListViewModel by viewModel()

    private var chatListAdapter: ChatListAdapter? = null

    private var clickDebounce: ((Chat) -> Unit)? = null

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

        clickDebounce = debounce(
            delayInMillis = Constants.CLICK_DEBOUNCE_DELAY,
            coroutineScope = viewLifecycleOwner.lifecycleScope,
            useLastParam = false
        ) {
            findNavController().navigate(
                R.id.action_chatListFragment_to_chatFragment2,
                ChatFragment.createArgs(it)
            )
        }

        chatListAdapter = ChatListAdapter(arrayListOf()) {
            clickDebounce?.invoke(it)
        }

        binding?.chatArea?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatListAdapter
        }

        viewModel.observeAvailableChats().observe(viewLifecycleOwner) {
            render(it)
        }

        viewModel.getChatsFromDb()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun render(
        state: ChatListState
    ) {
        when (state) {
            is ChatListState.UpdateChatList -> {
                viewModel.addChatsToDb(state.chats)
                chatListAdapter?.chats?.addAll(state.chats)
                chatListAdapter?.chats?.sortByDescending { it.id }
                chatListAdapter?.notifyDataSetChanged()
            }
            is ChatListState.ChatsRestoreFromDb -> {
                chatListAdapter?.chats?.addAll(state.chats)
                chatListAdapter?.notifyDataSetChanged()
            }
        }
    }

}