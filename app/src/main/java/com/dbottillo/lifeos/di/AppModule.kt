package com.dbottillo.lifeos.di

import android.content.Context
import androidx.room.Room
import com.dbottillo.lifeos.network.ApiInterface
import com.dbottillo.lifeos.BuildConfig
import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.MIGRATION_6_7
import com.dbottillo.lifeos.notification.NotificationManager
import com.dbottillo.lifeos.notification.NotificationProvider
import com.dbottillo.lifeos.network.RefreshManager
import com.dbottillo.lifeos.network.RefreshProvider
import com.dbottillo.lifeos.feature.home.HomeStorage
import com.dbottillo.lifeos.feature.home.HomeStorageImpl
import com.dbottillo.lifeos.network.HeaderInterceptor
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideApiService(
        okHttpClient: Lazy<OkHttpClient>,
    ): ApiInterface {
        return Retrofit.Builder()
            .baseUrl("https://api.notion.com/")
            .client(okHttpClient.get())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        headerInterceptor: HeaderInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideHomeStorage(
        @ApplicationContext appContext: Context
    ): HomeStorage {
        return HomeStorageImpl(appContext)
    }

    @Singleton
    @Provides
    fun provideNotificationManager(
        @ApplicationContext appContext: Context
    ): NotificationProvider {
        return NotificationManager(appContext)
    }

    @Singleton
    @Provides
    fun provideRefreshManager(
        @ApplicationContext appContext: Context
    ): RefreshProvider {
        return RefreshManager(appContext)
    }

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "notion-alert-articles"
        ).addMigrations(MIGRATION_6_7)
            .build()
    }
}
