package com.rkhrapunov.versustest.framework

import com.google.gson.annotations.SerializedName
import com.rkhrapunov.core.data.IContestantsStatsInfo

data class ContestantsStatsInfo (
    @SerializedName("name")
    override val name: String,
    @SerializedName("min_url")
    override val minUrl: String,
    @SerializedName("votes")
    override val votes: Int,
    @SerializedName("owner")
    override val owner: String,
    @SerializedName("percentage")
    override val percentage: String
) : IContestantsStatsInfo