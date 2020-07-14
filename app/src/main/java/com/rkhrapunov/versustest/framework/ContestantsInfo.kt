package com.rkhrapunov.versustest.framework

import com.google.gson.annotations.SerializedName
import com.rkhrapunov.core.data.IContestantsInfo

data class ContestantsInfo (
    @SerializedName("name")
    override val name: String,
    @SerializedName("url")
    override val url: String,
    @SerializedName("owner")
    override val owner: String
) : IContestantsInfo