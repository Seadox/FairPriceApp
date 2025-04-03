package com.seadox.fairprice.items

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ProductItem(
    @SerializedName("priceUpdateDate") val priceUpdateDate: String = "",
    @SerializedName("itemCode") val itemCode: Long = 0,
    @SerializedName("manufacturerItemDescription") val manufacturerItemDescription: String = "",
    @SerializedName("itemPrice") val itemPrice: Double = 0.0,
    @SerializedName("itemType") val itemType: Int = 0,
    @SerializedName("manufacturerName") val manufacturerName: String = "",
    @SerializedName("manufactureCountry") val manufactureCountry: String = "",
    @SerializedName("chainId") val chainId: Long = 0,
    @SerializedName("allStoreIds") val allStoreIds: List<String> = emptyList(),
    @SerializedName("priceDifference") val priceDifference: Float = 0.0f,
    @SerializedName("cheapestPrice") val cheapestPrice: Double = 0.0,
    @SerializedName("mostExpensivePrice") val mostExpensivePrice: Double = 0.0,
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("allChainIds") val allChainIds: List<Long> = emptyList(),
    @SerializedName("chainName") val chainName: String = "",
    @SerializedName("isFavorite") var isFavorite: Boolean = false,
    @SerializedName("pricesInChains") val pricesInChains: List<ProductPriceInChain> = emptyList(),
)

@Serializable
data class ProductPriceInChain(
    @SerializedName("chainName") val chainName: String = "",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("promo") val promo: String? = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("storeName") val storeName: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("chainId") val chainId: Long = 0,
    @SerializedName("storeId") val storeId: String = "",
)

