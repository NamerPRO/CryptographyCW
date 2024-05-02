package ru.namerpro.nchat.di

import org.koin.dsl.module
import ru.namerpro.nchat.domain.api.interactor.ChatManagerInteractor
import ru.namerpro.nchat.domain.api.interactor.ChatsDatabaseInteractor
import ru.namerpro.nchat.domain.api.interactor.InitializedClientsInteractor
import ru.namerpro.nchat.domain.api.interactor.MessagesDatabaseInteractor
import ru.namerpro.nchat.domain.api.interactor.MessagesInteractor
import ru.namerpro.nchat.domain.api.interactor.SecretKeyInteractor
import ru.namerpro.nchat.domain.impl.ChatManagerInteractorImpl
import ru.namerpro.nchat.domain.impl.ChatsDatabaseInteractorImpl
import ru.namerpro.nchat.domain.impl.InitializedClientsInteractorImpl
import ru.namerpro.nchat.domain.impl.MessagesDatabaseInteractorImpl
import ru.namerpro.nchat.domain.impl.MessagesInteractorImpl
import ru.namerpro.nchat.domain.impl.SecretKeyInteractorImpl

val domainModule = module {

    single<InitializedClientsInteractor> {
        InitializedClientsInteractorImpl(
            initializedClientsRepository = get()
        )
    }

    single<ChatManagerInteractor> {
        ChatManagerInteractorImpl(
            chatManagerRepository = get()
        )
    }

    single<SecretKeyInteractor> {
        SecretKeyInteractorImpl(
            secretKeyRepository = get()
        )
    }

    single<MessagesInteractor> {
        MessagesInteractorImpl(
            messagesRepository = get(),
            databaseInteractor = get()
        )
    }

    single<MessagesDatabaseInteractor> {
        MessagesDatabaseInteractorImpl(
            messagesDatabaseRepository = get()
        )
    }

    single<ChatsDatabaseInteractor> {
        ChatsDatabaseInteractorImpl(
            chatsDatabaseRepository = get()
        )
    }

}