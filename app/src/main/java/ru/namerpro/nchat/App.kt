package ru.namerpro.nchat

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ru.namerpro.nchat.di.dataModule
import ru.namerpro.nchat.di.domainModule
import ru.namerpro.nchat.di.viewModelModule

class App : Application() {

    override fun onCreate() {
        System.setProperty(
            kotlinx.coroutines.DEBUG_PROPERTY_NAME,
            kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
        )

        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                dataModule,
                domainModule,
                viewModelModule
            )
        }
    }

}