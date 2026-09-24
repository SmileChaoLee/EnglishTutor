package com.smile.englishtutor.models

import com.google.gson.annotations.SerializedName

// This sends 'user_prompt' to Python
data class NewAgentRequest(
    @SerializedName("user_prompt")
    val userPrompt: String,
    @SerializedName("option")
    val option: Int,
    @SerializedName("history_messages")
    val historyMessages: List<Map<String, String>>
)