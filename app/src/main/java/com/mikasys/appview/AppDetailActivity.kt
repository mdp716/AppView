package com.mikasys.appview

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

/**
 * Activity that displays detailed information about an installed application.
 * This includes basic app info, size details, dates, and traffic statistics.
 */
class AppDetailActivity : AppCompatActivity() {
    private val viewModel: AppDetailViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // View references
    private lateinit var toolbar: Toolbar
    private lateinit var iconView: ImageView
    private lateinit var packageNameView: TextView
    private lateinit var versionInfoView: TextView
    private lateinit var appPathView: TextView
    private lateinit var appTypeView: TextView
    private lateinit var installDateView: TextView
    private lateinit var updateDateView: TextView
    private lateinit var sharedUserIdView: TextView

    // Size views
    private lateinit var sizeCodeView: TextView
    private lateinit var sizeDataView: TextView
    private lateinit var sizeCacheView: TextView
    private lateinit var sizeExtDataView: TextView
    private lateinit var sizeExtCacheView: TextView
    private lateinit var sizeExtObbView: TextView
    private lateinit var sizeExtMediaView: TextView

    companion object {
        private const val EXTRA_PACKAGE_NAME = "package_name"

        /**
         * Creates an intent to launch this activity
         * @param context The context to create the intent from
         * @param packageName The package name of the app to display details for
         * @return Intent configured to launch this activity
         */
        fun createIntent(context: Context, packageName: String): Intent {
            return Intent(context, AppDetailActivity::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)
        
        initializeViews()
        setupToolbar()
        setupObservers()
        loadAppDetails()
    }

    private fun initializeViews() {
        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar)

        // Initialize basic info views
        iconView = findViewById(R.id.app_icon)
        packageNameView = findViewById(R.id.package_name)
        versionInfoView = findViewById(R.id.version_info)
        appPathView = findViewById(R.id.app_path)
        appTypeView = findViewById(R.id.app_type)
        installDateView = findViewById(R.id.install_date)
        updateDateView = findViewById(R.id.update_date)
        sharedUserIdView = findViewById(R.id.shared_user_id)

        // Initialize size info views
        sizeCodeView = findViewById(R.id.size_code)
        sizeDataView = findViewById(R.id.size_data)
        sizeCacheView = findViewById(R.id.size_cache)
        sizeExtDataView = findViewById(R.id.size_ext_data)
        sizeExtCacheView = findViewById(R.id.size_ext_cache)
        sizeExtObbView = findViewById(R.id.size_ext_obb)
        sizeExtMediaView = findViewById(R.id.size_ext_media)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupObservers() {
        // Observe app details updates
        viewModel.appDetails.observe(this) { appDetails ->
            updateUI(appDetails)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            updateLoadingState(isLoading)
        }

        // Observe errors
        viewModel.error.observe(this) { error ->
            showError(error)
        }
    }

    private fun loadAppDetails() {
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        if (packageName == null) {
            showError(getString(R.string.error_no_package_name))
            finish()
            return
        }
        
        lifecycleScope.launch {
            viewModel.loadAppDetails(packageName)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_view_settings -> {
                openAppSettings()
                true
            }
            R.id.action_view_manifest -> {
                showManifestDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(appDetails: AppDetails) {
        // Update app icon
        Glide.with(this)
            .load(appDetails.icon)
            .into(iconView)

        // Update title and basic info
        title = appDetails.appName
        packageNameView.text = appDetails.packageName
        versionInfoView.text = getString(R.string.version_format, 
            appDetails.versionName, 
            appDetails.versionCode)
        appPathView.text = appDetails.sourcePath
        appTypeView.text = if (appDetails.isSystemApp) {
            getString(R.string.system_app)
        } else {
            getString(R.string.user_app)
        }

        // Update sizes
        with(appDetails.sizes) {
            sizeCodeView.text = formatSize(codeSize)
            sizeDataView.text = formatSize(dataSize)
            sizeCacheView.text = formatSize(cacheSize)
            sizeExtDataView.text = formatSize(externalDataSize)
            sizeExtCacheView.text = formatSize(externalCacheSize)
            sizeExtObbView.text = formatSize(obbSize)
            sizeExtMediaView.text = formatSize(mediaSize)
        }

        // Update dates
        installDateView.text = getString(R.string.installed_format, 
            dateFormat.format(appDetails.installDate))
        updateDateView.text = getString(R.string.last_updated_format, 
            dateFormat.format(appDetails.updateDate))

        // Update shared user ID
        appDetails.sharedUserId?.let {
            sharedUserIdView.text = getString(R.string.shared_user_id_format, it)
            sharedUserIdView.isVisible = true
        } ?: run {
            sharedUserIdView.isVisible = false
        }
    }

    private fun formatSize(size: Long): String {
        return Formatter.formatFileSize(this, size)
    }

    private fun updateLoadingState(isLoading: Boolean) {
        // TODO: Implement loading state UI
        // You might want to show/hide a progress bar or loading indicator
    }

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    private fun openAppSettings() {
        viewModel.appDetails.value?.let { appDetails ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", appDetails.packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun showManifestDialog() {
        viewModel.appDetails.value?.let { appDetails ->
            try {
                val packageInfo = packageManager.getPackageInfo(
                    appDetails.packageName,
                    PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_META_DATA
                )

                val webView = WebView(this).apply {
                    settings.javaScriptEnabled = false
                    settings.allowFileAccess = false
                    webViewClient = WebViewClient()
                }

                val content = StringBuilder()
                content.append("""
                    <html>
                    <head>
                        <style>
                            body { font-family: monospace; white-space: pre; }
                            .comment { color: #008000; }
                            .tag { color: #000080; }
                            .attr { color: #7D0045; }
                            .value { color: #0000FF; }
                        </style>
                    </head>
                    <body>
                """.trimIndent())

                // Add manifest header with basic app info
                content.append("""
                    <span class="comment"><!-- App: ${appDetails.appName} --></span>
                    <span class="comment"><!-- Version: ${appDetails.versionName} (${appDetails.versionCode}) --></span>
                    <span class="comment"><!-- Package: ${appDetails.packageName} --></span>
                    <span class="comment"><!-- Install Location: ${getInstallLocation(packageInfo)} --></span>
                    
                    <span class="tag">&lt;manifest</span> 
                        <span class="attr">xmlns:android</span>=<span class="value">"http://schemas.android.com/apk/res/android"</span>
                        <span class="attr">package</span>=<span class="value">"${packageInfo.packageName}"</span>
                        <span class="attr">android:versionCode</span>=<span class="value">"${packageInfo.longVersionCode}"</span>
                        <span class="attr">android:versionName</span>=<span class="value">"${packageInfo.versionName}"</span>&gt;
                    
                """.trimIndent())

                // Add permissions
                packageInfo.requestedPermissions?.let { permissions ->
                    content.append("\n    <span class=\"comment\"><!-- Permissions --></span>\n")
                    permissions.forEach { permission ->
                        content.append("""
                            <span class="tag">    &lt;uses-permission</span> <span class="attr">android:name</span>=<span class="value">"$permission"</span> /&gt;
                        """.trimIndent()).append("\n")
                    }
                }

                // Add application block
                content.append("""
                    
                    <span class="tag">    &lt;application</span>
                        <span class="attr">android:label</span>=<span class="value">"${appDetails.appName}"</span>
                        <span class="attr">android:icon</span>=<span class="value">"@mipmap/ic_launcher"</span>&gt;
                """.trimIndent())

                // Add activities
                packageInfo.activities?.let { activities ->
                    content.append("\n        <span class=\"comment\"><!-- Activities --></span>\n")
                    activities.forEach { activity ->
                        content.append("""
                            <span class="tag">        &lt;activity</span> <span class="attr">android:name</span>=<span class="value">"${activity.name}"</span> /&gt;
                        """.trimIndent()).append("\n")
                    }
                }

                // Add services
                packageInfo.services?.let { services ->
                    content.append("\n        <span class=\"comment\"><!-- Services --></span>\n")
                    services.forEach { service ->
                        content.append("""
                            <span class="tag">        &lt;service</span> <span class="attr">android:name</span>=<span class="value">"${service.name}"</span> /&gt;
                        """.trimIndent()).append("\n")
                    }
                }

                // Add receivers
                packageInfo.receivers?.let { receivers ->
                    content.append("\n        <span class=\"comment\"><!-- Receivers --></span>\n")
                    receivers.forEach { receiver ->
                        content.append("""
                            <span class="tag">        &lt;receiver</span> <span class="attr">android:name</span>=<span class="value">"${receiver.name}"</span> /&gt;
                        """.trimIndent()).append("\n")
                    }
                }

                // Add providers
                packageInfo.providers?.let { providers ->
                    content.append("\n        <span class=\"comment\"><!-- Providers --></span>\n")
                    providers.forEach { provider ->
                        content.append("""
                            <span class="tag">        &lt;provider</span> <span class="attr">android:name</span>=<span class="value">"${provider.name}"</span> /&gt;
                        """.trimIndent()).append("\n")
                    }
                }

                // Close application and manifest tags
                content.append("""
                    
                    <span class="tag">    &lt;/application&gt;</span>
                    <span class="tag">&lt;/manifest&gt;</span>
                """.trimIndent())

                content.append("\n</body></html>")

                // Create and show the dialog
                AlertDialog.Builder(this)
                    .setTitle("AndroidManifest.xml")
                    .setView(webView)
                    .setPositiveButton(getString(R.string.ok), null)
                    .create()
                    .also { dialog ->
                        webView.loadDataWithBaseURL(null, content.toString(), "text/html", "UTF-8", null)
                        dialog.show()
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                showError(getString(R.string.manifest_file_error))
            }
        }
    }

    private fun getInstallLocation(packageInfo: PackageInfo): String {
        return when ((packageInfo.applicationInfo?.flags ?: 0 and ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            true -> "External"
            false -> "Internal"
        }
    }
}