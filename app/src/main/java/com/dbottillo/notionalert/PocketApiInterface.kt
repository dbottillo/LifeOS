package com.dbottillo.notionalert

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PocketApiInterface {

    @POST("v3/oauth/request")
    @FormUrlEncoded
    suspend fun oauthRequest(
        @Field(value = "consumer_key") consumerKey: String,
        @Field(value = "redirect_uri") redirectUri: String
    ): Response<ResponseBody>

    @POST("v3/oauth/authorize")
    @FormUrlEncoded
    suspend fun oauthAuthorize(
        @Field(value = "consumer_key") consumerKey: String,
        @Field(value = "code") code: String
    ): Response<ResponseBody>

    @GET("v3/get")
    suspend fun getArticles(
        @Query(value = "consumer_key") consumerKey: String,
        @Query(value = "access_token") accessToken: String
    ): Response<PocketGetResult>
}
