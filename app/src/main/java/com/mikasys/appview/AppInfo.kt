package com.mikasys.appview

import android.graphics.drawable.Drawable
import java.util.Date

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val isSystemApp: Boolean,
    val installDate: Date,
    val lastUpdateTime: Date,
    val size: Long,
    val versionName: String
) 