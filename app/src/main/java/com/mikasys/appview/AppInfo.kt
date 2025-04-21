package com.mikasys.appview

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val installTime: Long,
    val isSystemApp: Boolean,
    val icon: Drawable?,
    val lastUpdateTime: Long // Add the last update time field
)