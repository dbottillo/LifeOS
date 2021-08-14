package com.dbottillo.notionalert.di

import com.dbottillo.notionalert.ApiInterface
import com.dbottillo.notionalert.Navigator
import com.dbottillo.notionalert.NavigatorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideNavigator(): Navigator {
        return NavigatorImpl()
    }

    @Singleton
    @Provides
    fun provideApiService(): ApiInterface {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}
