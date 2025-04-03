package com.seadox.fairprice.api

import com.seadox.fairprice.api.requests.CartRequest
import com.seadox.fairprice.api.requests.ProductsRequest
import com.seadox.fairprice.api.requests.SearchStoresRequest
import com.seadox.fairprice.api.response.ProductResponse
import com.seadox.fairprice.items.Cart
import com.seadox.fairprice.items.Chain
import com.seadox.fairprice.items.ProductItem
import com.seadox.fairprice.items.Store
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Client {
    private const val BASE_URL = "http://158.178.129.34/fairprice/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val service: Service by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Service::class.java)
    }

    private fun <T> executeCall(
        call: Call<T>,
        successCallback: (T) -> Unit,
        failureCallback: (String) -> Unit
    ) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful && response.body() != null) {
                    successCallback(response.body()!!)
                } else {
                    failureCallback("Request failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                failureCallback("Request failed: ${t.message}")
            }
        })
    }

    // Refactored methods
    fun getStores(
        query: String,
        successCallback: (List<Store>) -> Unit,
        failureCallback: (String) -> Unit
    ) {
        executeCall(service.getStores(query), successCallback, failureCallback)
    }

    fun getStoresByIds(
        ids: SearchStoresRequest,
        successCallback: (List<Store>) -> Unit,
        failureCallback: (String) -> Unit
    ) {
        executeCall(service.getStoresByIds(ids), successCallback, failureCallback)
    }

    fun getProductByCode(
        code: String,
        successCallback: (ProductItem) -> Unit,
        failureCallback: (String) -> Unit
    ) {
        executeCall(service.getProducts(code), successCallback, failureCallback)
    }

    fun getProductsByName(
        name: String,
        nextKey: Int,
        limit: Int,
        successCallback: (ProductResponse) -> Unit,
        failureCallback: (String) -> Unit
    ) {
        executeCall(
            service.getProductsByName(name, nextKey, limit),
            successCallback,
            failureCallback
        )
    }

    fun getProductsByIds(
        ids: ProductsRequest,
        successCallback: (List<ProductItem>) -> Unit,
        failureCallback: (String) -> Unit
    ) {
        executeCall(service.getProductsByIds(ids), successCallback, failureCallback)
    }

    fun getCart(
        cartRequest: CartRequest,
        successCallback: (Cart) -> Unit,
        failureCallback: (String) -> Unit
    ) {
        executeCall(service.getCart(cartRequest), successCallback, failureCallback)
    }

    fun getSupportedChains(
        successCallback: (List<Chain>) -> Unit,
        failureCallback: (String) -> Unit
    ) {
        executeCall(service.getSupportedChains(), successCallback, failureCallback)
    }
}