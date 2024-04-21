package ru.namerpro.nchat.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.namerpro.nchat.R
import ru.namerpro.nchat.databinding.FragmentChatBinding
import ru.namerpro.nchat.domain.model.ChatModel

class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null

    private val viewModel: ChatViewModel by viewModel()

    private var chat: ChatModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        if (chat == null) {
            val chatDataJson = requireArguments().getString(ARGS_CHAT)
            chat = Gson().fromJson(chatDataJson, ChatModel::class.java)
        }

        viewModel.observeChat().observe(viewLifecycleOwner) {
            render(it)
        }

        binding?.chatName?.text = chat?.name
        binding?.partner?.text = getString(R.string.chat_partner, chat?.partnerName ?: getString(R.string.chat_partner_error))
    }

    private fun render(
        state: ChatState
    ) {

    }

    companion object {

        private const val ARGS_CHAT = "chatData"

        fun createArgs(
            chat: ChatModel
        ): Bundle {
            return bundleOf(ARGS_CHAT to Gson().toJson(chat))
        }

    }

}