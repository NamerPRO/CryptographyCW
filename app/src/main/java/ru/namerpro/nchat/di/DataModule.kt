package ru.namerpro.nchat.di

import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.namerpro.nchat.data.NetworkClient
import ru.namerpro.nchat.data.impl.ChatManagerRepositoryImpl
import ru.namerpro.nchat.data.impl.InitializedClientsRepositoryImpl
import ru.namerpro.nchat.data.impl.SecretKeyRepositoryImpl
import ru.namerpro.nchat.data.network.NChatServiceApi
import ru.namerpro.nchat.data.network.RetrofitNetworkClient
import ru.namerpro.nchat.domain.api.repository.ChatManagerRepository
import ru.namerpro.nchat.domain.api.repository.InitializedClientsRepository
import ru.namerpro.nchat.domain.api.repository.SecretKeyRepository
import java.util.concurrent.TimeUnit

val dataModule = module {

    single { Gson() }

    single<NChatServiceApi> {
        Retrofit.Builder()
            .baseUrl("http://192.168.79.99:8080/")
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

    single<SecretKeyRepository> {
        SecretKeyRepositoryImpl(
            networkClient = get()
        )
    }

}