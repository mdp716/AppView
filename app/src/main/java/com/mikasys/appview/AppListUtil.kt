// AppListUtil.kt
package com.mikasys.appview

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppListUtil {

    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val appList = mutableListOf<AppInfo>()

        val installedPackages = try {
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            Log.e("AppListUtil", "Error getting installed packages", e)
            return emptyList() // Return an empty list if retrieval fails
        }

        for (packageInfo in installedPackages) {
            val applicationInfo = packageInfo.applicationInfo
            if (applicationInfo != null) {
                try {
                    val appName = applicationInfo.loadLabel(packageManager).toString()
                    val packageName = packageInfo.packageName
                    val versionName = packageInfo.versionName ?: "Unknown"
                    val versionCode = packageInfo.longVersionCode

                    // Get install time
                    val installTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime
                    // Get last update time
                    val lastUpdateTime = packageManager.getPackageInfo(packageName, 0).lastUpdateTime

                    val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                    // Load the application icon
                    val icon: Drawable? = try {
                        applicationInfo.loadIcon(packageManager)
                    } catch (e: Exception) {
                        Log.e("AppListUtil", "Error loading icon for $packageName", e)
                        // You might want a default placeholder icon here from context.getDrawable(...)
                        null
                    }

                    val appInfo = AppInfo(
                        appName,
                        packageName,
                        versionName,
                        versionCode,
                        installTime,
                        isSystemApp,
                        icon,
                        lastUpdateTime
                    )
                    appList.add(appInfo)
                } catch (e: Exception) {
                    Log.e("AppListUtil", "Error processing package ${packageInfo.packageName}", e)
                    // Skip this app if any error occurs during processing its details
                }

            } else {
                Log.w("AppListUtil", "Skipping package ${packageInfo.packageName} because ApplicationInfo is null.")
            }
        }
        // Optional: Sort the list here, e.g., by app name
        // appList.sortBy { it.appName.lowercase() }
        return appList
    }

    // Helper function to format the date using MM/dd/yyyy
    fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    // Helper function to format the date and time using MM/dd/yyyy
    fun formatDateTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }
}