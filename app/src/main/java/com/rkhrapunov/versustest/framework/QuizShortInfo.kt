package com.rkhrapunov.versustest.framework

import com.google.gson.annotations.SerializedName
import com.rkhrapunov.core.data.IQuizShortInfo

data class QuizShortInfo(
    @SerializedName("quiz_title")
    override val title: String,
    @SerializedName("quiz_description")
    override val description: String,
    @SerializedName("url")
    override val url: String,
    @SerializedName("background_url")
    override val backgroundUrl: String
) : IQuizShortInfo