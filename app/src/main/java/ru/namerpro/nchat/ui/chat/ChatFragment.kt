package ru.namerpro.nchat.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.namerpro.nchat.R
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.commons.Constants.Companion.MAXIMUM_UPLOAD_AMOUNT_AT_THE_SAME_TIME
import ru.namerpro.nchat.commons.Constants.Companion.RC5_STANDARD_BLOCK_LENGTH_IN_BITS
import ru.namerpro.nchat.commons.Constants.Companion.RC5_STANDARD_ROUNDS_COUNT
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_KEY_SIZE_IN_BYTES
import ru.namerpro.nchat.commons.Constants.Companion.STANDARD_MAGENTA_PRIMITIVE_ELEMENT
import ru.namerpro.nchat.commons.composedImageUri
import ru.namerpro.nchat.commons.getContentType
import ru.namerpro.nchat.commons.getFileType
import ru.namerpro.nchat.commons.parentPath
import ru.namerpro.nchat.commons.showDialog
import ru.namerpro.nchat.commons.showExitDialog
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
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.DOWNLOAD_NOTINIFCTION_ID
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

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

        viewModel.getMessagesFromDatabase(chat.id)

        initializeEncrypter()

        val notificationChannel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = Constants.NOTIFICATION_CHANNEL_DESCRIPTION
        }

        val notificationManager = (requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).also {
            it.createNotificationChannel(notificationChannel)
        }

        val fileDownloadProgressNotificationBuilder = NotificationCompat.Builder(requireContext(), Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_no_chat_icon)
            .setDefaults(0)
            .setOngoing(true)
            .setAutoCancel(true)

        val toUri: (Message.File) -> Uri = {
            composedImageUri(it.devicePath ?: "", requireActivity().application)
        }
        val download: (Message.File) -> Unit = {
            val currentNotificationId = DOWNLOAD_NOTINIFCTION_ID++
            val downloadFolderPath = "${Environment.getExternalStorageDirectory()}${File.separator}${Environment.DIRECTORY_DOWNLOADS}${File.separator}${Constants.DOWNLOADS_FOLDER_NAME}${File.separator}"
            var totalProgress = 0.0
            viewModel.downloadOnClick(it, downloadFolderPath, encrypter) { it2 ->
                totalProgress = min(totalProgress + it2, Constants.END_PROGRESS)
                if (abs(Constants.END_PROGRESS - totalProgress) < Constants.EPSILON) {
                    Thread.sleep(Constants.NOTIFICATION_UPDATE_TIME)
                    val notification = fileDownloadProgressNotificationBuilder
                        .setContentTitle(getString(R.string.download_notification_title_on_load_end))
                        .setContentText(getString(R.string.download_notification_text_on_load_end, it.realName, Constants.DOWNLOADS_FOLDER_NAME))
                        .build()
                    notificationManager.notify(currentNotificationId, notification)
                } else {
                    val notification = fileDownloadProgressNotificationBuilder
                        .setOngoing(false)
                        .setContentTitle(getString(R.string.download_notification_title, it.realName))
                        .setContentText(getString(R.string.download_notification_text, totalProgress.toInt()))
                        .build()
                    notificationManager.notify(currentNotificationId, notification)
                }
            }
        }
        messagesAdapter = ChatAdapter(arrayListOf(), toUri, download)

        binding?.chat?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messagesAdapter
        }

        val contentPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data?.data != null) {
                val pickedFileUri = it.data?.data!!
                val date = SimpleDateFormat("HH:mm (dd/MM/yyyy)", Locale.getDefault()).format(Date())
                val pickedFileInfo = getPickedFileNameAndSize(pickedFileUri)
                val inputStream = requireActivity().contentResolver.openInputStream(pickedFileUri)
                val fileMessage = createAndDisplayFileObject(null, false, date, pickedFileInfo.second, Pair(pickedFileInfo.first, inputStream))
                ++viewModel.sendingFilesCounter
                viewModel.sendFile(chat.id, fileMessage, encrypter) { addition ->
                    activity?.runOnUiThread {
                        fileMessage.progress = min(fileMessage.progress + addition, Constants.END_PROGRESS)
                        messagesAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }

        binding?.attachResource?.setOnClickListener {
            if (viewModel.sendingFilesCounter < MAXIMUM_UPLOAD_AMOUNT_AT_THE_SAME_TIME) {
                contentPicker.launch(
                    Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                    }
                )
            } else {
                showDialog(requireContext(), getString(R.string.too_many_files_uploaded_title), getString(R.string.too_many_files_uploaded_text))
            }
        }

        viewModel.observeChat().observe(viewLifecycleOwner) {
            render(it)
        }

        viewModel.getMessages(chat.id, encrypter)

        binding?.chatName?.text = chat.name
        binding?.partner?.text = getString(R.string.chat_partner, chat.partnerName)

        binding?.backToChatlist?.setOnClickListener {
            alertIfNotAllFilesLoaded()
        }

        binding?.sendMessageButton?.setOnClickListener {
            val messageText = binding?.messageArea?.text?.toString()
            binding?.messageArea?.setText("")
            if (messageText.isNullOrBlank()) {
                Toast.makeText(requireContext(), getString(R.string.invalid_message), Toast.LENGTH_SHORT).show()
            } else {
                val date = SimpleDateFormat("HH:mm (dd/MM/yyyy)", Locale.getDefault()).format(Date())
                val message = Message.Data(messageText, date, true, Message.MESSAGE_TEXT_CODE)
                viewModel.sendMessage(chat.id, message, encrypter)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                alertIfNotAllFilesLoaded()
            }

        })

        binding?.chatName?.isSelected = true
    }

    private fun alertIfNotAllFilesLoaded() {
        if (viewModel.sendingFilesCounter > 0) {
            showExitDialog(requireContext(), getString(R.string.are_you_sure_title), getString(R.string.are_you_sure_text)) {
                findNavController().navigateUp()
            }
        } else {
            findNavController().navigateUp()
        }
    }

    private fun getPickedFileNameAndSize(
        pickedFileUri: Uri
    ): Pair<Long, String> {
        val cursor = requireContext().contentResolver.query(pickedFileUri, null, null, null, null)
        val name: String
        val size: Long
        if (cursor != null) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            name = cursor.getString(nameIndex)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            size = cursor.getLong(sizeIndex)
        } else {
            name = ""
            size = 0
        }
        cursor?.close()
        return Pair(size, name)
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

    private fun scrollToBottom() {
        binding?.chat?.scrollToPosition(messagesAdapter?.messages?.size?.minus(1) ?: 0)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun render(
        state: ChatState
    ) {
        when (state) {
            is ChatState.SuccessfullyGetMessages -> onSuccessfulMessageGet(state.messages)
            is ChatState.SuccessfullySendMessage -> onSuccessfullySendMessage(state.message)
            is ChatState.FailedToSendMessage -> Toast.makeText(requireContext(), getString(R.string.failed_to_send_message), Toast.LENGTH_SHORT).show()
            is ChatState.FailedToGetMessages -> Toast.makeText(requireContext(), getString(R.string.failed_to_get_messages), Toast.LENGTH_SHORT).show()
            is ChatState.SuccessfullySendFile -> onSuccessfullySendFile(state.fileMessage)
            is ChatState.SuccessfullyDownloadedFile -> onSuccessfullyDownloadedFile(state.fileMessage)
            is ChatState.FileNotSent -> onFileNotSent(state.fileMessage)
            is ChatState.FileNotReceived -> onFileNotReceived(state.fileMessage)
            is ChatState.FileAlreadyDownloadedOnClick -> Toast.makeText(requireContext(), getString(R.string.file_already_downloaded_on_click), Toast.LENGTH_SHORT).show()
            is ChatState.FileDownloadFailedOnClick -> Toast.makeText(requireContext(), getString(R.string.file_download_failed_on_click), Toast.LENGTH_SHORT).show()
            is ChatState.MessagesSuccessfullyLoadedFromDb -> showMessages(state.messages)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onFileNotReceived(
        fileMessage: Message.File
    ) {
        fileMessage.progress = Constants.FAILED_TO_LOAD_PROGRESS
        messagesAdapter?.notifyDataSetChanged()
        Toast.makeText(requireContext(), getString(R.string.download_cancelled), Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onFileNotSent(
        fileMessage: Message.File
    ) {
        fileMessage.progress = Constants.FAILED_TO_LOAD_PROGRESS
        messagesAdapter?.notifyDataSetChanged()
        Toast.makeText(requireContext(), getString(R.string.upload_cancelled), Toast.LENGTH_SHORT).show()
        --viewModel.sendingFilesCounter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onSuccessfullySendFile(
        fileMessage: Message.File
    ) {
        fileMessage.progress = Constants.END_PROGRESS
        messagesAdapter?.notifyDataSetChanged()
        --viewModel.sendingFilesCounter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onSuccessfullyDownloadedFile(
        fileMessage: Message.File
    ) {
        fileMessage.progress = Constants.END_PROGRESS
        messagesAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun createAndDisplayFileObject(
        devicePath: String?,
        isReceived: Boolean,
        date: String,
        realName: String,
        fileInfo: Pair<Long, InputStream?>?
    ): Message.File {
        val message = Message.File(devicePath, realName, date, isReceived, getContentType(getFileType(realName)), fileInfo, Constants.START_PROGRESS)
        messagesAdapter?.messages?.add(message)
        messagesAdapter?.notifyDataSetChanged()
        scrollToBottom()
        return message
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onSuccessfullySendMessage(
        message: Message.Data
    ) {
        viewModel.addMessagesToDataBase(chat.id, listOf(message))
        messagesAdapter?.messages?.add(message)
        messagesAdapter?.notifyDataSetChanged()
        viewModel.removeTempFiles(listOf(message))
        scrollToBottom()
    }

    private fun showMessages(
        messages: List<Message>
    ) {
        messages.forEach {
            when (it.type) {
                Message.MESSAGE_TEXT_CODE -> displayTextMessage(it as Message.Data)
                Message.MESSAGE_IMAGE_CODE -> displayImageMessage(it as Message.Data)
                Message.MESSAGE_FILE_CODE -> displayFile(it as Message.Data)
                Message.MESSAGE_CONVERSATION_END_CODE -> endConversation()
            }
        }
        viewModel.removeTempFiles(messages)
        scrollToBottom()
    }

    private fun onSuccessfulMessageGet(
        messages: List<Message>
    ) {
        if (chat.partnerName != CLIENT_NAME && messages.isNotEmpty()) {
            viewModel.addMessagesToDataBase(chat.id, messages)
            showMessages(messages)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayFile(
        message: Message.Data
    ) {
        val names = message.text.split('|', limit = 2)
        val devicePath = "${Environment.DIRECTORY_DOWNLOADS}${File.separator}${names[0]}"
        val fileMessage = Message.File(devicePath, names[1], message.date, message.isReceived, message.contentType, null, Constants.END_PROGRESS)
        messagesAdapter?.messages?.add(fileMessage)
        messagesAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayTextMessage(
        message: Message.Data
    ) {
        messagesAdapter?.messages?.add(message)
        messagesAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun endConversation() {
        messagesAdapter?.messages?.add(Message.ChatEnd)
        messagesAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayImageMessage(
        message: Message.Data
    ) {
        val names = message.text.split('|', limit = 2)
        val devicePath = "${requireActivity().externalCacheDir}${File.separator}${names[0]}"
        val fileMessage = createAndDisplayFileObject(devicePath, message.isReceived, message.date, names[1], null)
        viewModel.downloadFile(fileMessage, parentPath(fileMessage.devicePath), encrypter) { addition ->
            activity?.runOnUiThread {
                fileMessage.progress = min(fileMessage.progress + addition, Constants.END_PROGRESS)
                messagesAdapter?.notifyDataSetChanged()
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