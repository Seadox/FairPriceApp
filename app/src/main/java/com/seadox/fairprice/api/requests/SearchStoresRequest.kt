package com.seadox.fairprice.api.requests

import com.google.gson.annotations.SerializedName

data class SearchStoresRequest(
    @SerializedName("ids") val ids: List<String> = emptyList()
)
