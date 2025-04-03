package com.seadox.fairprice.api.response

import com.google.gson.annotations.SerializedName
import com.seadox.fairprice.items.ProductItem

data class ProductResponse(
    @SerializedName("products") val products: List<ProductItem> = emptyList(),
    @SerializedName("nextKey") val nextKey: Int = 0
)
