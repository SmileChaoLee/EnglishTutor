package com.smile.englishtutor.retrofit

import com.smile.englishtutor.models.AgentRequest
import com.smile.englishtutor.models.AgentResponse
import com.smile.englishtutor.models.NewAgentRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface RestApiInterface {
    @POST("agent/run")
    fun runAgentSync(@Body request: AgentRequest): Call<AgentResponse>
    @POST("agent/run/option")
    fun runAgentOptionSync(@Body request: AgentRequest, @Query("option") option: Int): Call<AgentResponse>
    @POST("new_run_agent")
    fun newRunAgentSync(@Body request: NewAgentRequest): Call<AgentResponse>
}