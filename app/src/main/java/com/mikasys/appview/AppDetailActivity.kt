package com.mikasys.appview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide

class AppDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_PACKAGE_NAME = "package_name"

        fun createIntent(context: Context, packageName: String): Intent {
            return Intent(context, AppDetailActivity::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        if (packageName == null) {
            finish()
            return
        }

        // Load app details
        loadAppDetails(packageName)
    }

    private fun loadAppDetails(packageName: String) {
        val packageManager = packageManager

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val applicationInfo = packageInfo.applicationInfo

            if (applicationInfo != null) {
                // Set app name as title
                val appName = applicationInfo.loadLabel(packageManager).toString()
                supportActionBar?.title = appName

                // Get app icon
                val iconImageView: ImageView = findViewById(R.id.iv_app_icon)
                Glide.with(this)
                    .load(applicationInfo.loadIcon(packageManager))
                    .into(iconImageView)

                // Set app path
                val pathTextView: TextView = findViewById(R.id.tv_app_path)
                pathTextView.text = applicationInfo.sourceDir

                // Set app type
                val typeTextView: TextView = findViewById(R.id.tv_app_type)
                val isSystemApp = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                typeTextView.text = if (isSystemApp) "System" else "User"
            }

            // Set package name
            val packageNameTextView: TextView = findViewById(R.id.tv_package_name)
            packageNameTextView.text = packageName

            // Set version info
            val versionTextView: TextView = findViewById(R.id.tv_version)
            val versionCode = packageInfo.longVersionCode
            val versionName = packageInfo.versionName ?: "Unknown"
            versionTextView.text = "$versionName ($versionCode)"

            // Set installation and update dates
            val installDateTextView: TextView = findViewById(R.id.tv_install_date)
            installDateTextView.text = "Installation: ${AppListUtil.formatDateTime(packageInfo.firstInstallTime)}"

            val updateDateTextView: TextView = findViewById(R.id.tv_update_date)
            updateDateTextView.text = "Update: ${AppListUtil.formatDateTime(packageInfo.lastUpdateTime)}"

            // Add expandable sections for activities, services, etc.
            setupExpandableSections(packageName)

        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun setupExpandableSections(packageName: String) {
        // This would set up the expandable sections for Activities, Services, etc.
        // For simplicity, we'll just count them in this example

        try {
            val packageInfo = packageManager.getPackageInfo(packageName,
                android.content.pm.PackageManager.GET_ACTIVITIES or
                        android.content.pm.PackageManager.GET_SERVICES or
                        android.content.pm.PackageManager.GET_RECEIVERS or
                        android.content.pm.PackageManager.GET_PROVIDERS)

            // Set up Activities section
            val activitiesTextView: TextView = findViewById(R.id.tv_activities)
            val activitiesCount = packageInfo.activities?.size ?: 0
            activitiesTextView.text = "Activities ($activitiesCount)"

            // Set up Services section
            val servicesTextView: TextView = findViewById(R.id.tv_services)
            val servicesCount = packageInfo.services?.size ?: 0
            servicesTextView.text = "Services ($servicesCount)"

            // Set up Receivers section
            val receiversTextView: TextView = findViewById(R.id.tv_receivers)
            val receiversCount = packageInfo.receivers?.size ?: 0
            receiversTextView.text = "Receivers ($receiversCount)"

            // Set up Providers section
            val providersTextView: TextView = findViewById(R.id.tv_providers)
            val providersCount = packageInfo.providers?.size ?: 0
            providersTextView.text = "Providers ($providersCount)"

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}