package com.avp.ctbo.framework

import com.google.gson.annotations.SerializedName
import com.avp.core.data.IContestantsInfo

data class ContestantsInfo (
    @SerializedName("name")
    override val name: String,
    @SerializedName("url")
    override val url: String,
    @SerializedName("min_url")
    override val minUrl: String,
    @SerializedName("short_description")
    override val shortDescription: String
) : IContestantsInfo