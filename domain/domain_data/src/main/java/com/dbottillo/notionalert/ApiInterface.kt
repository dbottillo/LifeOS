package com.dbottillo.notionalert

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    @GET("v1/pages/{id}")
    suspend fun getPage(@Path(value = "id") pageId: String): Response<NotionPage>

    @POST("v1/databases/{id}/query")
    suspend fun queryDatabase(
        @Path(value = "id") databaseId: String,
        @Body body: FilterRequest
    ): Response<NotionDatabase>
}
