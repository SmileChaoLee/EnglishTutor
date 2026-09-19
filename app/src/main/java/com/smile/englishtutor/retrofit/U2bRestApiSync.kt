package com.smile.englishtutor.retrofit

import com.smile.englishtutor.models.YouTubeVideo

import com.smile.englishtutor.utilities.LogUtil
import retrofit2.Response

object U2bRestApiSync {

    private const val TAG = "U2bRestApiSync"
    private const val HTTP_OK = 200

    private fun getApiInstance(): U2bApiInterface {
        LogUtil.d(TAG, "getApiInstance")
        return U2bRetrofitClient.getRetrofit().create(U2bApiInterface::class.java)
    }

    fun getVideos(searchTerm: String):  ArrayList<YouTubeVideo> {
        val logStr = "getVideos"
        LogUtil.d(TAG, "$logStr.searchTerm = $searchTerm")
        var result = ArrayList<YouTubeVideo>()
        try {
            val response: Response<ArrayList<YouTubeVideo>> = getApiInstance()
                .searchVideos(searchTerm).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) {
                result = response.body() ?: ArrayList()
            }
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return result
    }
}

