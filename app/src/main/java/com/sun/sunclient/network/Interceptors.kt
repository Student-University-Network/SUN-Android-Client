package com.sun.sunclient.network

import com.sun.sunclient.data.AppDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

// sets cookies from datastore to outgoing request
class AddCookieInterceptor(private val dataStore: AppDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request();
        val accessToken = runBlocking {
            dataStore.readAccessToken().first()
        }
        val cookies = runBlocking {
            dataStore.readCookieSet().first()
        }
        val newRequestBuilder =
            originalRequest.newBuilder().addHeader("Authorization", "Bearer $accessToken")
        for (cookie in cookies) {
            newRequestBuilder.addHeader("Cookie", cookie)
        }
        return chain.proceed(newRequestBuilder.build())
    }
}

// saves cookies in datastore from incoming responses
class SaveCookiesInterceptor(private val dataStore: AppDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            val cookies: HashSet<String> = HashSet()
            for (header in originalResponse.headers("Set-Cookie")) {
                cookies.add(header)
            }
            runBlocking {
                dataStore.saveCookieSet(cookies)
            }
        }
        return originalResponse
    }
}