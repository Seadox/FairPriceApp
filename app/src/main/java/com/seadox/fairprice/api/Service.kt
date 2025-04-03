package com.seadox.fairprice.api

import com.seadox.fairprice.api.requests.CartRequest
import com.seadox.fairprice.api.requests.ProductsRequest
import com.seadox.fairprice.api.requests.SearchStoresRequest
import com.seadox.fairprice.api.response.ProductResponse
import com.seadox.fairprice.items.Cart
import com.seadox.fairprice.items.Chain
import com.seadox.fairprice.items.ProductItem
import com.seadox.fairprice.items.Store
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Service {
    @GET("api/stores")
    fun getStores(@Query("search") search: String): Call<List<Store>>

    @POST("api/stores")
    fun getStoresByIds(@Body request: SearchStoresRequest): Call<List<Store>>

    @GET("api/product")
    fun getProducts(@Query("code") code: String): Call<ProductItem>

    @GET("api/product")
    fun getProductsByName(
        @Query("name") name: String,
        @Query("nextKey") nextKey: Int,
        @Query("limit") limit: Int
    ): Call<ProductResponse>

    @POST("api/products")
    fun getProductsByIds(@Body ids: ProductsRequest): Call<List<ProductItem>>

    @POST("api/cart")
    fun getCart(@Body cart: CartRequest): Call<Cart>

    @GET("api/supported_chains")
    fun getSupportedChains(): Call<List<Chain>>
}