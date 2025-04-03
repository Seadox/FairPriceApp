package com.seadox.fairprice.items

import com.google.gson.annotations.SerializedName

data class Chain(
    @SerializedName("chainName") val chainName: String = "",
    @SerializedName("supported") val supported: Boolean = false,
)
