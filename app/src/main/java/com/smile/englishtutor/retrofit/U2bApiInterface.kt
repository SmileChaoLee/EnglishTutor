package com.smile.englishtutor.retrofit

import com.smile.englishtutor.models.YouTubeVideo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface U2bApiInterface {

    @GET("api/U2b/{searchTerm}")
    fun searchVideos(
        @Path("searchTerm") searchTerm: String?
    ): Call<ArrayList<YouTubeVideo>>
}