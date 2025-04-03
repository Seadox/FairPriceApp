package com.seadox.fairprice.items

import com.google.gson.annotations.SerializedName

data class StorePrice(
    @SerializedName("name") val name: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("price") val price: String = "",
)
