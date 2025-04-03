package com.seadox.fairprice.api.requests

import com.google.gson.annotations.SerializedName

data class CartRequest(
    @SerializedName("products") val products: List<String>,
    @SerializedName("stores") val stores: List<String>
)
