package ru.namerpro.nchat.di

import androidx.room.Room
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.namerpro.nchat.commons.Constants.Companion.API_BASE_URL
import ru.namerpro.nchat.commons.Constants.Companion.CHATS_DATABASE
import ru.namerpro.nchat.commons.Constants.Companion.MESSAGES_DATABASE
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.db.ChatsDatabase
import ru.namerpro.nchat.data.db.MessagesDatabase
import ru.namerpro.nchat.data.impl.ChatManagerRepositoryImpl
import ru.namerpro.nchat.data.impl.ChatsDatabaseRepositoryImpl
import ru.namerpro.nchat.data.impl.InitializedClientsRepositoryImpl
import ru.namerpro.nchat.data.impl.MessagesDatabaseRepositoryImpl
import ru.namerpro.nchat.data.impl.MessagesRepositoryImpl
import ru.namerpro.nchat.data.impl.SecretKeyRepositoryImpl
import ru.namerpro.nchat.data.network.NChatServiceApi
import ru.namerpro.nchat.data.network.RetrofitNetworkClient
import ru.namerpro.nchat.domain.api.repository.ChatManagerRepository
import ru.namerpro.nchat.domain.api.repository.ChatsDatabaseRepository
import ru.namerpro.nchat.domain.api.repository.InitializedClientsRepository
import ru.namerpro.nchat.domain.api.repository.MessagesDatabaseRepository
import ru.namerpro.nchat.domain.api.repository.MessagesRepository
import ru.namerpro.nchat.domain.api.repository.SecretKeyRepository

val dataModule = module {

    single { Gson() }

    single<NChatServiceApi> {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NChatServiceApi::class.java)
    }

    single<InitializedClientsRepository> {
        InitializedClientsRepositoryImpl(
            networkClient = get()
        )
    }

    single<NetworkClient> {
        RetrofitNetworkClient(
            nChatServiceApi = get()
        )
    }

    single<ChatManagerRepository> {
        ChatManagerRepositoryImpl(
            networkClient = get()
        )
    }

    single {
        Room.databaseBuilder(androidContext(), MessagesDatabase::class.java, MESSAGES_DATABASE)
            .build()
    }

    single<SecretKeyRepository> {
        SecretKeyRepositoryImpl(
            networkClient = get()
        )
    }

    single<MessagesRepository> {
        MessagesRepositoryImpl(
            networkClient = get()
        )
    }

    single<MessagesDatabaseRepository> {
        MessagesDatabaseRepositoryImpl(
            messagesDatabase = get()
        )
    }

    single {
        Room.databaseBuilder(androidContext(), ChatsDatabase::class.java, CHATS_DATABASE)
            .build()
    }

    single<ChatsDatabaseRepository> {
        ChatsDatabaseRepositoryImpl(
            chatsDatabase = get()
        )
    }

}