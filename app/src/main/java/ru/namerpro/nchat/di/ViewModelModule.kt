package ru.namerpro.nchat.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.namerpro.nchat.ui.chatlist.ChatListViewModel
import ru.namerpro.nchat.ui.createchat.CreateChatViewModel
import ru.namerpro.nchat.ui.root.RootViewModel

val viewModelModule = module {

    viewModel {
        CreateChatViewModel(
            initializedClientsInteractor = get(),
            chatManagerInteractor = get()
        )
    }

    viewModel {
        RootViewModel(
            chatManagerInteractor = get(),
            secretKeyInteractor = get(),
            initializedClientsInteractor = get()
        )
    }

    viewModel {
        ChatListViewModel()
    }

}