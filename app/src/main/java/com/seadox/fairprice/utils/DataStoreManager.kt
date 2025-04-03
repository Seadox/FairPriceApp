package com.seadox.fairprice.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.seadox.fairprice.models.DataStoreItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val PRODUCTS_DATASTORE = "product_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PRODUCTS_DATASTORE)

class DataStoreManager private constructor() {

    private lateinit var context: Context
    private var dataStoreItem: DataStoreItem = DataStoreItem()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: DataStoreManager? = null

        // Singleton instance initialization
        fun getInstance(): DataStoreManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStoreManager().also { INSTANCE = it }
            }
        }

        private val DATA_KEY = stringPreferencesKey("code")
    }

    fun init(context: Context) {
        if (::context.isInitialized) return
        this.context = context.applicationContext

        // Load data from DataStore
        CoroutineScope(Dispatchers.Main).launch {
            loadDataStore()
        }
    }

    private suspend fun loadDataStore() {
        // Load data from DataStore if it exists
        context.dataStore.data.collect { preferences ->
            val json = preferences[DATA_KEY]
            if (json != null) {
                dataStoreItem = Json.decodeFromString(json) // Deserialize into dataStoreItem
            }
        }
    }

    suspend fun saveToDataStore() {
        val json = Json.encodeToString(dataStoreItem)
        context.dataStore.edit {
            it[DATA_KEY] = json
        }
    }

    fun getDataStore(): Flow<DataStoreItem?> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[DATA_KEY]
            json?.let { Json.decodeFromString(it) }
        }
    }

    suspend fun clearDataStore() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun addStoreToDataStoreItem(code: String): DataStoreManager {
        dataStoreItem.stores.add(code)

        return this
    }

    fun addProductToDataStoreItem(code: String): DataStoreManager {
        dataStoreItem.products.add(code)

        return this
    }

    fun removeStoreFromDataStoreItem(code: String): DataStoreManager {
        dataStoreItem.stores.remove(code)

        return this
    }

    fun removeProductFromDataStoreItem(code: String): DataStoreManager {
        val id = code.split("_")[1]

        dataStoreItem.products.removeIf { it.split("_")[1] == id }
        
        return this
    }

    fun isProductInDataStoreItem(code: String): Boolean {
        val ids = dataStoreItem.products.map { it.split("_")[1] }
        return ids.contains(code)
    }

    fun isStoreInDataStoreItem(code: String): Boolean {
        return dataStoreItem.stores.contains(code)
    }

    fun getStores(): List<String> {
        return dataStoreItem.stores
    }

    fun getProducts(): List<String> {
        return dataStoreItem.products
    }
}