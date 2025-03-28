package com.dbottillo.lifeos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    @GET("v1/pages/{id}")
    suspend fun getPage(@Path(value = "id") pageId: String): Response<NotionPage>

    @GET("v1/databases/{id}")
    suspend fun getDatabase(@Path(value = "id") databaseId: String): Response<NotionDatabase>

    @POST("v1/databases/{id}/query")
    suspend fun queryDatabase(
        @Path(value = "id") databaseId: String,
        @Body body: NotionBodyRequest
    ): Response<NotionDatabaseQueryResult>

    @POST("v1/pages")
    suspend fun addPage(
        @Body body: AddPageNotionBodyRequest
    ): Response<Any>

    @PATCH("v1/pages/{id}")
    suspend fun archivePage(@Path(value = "id") pageId: String, @Body body: ArchiveBodyRequest): Response<NotionPage>

    @PATCH("v1/pages/{id}")
    suspend fun updatePage(@Path(value = "id") pageId: String, @Body body: UpdateBodyRequest): Response<NotionPage>

    @PATCH("v1/pages/{id}")
    suspend fun updatePageV2(
        @Path(
        value = "id"
    ) pageId: String,
        @Body body: UpdatePropertiesBodyRequest
    ): Response<NotionPage>

    @GET("v1/blocks/{id}/children")
    suspend fun queryBlock(
        @Path(value = "id") blockId: String
    ): Response<NotionDatabaseBlockResult>
}
