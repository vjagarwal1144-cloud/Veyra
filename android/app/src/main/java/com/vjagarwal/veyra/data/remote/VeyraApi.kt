package com.vjagarwal.veyra.data.remote

import com.vjagarwal.veyra.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VeyraApi {
    @GET("health")
    suspend fun health(): Map<String, Any?>

    @GET("api/maps/geocode")
    suspend fun geocode(@Query("address") address: String): Any

    @POST("api/maps/search")
    suspend fun placesSearch(@Body body: Map<String, Any?>): Any

    @POST("api/maps/route")
    suspend fun route(@Body body: Map<String, Any?>): Any

    @GET("api/trains/{number}")
    suspend fun train(@Path("number") number: String): Any

    companion object {
        fun create(): VeyraApi = Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL.trimEnd('/') + "/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(VeyraApi::class.java)
    }
}
