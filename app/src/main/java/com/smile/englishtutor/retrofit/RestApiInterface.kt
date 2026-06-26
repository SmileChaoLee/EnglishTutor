package com.smile.englishtutor.retrofit

import com.smile.englishtutor.models.AgentRequest
import com.smile.englishtutor.models.AgentResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RestApiInterface {
    @POST("agent/run")
    fun runAgentSync(@Body request: AgentRequest): Call<AgentResponse>
}