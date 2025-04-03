package com.seadox.fairprice.api.requests

import com.google.gson.annotations.SerializedName

data class ProductsRequest(
    @SerializedName("products") val products: List<String>
)
