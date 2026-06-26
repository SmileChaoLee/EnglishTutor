package com.smile.englishtutor.models

import com.google.gson.annotations.SerializedName

// Receive 'agent_response' from Python
data class AgentResponse(
    @SerializedName("agent_response")
    val agentResponse: String
)