package com.dbottillo.notionalert.network

import com.dbottillo.notionalert.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader(AUTHORIZATION, "$BEARER_TOKEN_PREFIX ${BuildConfig.NOTION_KEY}")
        builder.addHeader("Notion-Version", "2021-08-16")
        return chain.proceed(builder.build())
    }

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val BEARER_TOKEN_PREFIX = "Bearer"
    }
}
