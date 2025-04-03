package com.seadox.fairprice.items

import com.google.gson.annotations.SerializedName

data class Cart(
    @SerializedName("totalPrice") val totalPrice: Double = 0.0,
    @SerializedName("chains") val chains: Map<String, List<ProductItem>> = emptyMap()
)
