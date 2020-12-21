package com.avp.ctbo.framework

import com.google.gson.annotations.SerializedName
import com.avp.core.data.ISuperCategory

data class SuperCategory(
    @SerializedName("category_name")
    override val name: String,
    @SerializedName("category_url")
    override val url: String,
    @SerializedName("background_url")
    override val backgroundUrl: String
) : ISuperCategory