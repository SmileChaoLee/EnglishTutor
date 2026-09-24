package com.smile.englishtutor.retrofit

import com.smile.englishtutor.models.AgentRequest
import com.smile.englishtutor.models.AgentResponse
import com.smile.englishtutor.models.NewAgentRequest
import com.smile.englishtutor.utilities.LogUtil
import retrofit2.Response

object RestApiSync {

    private const val TAG = "RestApiSync"
    private const val HTTP_OK = 200

    private fun getApiInstance(): RestApiInterface {
        LogUtil.d(TAG, "getApiInstance")
        return RetrofitClient.getRetrofit().create(RestApiInterface::class.java)
    }

    fun getAgentResponse(userPrompt: String, option: Int = 0): AgentResponse? {
        val logStr = "getAgentResponse"
        LogUtil.d(TAG, "$logStr.userPrompt = $userPrompt")
        val request = AgentRequest(userPrompt)
        val api = getApiInstance()
        var agentResponse: AgentResponse? = null
        try {
            val response: Response<AgentResponse> =
                if (option == 0) api.runAgentSync(request).execute()
                else api.runAgentOptionSync(request, option).execute()
            if (response.isSuccessful && response.code() == HTTP_OK) {
                agentResponse = response.body()
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", e)
        }
        LogUtil.i(TAG, "$logStr.agentResponse = $agentResponse")
        return agentResponse
    }

    fun getAgentResponse(
        userPrompt: String,
        option: Int = 0,
        history: List<Map<String, String>>
    ): AgentResponse? {
        val logStr = "getAgentResponse"
        LogUtil.i(TAG, "$logStr.userPrompt = $userPrompt")
        LogUtil.i(TAG, "$logStr.option = $option")
        LogUtil.i(TAG, "$logStr.history = $history")
        val request = NewAgentRequest(userPrompt, option, history)
        val api = getApiInstance()
        var agentResponse: AgentResponse? = null
        try {
            val response: Response<AgentResponse> =
                api.newRunAgentSync(request).execute()
            if (response.isSuccessful && response.code() == HTTP_OK) {
                agentResponse = response.body()
            }
        } catch (e: Exception) {
           LogUtil.e(TAG, "$logStr.Exception", e)
        }
        LogUtil.i(TAG, "$logStr.agentResponse = $agentResponse")
        return agentResponse
    }
}

