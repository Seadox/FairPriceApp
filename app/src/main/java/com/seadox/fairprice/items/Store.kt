package com.seadox.fairprice.items

import com.google.gson.annotations.SerializedName

data class Store(
    @SerializedName("chainid") val chainId: Long = 0,
    @SerializedName("storeid") val storeId: Int = 0,
    @SerializedName("storetype") val storeType: Int = 0,
    @SerializedName("storename") val storeName: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("chainname") val chainName: String = "",
)
