package ru.namerpro.nchat.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.namerpro.nchat.R
import ru.namerpro.nchat.commons.Constants.Companion.RC5_STANDARD_BLOCK_LENGTH_IN_BITS
import ru.namerpro.nchat.commons.Constants.Companion.RC5_STANDARD_ROUNDS_COUNT
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_KEY_SIZE_IN_BYTES
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_MAGENTA_PRIMITIVE_ELEMENT
import ru.namerpro.nchat.databinding.FragmentChatBinding
import ru.namerpro.nchat.domain.entities.ciphers.context.SymmetricEncrypterContext
import ru.namerpro.nchat.domain.entities.ciphers.context.encrypter.Encrypter
import ru.namerpro.nchat.domain.entities.ciphers.mode.Mode
import ru.namerpro.nchat.domain.entities.ciphers.padding.Padding
import ru.namerpro.nchat.domain.model.Chat
import ru.namerpro.nchat.domain.model.Cipher
import ru.namerpro.nchat.domain.model.Message
import ru.namerpro.nchat.ui.chat.recyclerview.ChatAdapter
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_NAME
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null

    private val viewModel: ChatViewModel by viewModel()

    private var _chat: Chat? = null
    private val chat: Chat
        get() = _chat!!

    private var encrypter: SymmetricEncrypterContext? = null

    private var messagesAdapter: ChatAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        if (_chat == null) {
            val chatDataJson = requireArguments().getString(ARGS_CHAT)
            _chat = Gson().fromJson(chatDataJson, Chat::class.java)
        }

        initializeEncrypter()

        messagesAdapter = ChatAdapter(arrayListOf()) {
            viewModel.getFileUri(it, encrypter)
        }

        binding?.chat?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messagesAdapter
        }

        val contentPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data?.data != null) {
                val pickedFileUri = it.data?.data!!
                val type = MimeTypeMap.getSingleton().getExtensionFromMimeType(requireContext().contentResolver.getType(pickedFileUri)) ?: ""
                val date = SimpleDateFormat("HH:mm (dd/MM/yyyy)", Locale.getDefault()).format(Date())
                requireActivity().contentResolver.openInputStream(pickedFileUri).use { input ->
                    if (input != null) {
                        val descriptor = requireContext().contentResolver.openAssetFileDescriptor(pickedFileUri, "r")
                        viewModel.sendFile(chat.id, input, descriptor?.length ?: 0, date, type, encrypter)
                        descriptor?.close()
                    } else {
                        // handle error here (show user a message)
                    }
                }
            }
        }

        binding?.attachResource?.setOnClickListener {
            contentPicker.launch(
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "file/*"
                }
            )
        }

        viewModel.observeChat().observe(viewLifecycleOwner) {
            render(it)
        }

        viewModel.getMessages(chat.id, encrypter)

        binding?.chatName?.text = chat.name
        binding?.partner?.text = getString(R.string.chat_partner, chat.partnerName)

        binding?.backToChatlist?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.sendMessageButton?.setOnClickListener {
            val messageText = binding?.messageArea?.text?.toString()
            binding?.messageArea?.setText("")
            if (messageText.isNullOrBlank()) {
                Toast.makeText(requireContext(), getString(R.string.invalid_message), Toast.LENGTH_SHORT).show()
            } else {
                val date = SimpleDateFormat("HH:mm (dd/MM/yyyy)", Locale.getDefault()).format(Date())
                viewModel.sendMessage(chat.id, messageText, encrypter)
                sendForClientItself(Message.Data(false, messageText, date, Message.MESSAGE_TEXT_CODE))
            }
        }

        binding?.chatName?.isSelected = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sendForClientItself(
        message: Message
    ) {
        messagesAdapter?.messages?.add(message)
        messagesAdapter?.notifyDataSetChanged()
        binding?.chat?.scrollToPosition(messagesAdapter?.messages?.size?.minus(1) ?: 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        encrypter?.close()
    }

    private fun initializeEncrypter() {
        encrypter = when (chat.cipher) {
            Cipher.RC5 -> SymmetricEncrypterContext(
                Encrypter.RC5,
                chat.key,
                Mode.CBC,
                Padding.PKCS7,
                chat.iv,
                RC5_STANDARD_BLOCK_LENGTH_IN_BITS,
                RC5_STANDARD_ROUNDS_COUNT,
                STANDARD_KEY_SIZE_IN_BYTES.toUByte()
            )
            Cipher.MAGENTA -> SymmetricEncrypterContext(
                Encrypter.MAGENTA,
                chat.key,
                Mode.CBC,
                Padding.PKCS7,
                chat.iv,
                STANDARD_MAGENTA_PRIMITIVE_ELEMENT
            )
        }
    }

    private fun render(
        state: ChatState
    ) {
        when (state) {
            is ChatState.SuccessfullySendFile -> {
                updateForClientItself(state.message)
                viewModel.removeTempFiles(listOf(state.message))
            }
            is ChatState.SuccessfullyGetMessages -> {
                updateMessages(state.messages)
                viewModel.removeTempFiles(state.messages)
            }
            is ChatState.SuccessfullySendMessage -> {

            }
            is ChatState.FailedToSendMessage -> {

            }
            is ChatState.FailedToGetMessages -> {

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateForClientItself(
        message: Message
    ) {
        messagesAdapter?.messages?.addAll(listOf(message))
        messagesAdapter?.notifyDataSetChanged()
        binding?.chat?.scrollToPosition(messagesAdapter?.messages?.size?.minus(1) ?: 0)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateMessages(
        messages: List<Message>
    ) {
        if (chat.partnerName != CLIENT_NAME && messages.isNotEmpty()) {
            messagesAdapter?.messages?.addAll(messages)
            messagesAdapter?.notifyDataSetChanged()
            if (messagesAdapter?.messages?.isNotEmpty() == true) {
                binding?.chat?.scrollToPosition(messagesAdapter?.messages?.size?.minus(1) ?: 0)
            }
        }
    }

    companion object {

        private const val ARGS_CHAT = "chatData"

        fun createArgs(
            chat: Chat
        ): Bundle {
            return bundleOf(ARGS_CHAT to Gson().toJson(chat))
        }

    }

}