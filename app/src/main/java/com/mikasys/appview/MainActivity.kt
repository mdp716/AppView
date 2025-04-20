package com.mikasys.appview

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_PACKAGE_NAME = "extra_package_name"

        fun createIntent(context: Context, packageName: String): Intent {
            return Intent(context, AppDetailActivity::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        packageName?.let { loadAppDetails(it) }
    }

    private fun loadAppDetails(packageName: String) {
        try {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_ACTIVITIES or PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or
                        PackageManager.GET_PROVIDERS or PackageManager.GET_META_DATA or
                        PackageManager.GET_CONFIGURATIONS or PackageManager.GET_INSTRUMENTATION or
                        PackageManager.GET_SIGNATURES or PackageManager.GET_RESOLVED_FILTER
            )

            displayAppInfo(packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("AppDetailActivity", "Package not found: $packageName", e)
        }
    }

    private fun displayAppInfo(packageInfo: PackageInfo) {
        val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
        val packageName = packageInfo.packageName
        val installTime = formatDate(Date(packageInfo.firstInstallTime))
        val updateTime = formatDate(Date(packageInfo.lastUpdateTime))
        val activities = packageInfo.activities?.map { it.name } ?: emptyList()
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        val services = packageInfo.services?.map { it.name } ?: emptyList()
        val receivers = packageInfo.receivers?.map { it.name } ?: emptyList()
        val features = packageInfo.reqFeatures?.map { it.name } ?: emptyList()

        // Set App Details
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)
        val tvPackageName = findViewById<TextView>(R.id.tv_package_name)
        val tvInstallTime = findViewById<TextView>(R.id.tv_install_time)
        val tvUpdateTime = findViewById<TextView>(R.id.tv_update_time)
        val tvAppType = findViewById<TextView>(R.id.tv_app_type)

        tvAppName.text = appName
        tvPackageName.text = packageName
        tvInstallTime.text = getString(R.string.installed_date_format, installTime)
        tvUpdateTime.text = getString(R.string.updated_date_format, updateTime)
        tvAppType.text = if (AppListUtil.isSystemApp(packageInfo.applicationInfo)) {
            getString(R.string.app_type_system)
        } else {
            getString(R.string.app_type_user)
        }

        // Set Component Details
        val tvActivitiesCount = findViewById<TextView>(R.id.tv_activities_count)
        val tvPermissionsCount = findViewById<TextView>(R.id.tv_permissions_count)
        val tvServicesCount = findViewById<TextView>(R.id.tv_services_count)
        val tvReceiversCount = findViewById<TextView>(R.id.tv_receivers_count)
        val tvFeaturesCount = findViewById<TextView>(R.id.tv_features_count)

        tvActivitiesCount.text = getString(R.string.activities_count_format, activities.size)
        tvPermissionsCount.text = getString(R.string.permissions_count_format, permissions.size)
        tvServicesCount.text = getString(R.string.services_count_format, services.size)
        tvReceiversCount.text = getString(R.string.receivers_count_format, receivers.size)
        tvFeaturesCount.text = getString(R.string.features_count_format, features.size)

        setListView(R.id.lv_activities, activities)
        setListView(R.id.lv_permissions, permissions)
        setListView(R.id.lv_services, services)
        setListView(R.id.lv_receivers, receivers)
        setListView(R.id.lv_features, features)
    }

    private fun setListView(listViewId: Int, data: List<String>) {
        val listView = findViewById<ListView>(listViewId)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
        listView.adapter = adapter
    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }
}