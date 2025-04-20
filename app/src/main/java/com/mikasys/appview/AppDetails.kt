package com.mikasys.appview

import android.graphics.drawable.Drawable
import java.util.Date

data class AppDetails(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable,
    val sourcePath: String,
    val isSystemApp: Boolean,
    val installDate: Date,
    val updateDate: Date,
    val sharedUserId: String?,
    val uid: Int,
    val sizes: AppSizes
)

data class AppSizes(
    val codeSize: Long,
    val dataSize: Long,
    val cacheSize: Long,
    val externalDataSize: Long,
    val externalCacheSize: Long,
    val obbSize: Long,
    val mediaSize: Long
)