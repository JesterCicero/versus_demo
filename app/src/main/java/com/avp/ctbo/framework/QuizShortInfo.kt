package com.avp.ctbo.framework

import com.google.gson.annotations.SerializedName
import com.avp.core.data.IQuizShortInfo

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