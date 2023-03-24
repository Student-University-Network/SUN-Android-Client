package com.sun.sunclient.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.sun.sunclient.config.Config
import com.sun.sunclient.data.AppDataStore
import com.sun.sunclient.network.repository.AuthRepository
import com.sun.sunclient.network.repository.UserRepository
import com.sun.sunclient.network.service.AuthApiService
import com.sun.sunclient.network.service.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppApi {

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(dataStore: AppDataStore): OkHttpClient {
        return OkHttpClient().newBuilder()
            .addInterceptor(AddCookieInterceptor(dataStore))
            .addInterceptor(SaveCookiesInterceptor(dataStore))
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(Config.API_BASE_URL)
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideAuthRepository(api: AuthApiService, dataStore: AppDataStore) : AuthRepository {
        return AuthRepository(api, dataStore)
    }

    @Singleton
    @Provides
    fun provideUserApiService(retrofit: Retrofit) : UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideUserRepository(api: UserApiService) : UserRepository {
        return UserRepository(api)
    }
}