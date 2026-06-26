package com.smile.englishtutor.models

import com.google.gson.annotations.SerializedName

// This sends 'user_prompt' to Python
data class AgentRequest(
    @SerializedName("user_prompt")
    val userPrompt: String
)
