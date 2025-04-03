package com.seadox.fairprice.models

import kotlinx.serialization.Serializable

@Serializable
data class DataStoreItem(
    val stores: ArrayList<String> = ArrayList(),
    val products: ArrayList<String> = ArrayList()
)
