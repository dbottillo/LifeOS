package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.ApiInterface
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.NotionPage
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val api: ApiInterface
) {

    @Suppress("TooGenericExceptionCaught")
    suspend fun makeNetworkRequest(): ApiResult<NotionPage> {
        return try {
            val response = api.getPage(MAIN_NOTION_PAGE_ID)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return ApiResult.Success(body)
            }
            ApiResult.Error(Throwable("${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            ApiResult.Error(Throwable(e.message ?: e.toString()))
        }
    }
}

private const val MAIN_NOTION_PAGE_ID = "4be491b5ee164e299fa1f819825732be"

sealed class AppState {
    object Waiting : AppState()
    object Loading : AppState()
    object Data : AppState()
}
