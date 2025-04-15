package com.mikasys.appview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
                typeTextView.text = getString(if (isSystemApp) R.string.app_type_system else R.string.app_type_user)
            }

            // Set version info
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            val versionName = packageInfo.versionName ?: getString(R.string.unknown_version)
            val versionTextView: TextView? = findViewById(R.id.versionTextView)
            versionTextView?.text = getString(R.string.version_info, versionName, versionCode)

            // Set installation and update dates
            val installDateTextView: TextView = findViewById(R.id.tv_install_date)
            installDateTextView.text = getString(R.string.installation_date, AppListUtil.formatDateTime(packageInfo.firstInstallTime))

            val updateDateTextView: TextView = findViewById(R.id.tv_update_date)
            updateDateTextView.text = getString(R.string.update_date, AppListUtil.formatDateTime(packageInfo.lastUpdateTime))

            // Add expandable sections for activities, services, etc.
            setupExpandableSections(packageName)

        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun setupExpandableSections(packageName: String) {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName,
                android.content.pm.PackageManager.GET_ACTIVITIES or
                        android.content.pm.PackageManager.GET_SERVICES or
                        android.content.pm.PackageManager.GET_RECEIVERS or
                        android.content.pm.PackageManager.GET_PROVIDERS)

            // Set up Activities section
            val activitiesTextView: TextView = findViewById(R.id.tv_activities)
            val activitiesCount = packageInfo.activities?.size ?: 0
            activitiesTextView.text = getString(R.string.activities_count, activitiesCount)

            // Set up Services section
            val servicesTextView: TextView = findViewById(R.id.tv_services)
            val servicesCount = packageInfo.services?.size ?: 0
            servicesTextView.text = getString(R.string.services_count, servicesCount)

            // Set up Receivers section
            val receiversTextView: TextView = findViewById(R.id.tv_receivers)
            val receiversCount = packageInfo.receivers?.size ?: 0
            receiversTextView.text = getString(R.string.receivers_count, receiversCount)

            // Set up Providers section
            val providersTextView: TextView = findViewById(R.id.tv_providers)
            val providersCount = packageInfo.providers?.size ?: 0
            providersTextView.text = getString(R.string.providers_count, providersCount)

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