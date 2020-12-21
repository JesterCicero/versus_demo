package com.avp.ctbo.framework

import com.google.gson.annotations.SerializedName
import com.avp.core.data.ICategory

data class Category (
    @SerializedName("category_name")
    override val name: String,
    @SerializedName("category_url")
    override val url: String,
    @SerializedName("background_url")
    override val backgroundUrl: String
) : ICategory
