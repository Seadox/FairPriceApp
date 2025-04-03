package com.seadox.fairprice.items

import com.google.gson.annotations.SerializedName

data class Promo(
    @SerializedName("chainName") val chainName: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("endDate") val endDate: String = "",
    @SerializedName("promotionItems") val promotionItems: List<ProductItem> = emptyList(),
    @SerializedName("stores") val stores: List<Store> = emptyList(),
)
