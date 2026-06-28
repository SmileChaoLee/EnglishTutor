package com.smile.englishtutor.retrofit

import com.smile.englishtutor.utilities.LogUtil
import com.smile.smilelibraries.retrofit.Client
import retrofit2.Retrofit

object RetrofitClient {

    private const val TAG = "RetrofitClient"
    private const val CHAO_URL = "http://137.184.120.171:8001/"
    // private const val CHAO_URL = "http://10.0.2.2:8000/"    // Emulator for local

    fun getRetrofit(): Retrofit {
        val retrofit = Client.getInstance(
            CHAO_URL,
            connectTimeout = 60,
            readTimeout = 60,
            writeTimeout = 20)
        LogUtil.d(TAG, "getRetrofit.retrofit = $retrofit")
        return retrofit
    }
}