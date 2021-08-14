package com.dbottillo.notionalert.feature.home

import com.dbottillo.notionalert.ApiInterface
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.Todo
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val api: ApiInterface
) {

    @Suppress("TooGenericExceptionCaught")
    suspend fun get(): ApiResult<Todo> {
        return try {
            val response = api.getTodo(TODO_ID)
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

private const val TODO_ID = 5
