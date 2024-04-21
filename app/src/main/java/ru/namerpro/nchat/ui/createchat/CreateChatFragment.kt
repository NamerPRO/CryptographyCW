package ru.namerpro.nchat.ui.createchat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.namerpro.nchat.R
import ru.namerpro.nchat.commons.showDialog
import ru.namerpro.nchat.databinding.FragmentCreateChatBinding
import ru.namerpro.nchat.domain.model.ClientModel
import ru.namerpro.nchat.domain.model.State
import ru.namerpro.nchat.ui.root.RootViewModel.Companion.CLIENT_ID

class CreateChatFragment : Fragment() {

    private var binding: FragmentCreateChatBinding? = null

    private val viewModel: CreateChatViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateChatBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshPartners?.setOnClickListener {
            viewModel.getInitializedClients()
            Toast.makeText(requireContext(), getString(R.string.clients_updating), Toast.LENGTH_SHORT).show()
        }

        chatPreparationInitialization()

        viewModel.observeChatCreationState().observe(viewLifecycleOwner) {
            render(it)
        }

        binding?.createChat?.setOnClickListener {
            val chatName = binding?.chatName?.text?.toString() ?: ""
            val partnerName = binding?.selectPartner?.selectedItem?.toString()?.takeWhile { it != ' ' } ?: ""
            val cipherType = binding?.cipherType?.selectedItem?.toString()?.takeWhile { it != ' ' } ?: ""
            binding?.chatName?.setText("")
            viewModel.createChat(chatName, partnerName, cipherType)
        }

        if (viewModel.initializedClients.isNullOrEmpty()) {
            viewModel.getInitializedClients()
        } else {
            handleClientsInitialized(viewModel.initializedClients!!)
        }
    }

    private fun chatPreparationInitialization() {
        binding?.chatName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(str: CharSequence?, start: Int, count: Int, after: Int) { /* cannot be removed */ }
            override fun afterTextChanged(str: Editable?) { /* cannot be removed */ }

            override fun onTextChanged(
                searchText: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                viewModel.chatPreparationState = isChatCreatable()
                binding?.createChat?.isEnabled = viewModel.chatPreparationState is State.WithData
            }
        })

        binding?.selectPartner?.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.chatPreparationState = isChatCreatable()
                binding?.createChat?.isEnabled = viewModel.chatPreparationState is State.WithData
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }
    }

    private fun isChatCreatable(): State<Triple<String, String, String>> {
        val partnerName = binding?.selectPartner?.selectedItem?.toString()
        if (partnerName.isNullOrBlank() || partnerName == getString(R.string.no_clients_online) || partnerName == getString(R.string.failed_to_get_clients)) {
            return State.NotReady()
        }
        val chatName = binding?.chatName?.text?.toString()?.takeWhile { it != ' ' }
        if (chatName.isNullOrBlank()) {
            return State.NotReady()
        }
        val cipherType = binding?.cipherType?.selectedItem?.toString()?.takeWhile { it != ' ' } ?: STANDARD_CIPHER
        return State.WithData(Triple(chatName, cipherType, partnerName))
    }

    private fun render(
        state: ChatCreationState
    ) {
        when (state) {
            is ChatCreationState.InitializedClientsRequestSuccess -> handleClientsInitialized(state.clients)
            is ChatCreationState.InitializedClientsRequestFailed -> fillSpinnerWithClients(listOf(getString(R.string.failed_to_get_clients)))
            is ChatCreationState.FailedToCreateChat -> showDialog(requireContext(), getString(R.string.chat_creation_failed_title), getString(R.string.chat_creation_failed_message))
            is ChatCreationState.FailedToGetNewChats -> Toast.makeText(requireContext(), getString(R.string.failed_to_get_new_chats), Toast.LENGTH_SHORT).show()
            is ChatCreationState.FailedToAddNewChat -> Toast.makeText(requireContext(), getString(R.string.failed_to_add_new_chat), Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleClientsInitialized(
        clients: List<ClientModel>
    ) {
        val partners = if (clients.isEmpty()) {
            listOf(getString(R.string.no_clients_online))
        } else {
            clients.map {
                if (it.id == CLIENT_ID) {
                    it.name + " (Вы)"
                } else {
                    it.name
                }
            }.toList()
        }
        fillSpinnerWithClients(partners)
    }

    private fun fillSpinnerWithClients(
        partners: List<String>
    ) {
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partners)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.selectPartner?.adapter = spinnerAdapter
    }

    companion object {
        private const val STANDARD_CIPHER = "RC5"
    }

}