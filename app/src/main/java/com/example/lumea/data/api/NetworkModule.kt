package com.example.lumea.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val connectionApi: ConnectionApi = retrofit.create(ConnectionApi::class.java)
    val locationApi: LocationApi = retrofit.create(LocationApi::class.java)
//    val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)

    fun getProfileImageUrl(path: String): String {
        return "${BASE_URL}uploads/profile-picture/$path"
    }

    fun provideUserApi(): UserApi {
        return retrofit.create(UserApi::class.java)
    }

}