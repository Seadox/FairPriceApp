package com.seadox.fairprice

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.seadox.fairprice.utils.DataStoreManager

class App : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        DataStoreManager.getInstance().init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader(this).newBuilder()
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.1)
                    .strongReferencesEnabled(true)
                    .build()
            }
//            .logger(DebugLogger())
            .build()
    }
}