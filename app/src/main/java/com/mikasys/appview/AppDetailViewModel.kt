package com.mikasys.appview

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

class AppDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val _appDetails = MutableLiveData<AppDetails>()
    val appDetails: LiveData<AppDetails> = _appDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val packageManager: PackageManager = application.packageManager

    suspend fun loadAppDetails(packageName: String) {
        _isLoading.value = true
        try {
            withContext(Dispatchers.IO) {
                val packageInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_META_DATA or
                            PackageManager.GET_SHARED_LIBRARY_FILES
                )

                val applicationInfo = packageInfo.applicationInfo
                    ?: throw PackageManager.NameNotFoundException("ApplicationInfo is null")

                val sizes = calculateAppSizes(applicationInfo)

                withContext(Dispatchers.Main) {
                    _appDetails.value = AppDetails(
                        packageName = packageName,
                        appName = applicationInfo.loadLabel(packageManager).toString(),
                        versionName = packageInfo.versionName ?: "",
                        versionCode = packageInfo.longVersionCode,
                        icon = applicationInfo.loadIcon(packageManager),
                        sourcePath = applicationInfo.sourceDir,
                        isSystemApp = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                        installDate = Date(packageInfo.firstInstallTime),
                        updateDate = Date(packageInfo.lastUpdateTime),
                        sharedUserId = packageInfo.sharedUserId,
                        uid = applicationInfo.uid,
                        sizes = sizes
                    )
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            _error.value = getApplication<Application>().getString(R.string.error_package_not_found)
        } catch (e: Exception) {
            _error.value = e.message ?: getApplication<Application>().getString(R.string.error_unknown)
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun calculateAppSizes(applicationInfo: android.content.pm.ApplicationInfo): AppSizes {
        return withContext(Dispatchers.IO) {
            val context = getApplication<Application>()

            AppSizes(
                codeSize = File(applicationInfo.sourceDir).length(),
                dataSize = SizeCalculator.calculateDirSize(File(applicationInfo.dataDir)),
                cacheSize = SizeCalculator.calculateDirSize(File(applicationInfo.dataDir, "cache")),
                externalDataSize = context.getExternalFilesDir(null)?.let {
                    SizeCalculator.calculateDirSize(it)
                } ?: 0L,
                externalCacheSize = context.externalCacheDir?.let {
                    SizeCalculator.calculateDirSize(it)
                } ?: 0L,
                obbSize = context.getObbDir()?.let {
                    SizeCalculator.calculateDirSize(it)
                } ?: 0L,
                mediaSize = context.getExternalMediaDirs().firstOrNull()?.let {
                    SizeCalculator.calculateDirSize(it)
                } ?: 0L
            )
        }
    }
}