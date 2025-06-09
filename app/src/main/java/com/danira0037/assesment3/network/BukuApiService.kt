package com.danira0037.assesment3.network

import com.danira0037.assesment3.model.Buku
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

private const val BASE_URL = "https://6843ed6e71eb5d1be031ec13.mockapi.io/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface BukuApiService {
    @GET("perpustakaan/buku")
    suspend fun getBuku(): List<Buku>

    @POST("perpustakaan/buku")
    suspend fun addBuku(@Body buku: Buku): Buku

    @PUT("perpustakaan/buku/{id}")
    suspend fun updateBuku(
        @Path("id") id: String,
        @Body buku: Buku
    ):Buku

    @DELETE("perpustakaan/buku/{id}")
    suspend fun deleteBuku(
        @Path("id") id: String
        ):Buku
}

object BukuApi {
    val service: BukuApiService by lazy {
        retrofit.create(BukuApiService::class.java)
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }