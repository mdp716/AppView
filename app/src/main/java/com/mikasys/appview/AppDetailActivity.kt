package com.mikasys.appview

import android.content.Intent
import android.content.pm.*
import android.net.TrafficStats
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.storage.StorageManager
import android.os.Process
import android.os.UserHandle
import android.webkit.WebView

class AppDetailActivity : AppCompatActivity() {
    private lateinit var packageManager: PackageManager
    private lateinit var packageInfo: PackageInfo
    private lateinit var applicationInfo: ApplicationInfo
    private lateinit var packageName: String
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get package info
        packageName = intent.getStringExtra("package_name") ?: return
        packageManager = applicationContext.packageManager
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 
                PackageManager.GET_ACTIVITIES or 
                PackageManager.GET_SERVICES or 
                PackageManager.GET_RECEIVERS or 
                PackageManager.GET_PROVIDERS or 
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_CONFIGURATIONS or
                PackageManager.GET_SIGNING_CERTIFICATES or
                PackageManager.GET_META_DATA or
                PackageManager.GET_SHARED_LIBRARY_FILES)
            applicationInfo = packageInfo.applicationInfo ?: throw PackageManager.NameNotFoundException("ApplicationInfo is null")
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, "Package not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupBasicInfo()
        setupSizeInfo()
        setupDatesInfo()
        setupTrafficInfo()
        setupCollapsibleSections()
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

    private fun setupBasicInfo() {
        // Load app icon
        val iconView = findViewById<ImageView>(R.id.app_icon)
        Glide.with(this)
            .load(packageManager.getApplicationIcon(packageName))
            .into(iconView)

        // Set package name
        findViewById<TextView>(R.id.package_name).text = packageName

        // Set version info
        val versionInfo = findViewById<TextView>(R.id.version_info)
        versionInfo.text = "Version ${packageInfo.versionName} (${packageInfo.longVersionCode})"

        // Set app path
        val appPath = findViewById<TextView>(R.id.app_path)
        appPath.text = applicationInfo.sourceDir

        // Set app type
        val appType = findViewById<TextView>(R.id.app_type)
        appType.text = if ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
            "System app"
        } else {
            "User app"
        }
    }

    private fun setupSizeInfo() {
        try {
            // Code size (APK size)
            val apkFile = File(applicationInfo.sourceDir)
            findViewById<TextView>(R.id.size_code).text = Formatter.formatFileSize(this, apkFile.length())
            findViewById<TextView>(R.id.size_ext_code).text = "--"

            // Data size
            val dataDir = File(applicationInfo.dataDir)
            val dataSize = calculateDirSize(dataDir)
            findViewById<TextView>(R.id.size_data).text = Formatter.formatFileSize(this, dataSize)

            val externalDataDir = getExternalFilesDir(null)
            val externalDataSize = externalDataDir?.let { calculateDirSize(it) } ?: 0L
            findViewById<TextView>(R.id.size_ext_data).text = Formatter.formatFileSize(this, externalDataSize)

            // Cache size
            val cacheDir = File(applicationInfo.dataDir, "cache")
            val cacheSize = calculateDirSize(cacheDir)
            findViewById<TextView>(R.id.size_cache).text = Formatter.formatFileSize(this, cacheSize)

            val externalCacheDir = externalCacheDir
            val externalCacheSize = externalCacheDir?.let { calculateDirSize(it) } ?: 0L
            findViewById<TextView>(R.id.size_ext_cache).text = Formatter.formatFileSize(this, externalCacheSize)

            // OBB size
            val obbDir = getObbDir()
            val obbSize = obbDir?.let { calculateDirSize(it) } ?: 0L
            findViewById<TextView>(R.id.size_ext_obb).text = Formatter.formatFileSize(this, obbSize)

            // Media size
            val externalMediaDir = getExternalMediaDirs().firstOrNull()
            val mediaSize = externalMediaDir?.let { calculateDirSize(it) } ?: 0L
            findViewById<TextView>(R.id.size_ext_media).text = Formatter.formatFileSize(this, mediaSize)

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle errors gracefully
            findViewById<TextView>(R.id.size_code).text = "--"
            findViewById<TextView>(R.id.size_ext_code).text = "--"
            findViewById<TextView>(R.id.size_data).text = "--"
            findViewById<TextView>(R.id.size_ext_data).text = "--"
            findViewById<TextView>(R.id.size_cache).text = "--"
            findViewById<TextView>(R.id.size_ext_cache).text = "--"
            findViewById<TextView>(R.id.size_ext_obb).text = "--"
            findViewById<TextView>(R.id.size_ext_media).text = "--"
        }
    }

    private fun calculateDirSize(dir: File): Long {
        var size = 0L
        if (!dir.exists()) return size
        
        dir.listFiles()?.forEach { file ->
            size += if (file.isFile) {
                file.length()
            } else {
                calculateDirSize(file)
            }
        }
        return size
    }

    private fun setupDatesInfo() {
        // Install date
        val installDate = findViewById<TextView>(R.id.install_date)
        installDate.text = "Installed: ${dateFormat.format(Date(packageInfo.firstInstallTime))}"

        // Update date
        val updateDate = findViewById<TextView>(R.id.update_date)
        updateDate.text = "Last updated: ${dateFormat.format(Date(packageInfo.lastUpdateTime))}"

        // Shared User ID
        val sharedUserId = findViewById<TextView>(R.id.shared_user_id)
        packageInfo.sharedUserId?.let {
            sharedUserId.text = "Shared User ID: $it"
            sharedUserId.visibility = View.VISIBLE
        } ?: run {
            sharedUserId.visibility = View.GONE
        }
    }

    private fun setupTrafficInfo() {
        val uid = applicationInfo.uid
        val received = findViewById<TextView>(R.id.data_received)
        val sent = findViewById<TextView>(R.id.data_sent)

        val rxBytes = TrafficStats.getUidRxBytes(uid).let { if (it < 0) 0 else it }
        val txBytes = TrafficStats.getUidTxBytes(uid).let { if (it < 0) 0 else it }

        received.text = "Received: ${Formatter.formatFileSize(this, rxBytes)}"
        sent.text = "Sent: ${Formatter.formatFileSize(this, txBytes)}"
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = android.net.Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    private fun showManifestDialog() {
        val manifestFile = File(applicationInfo.sourceDir)
        if (!manifestFile.exists()) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Cannot access manifest file")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val manifestContent = packageManager.getPackageArchiveInfo(
            manifestFile.absolutePath,
            PackageManager.GET_ACTIVITIES or 
            PackageManager.GET_META_DATA or
            PackageManager.GET_SHARED_LIBRARY_FILES or
            PackageManager.GET_SIGNATURES or
            PackageManager.GET_SIGNING_CERTIFICATES or
            PackageManager.GET_CONFIGURATIONS or
            PackageManager.GET_GIDS or
            PackageManager.GET_INSTRUMENTATION or
            PackageManager.GET_INTENT_FILTERS or
            PackageManager.GET_PERMISSIONS or
            PackageManager.GET_PROVIDERS or
            PackageManager.GET_RECEIVERS or
            PackageManager.GET_SERVICES or
            PackageManager.GET_URI_PERMISSION_PATTERNS
        )?.let { info ->
            buildString {
                append("<html><head>")
                append("<style>")
                append("""
                    body {
                        font-family: monospace;
                        padding: 16px;
                        background-color: #FFFFFF;
                        white-space: pre;
                        font-size: 14px;
                        line-height: 1.5;
                    }
                    .tag { color: #800080; }
                    .attr { color: #A52A2A; }
                    .value { color: #0000FF; }
                    .text { color: #000000; }
                    .comment { color: #008000; }
                """)
                append("</style>")
                append("</head><body>")
                
                append("<div class='comment'><!-- This XML file does not appear to have any style information associated with it. The document tree is shown below. --></div>\n\n")
                
                append("<span class='tag'>&lt;manifest</span> ")
                append("<span class='attr'>versionCode</span>=<span class='value'>\"${info.longVersionCode}\"</span> ")
                append("<span class='attr'>versionName</span>=<span class='value'>\"${info.versionName}\"</span> ")
                append("<span class='attr'>package</span>=<span class='value'>\"${info.packageName}\"</span>")
                append("<span class='tag'>&gt;</span>\n")

                // Uses SDK
                append("    <span class='tag'>&lt;uses-sdk</span> ")
                append("<span class='attr'>minSdkVersion</span>=<span class='value'>\"${applicationInfo.minSdkVersion}\"</span> ")
                append("<span class='attr'>targetSdkVersion</span>=<span class='value'>\"${applicationInfo.targetSdkVersion}\"</span>")
                append("<span class='tag'>/&gt;</span>\n")

                // Uses Features
                info.reqFeatures?.forEach { feature ->
                    append("    <span class='tag'>&lt;uses-feature</span> ")
                    if (feature.reqGlEsVersion != null) {
                        append("<span class='attr'>glEsVersion</span>=<span class='value'>\"${String.format("0x%08x", feature.reqGlEsVersion)}\"</span>")
                    } else {
                        append("<span class='attr'>name</span>=<span class='value'>\"${feature.name}\"</span>")
                    }
                    append("<span class='tag'>/&gt;</span>\n")
                }

                // Uses Permissions
                info.requestedPermissions?.forEach { permission ->
                    append("    <span class='tag'>&lt;uses-permission</span> ")
                    append("<span class='attr'>name</span>=<span class='value'>\"$permission\"</span>")
                    append("<span class='tag'>/&gt;</span>\n")
                }

                // Application
                append("    <span class='tag'>&lt;application</span>\n")

                // Activities
                info.activities?.forEach { activity ->
                    append("        <span class='tag'>&lt;activity</span> ")
                    append("<span class='attr'>name</span>=<span class='value'>\"${activity.name}\"</span>")
                    if (activity.exported) {
                        append(" <span class='attr'>exported</span>=<span class='value'>\"true\"</span>")
                    }
                    append("<span class='tag'>/&gt;</span>\n")
                }

                // Services
                info.services?.forEach { service ->
                    append("        <span class='tag'>&lt;service</span> ")
                    append("<span class='attr'>name</span>=<span class='value'>\"${service.name}\"</span>")
                    if (service.exported) {
                        append(" <span class='attr'>exported</span>=<span class='value'>\"true\"</span>")
                    }
                    append("<span class='tag'>/&gt;</span>\n")
                }

                // Receivers
                info.receivers?.forEach { receiver ->
                    append("        <span class='tag'>&lt;receiver</span> ")
                    append("<span class='attr'>name</span>=<span class='value'>\"${receiver.name}\"</span>")
                    if (receiver.exported) {
                        append(" <span class='attr'>exported</span>=<span class='value'>\"true\"</span>")
                    }
                    append("<span class='tag'>/&gt;</span>\n")
                }

                // Providers
                info.providers?.forEach { provider ->
                    append("        <span class='tag'>&lt;provider</span> ")
                    append("<span class='attr'>name</span>=<span class='value'>\"${provider.name}\"</span>")
                    append(" <span class='attr'>authorities</span>=<span class='value'>\"${provider.authority}\"</span>")
                    if (provider.exported) {
                        append(" <span class='attr'>exported</span>=<span class='value'>\"true\"</span>")
                    }
                    append("<span class='tag'>/&gt;</span>\n")
                }

                append("    <span class='tag'>&lt;/application&gt;</span>\n")
                append("<span class='tag'>&lt;/manifest&gt;</span>")
                append("</body></html>")
            }
        } ?: "<html><body><h2>Error</h2><p>Unable to read manifest content</p></body></html>"

        val webView = WebView(this)
        webView.settings.apply {
            defaultFontSize = 14
        }
        webView.loadDataWithBaseURL(null, manifestContent, "text/html", "UTF-8", null)

        AlertDialog.Builder(this)
            .setTitle("Manifest: ${applicationInfo.loadLabel(packageManager)}")
            .setView(webView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun setupCollapsibleSections() {
        // Setup all collapsible sections
        setupActivitiesSection()
        setupIntentsSection()
        setupServicesSection()
        setupReceiversSection()
        setupProvidersSection()
        setupPermissionsSection()
        setupUsesPermissionsSection()
        setupFeaturesSection()
        setupConfigurationsSection()
        setupSignatureSection()
    }

    private fun setupActivitiesSection() {
        val header = findViewById<LinearLayout>(R.id.activities_header) ?: return
        val activitiesCard = header.parent as? MaterialCardView ?: return
        val content = activitiesCard.findViewById<LinearLayout>(R.id.activities_content) ?: return
        val title = activitiesCard.findViewById<TextView>(R.id.activities_title) ?: return
        val arrow = activitiesCard.findViewById<ImageView>(R.id.activities_arrow) ?: return

        // Get activities directly from the package manager
        val activities = try {
            // Create a new package context to force a fresh load of the manifest
            val pkgContext = createPackageContext(packageName, 0)
            val pm = pkgContext.packageManager
            
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES or 
                PackageManager.GET_META_DATA)?.activities ?: emptyArray()
        } catch (e: Exception) {
            e.printStackTrace()
            packageInfo.activities ?: emptyArray() // Fallback to cached activities if direct access fails
        }

        title.text = "Activities (${activities.size})"
        setupCollapsibleSection(header, content, arrow)

        activities.forEach { activityInfo ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_activity, content, false)
            itemView.findViewById<TextView>(R.id.activity_name)?.text = activityInfo.name
            itemView.findViewById<TextView>(R.id.activity_details)?.text = buildActivityDetails(activityInfo)
            content.addView(itemView)
        }
    }

    private fun setupIntentsSection() {
        val header = findViewById<LinearLayout>(R.id.intents_header) ?: return
        val intentsCard = header.parent as? MaterialCardView ?: return
        val content = intentsCard.findViewById<LinearLayout>(R.id.intents_content) ?: return
        val title = intentsCard.findViewById<TextView>(R.id.intents_title) ?: return
        val arrow = intentsCard.findViewById<ImageView>(R.id.intents_arrow) ?: return

        val launchableActivities = packageInfo.activities?.filter { activityInfo ->
            packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).setClassName(packageInfo.packageName, activityInfo.name), 0) != null
        } ?: emptyList()

        title.text = "Intents (${launchableActivities.size})"

        setupCollapsibleSection(header, content, arrow)

        launchableActivities.forEach { activityInfo ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_activity, content, false)
            
            itemView.findViewById<TextView>(R.id.activity_name)?.text = activityInfo.name
            itemView.findViewById<TextView>(R.id.activity_details)?.text = "Task affinity: ${activityInfo.taskAffinity}\n" +
                                                                        "Launch mode: ${getLaunchMode(activityInfo.launchMode)}\n" +
                                                                        "Orientation: ${getOrientation(activityInfo.screenOrientation)}"
            
            val launchContainer = itemView.findViewById<LinearLayout>(R.id.launch_container)
            launchContainer?.visibility = View.VISIBLE
            
            val adbCommand = "adb shell am start -n ${packageInfo.packageName}/${activityInfo.name}"
            itemView.findViewById<TextView>(R.id.adb_command)?.text = adbCommand
            
            itemView.findViewById<Button>(R.id.launch_button)?.setOnClickListener {
                startActivity(Intent().setClassName(packageInfo.packageName, activityInfo.name))
            }
            
            content.addView(itemView)
        }
    }

    private fun setupServicesSection() {
        val header = findViewById<LinearLayout>(R.id.services_header) ?: return
        val servicesCard = header.parent as? MaterialCardView ?: return
        val content = servicesCard.findViewById<LinearLayout>(R.id.services_content) ?: return
        val title = servicesCard.findViewById<TextView>(R.id.services_title) ?: return
        val arrow = servicesCard.findViewById<ImageView>(R.id.services_arrow) ?: return

        val services = packageInfo.services ?: emptyArray()
        title.text = "Services (${services.size})"

        setupCollapsibleSection(header, content, arrow)

        services.forEach { serviceInfo ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_service, content, false)
            itemView.findViewById<TextView>(R.id.service_name)?.text = serviceInfo.name
            itemView.findViewById<TextView>(R.id.service_details)?.text = buildServiceDetails(serviceInfo)
            content.addView(itemView)
        }
    }

    private fun setupReceiversSection() {
        val header = findViewById<LinearLayout>(R.id.receivers_header) ?: return
        val receiversCard = header.parent as? MaterialCardView ?: return
        val content = receiversCard.findViewById<LinearLayout>(R.id.receivers_content) ?: return
        val title = receiversCard.findViewById<TextView>(R.id.receivers_title) ?: return
        val arrow = receiversCard.findViewById<ImageView>(R.id.receivers_arrow) ?: return

        val receivers = packageInfo.receivers ?: emptyArray()
        title.text = "Receivers (${receivers.size})"

        setupCollapsibleSection(header, content, arrow)

        receivers.forEach { receiverInfo ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_service, content, false)
            itemView.findViewById<TextView>(R.id.service_name)?.text = receiverInfo.name
            itemView.findViewById<TextView>(R.id.service_details)?.text = buildComponentDetails(receiverInfo)
            content.addView(itemView)
        }
    }

    private fun setupProvidersSection() {
        val header = findViewById<LinearLayout>(R.id.providers_header) ?: return
        val providersCard = header.parent as? MaterialCardView ?: return
        val content = providersCard.findViewById<LinearLayout>(R.id.providers_content) ?: return
        val title = providersCard.findViewById<TextView>(R.id.providers_title) ?: return
        val arrow = providersCard.findViewById<ImageView>(R.id.providers_arrow) ?: return

        val providers = packageInfo.providers ?: emptyArray()
        title.text = "Providers (${providers.size})"

        setupCollapsibleSection(header, content, arrow)

        providers.forEach { providerInfo ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_service, content, false)
            itemView.findViewById<TextView>(R.id.service_name)?.text = providerInfo.name
            itemView.findViewById<TextView>(R.id.service_details)?.text = buildProviderDetails(providerInfo)
            content.addView(itemView)
        }
    }

    private fun setupPermissionsSection() {
        val header = findViewById<LinearLayout>(R.id.permissions_header) ?: return
        val permissionsCard = header.parent as? MaterialCardView ?: return
        val content = permissionsCard.findViewById<LinearLayout>(R.id.permissions_content) ?: return
        val title = permissionsCard.findViewById<TextView>(R.id.permissions_title) ?: return
        val arrow = permissionsCard.findViewById<ImageView>(R.id.permissions_arrow) ?: return

        val permissions = packageInfo.permissions ?: emptyArray()
        title.text = "Permissions (${permissions.size})"

        setupCollapsibleSection(header, content, arrow)

        permissions.forEach { permissionInfo ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_service, content, false)
            itemView.findViewById<TextView>(R.id.service_name)?.text = permissionInfo.name
            itemView.findViewById<TextView>(R.id.service_details)?.text = buildPermissionDetails(permissionInfo)
            content.addView(itemView)
        }
    }

    private fun setupUsesPermissionsSection() {
        val header = findViewById<LinearLayout>(R.id.uses_permissions_header) ?: return
        val usesPermissionsCard = header.parent as? MaterialCardView ?: return
        val content = usesPermissionsCard.findViewById<LinearLayout>(R.id.uses_permissions_content) ?: return
        val title = usesPermissionsCard.findViewById<TextView>(R.id.uses_permissions_title) ?: return
        val arrow = usesPermissionsCard.findViewById<ImageView>(R.id.uses_permissions_arrow) ?: return

        val usesPermissions = packageInfo.requestedPermissions ?: emptyArray()
        title.text = "Uses Permissions (${usesPermissions.size})"

        setupCollapsibleSection(header, content, arrow)

        usesPermissions.forEach { permission ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_service, content, false)
            itemView.findViewById<TextView>(R.id.service_name)?.text = permission
            content.addView(itemView)
        }
    }

    private fun setupFeaturesSection() {
        val header = findViewById<LinearLayout>(R.id.features_header) ?: return
        val featuresCard = header.parent as? MaterialCardView ?: return
        val content = featuresCard.findViewById<LinearLayout>(R.id.features_content) ?: return
        val title = featuresCard.findViewById<TextView>(R.id.features_title) ?: return
        val arrow = featuresCard.findViewById<ImageView>(R.id.features_arrow) ?: return

        val features = packageInfo.reqFeatures ?: emptyArray()
        title.text = "Uses features (${features.size})"

        setupCollapsibleSection(header, content, arrow)

        features.forEach { feature ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_service, content, false)
            val featureText = if (feature.reqGlEsVersion != null) {
                "OpenGL ES ${feature.reqGlEsVersion}"
            } else {
                feature.name ?: "Unknown Feature"
            }
            itemView.findViewById<TextView>(R.id.service_name)?.text = featureText
            content.addView(itemView)
        }
    }

    private fun setupConfigurationsSection() {
        val header = findViewById<LinearLayout>(R.id.configurations_header) ?: return
        val configsCard = header.parent as? MaterialCardView ?: return
        val content = configsCard.findViewById<LinearLayout>(R.id.configurations_content) ?: return
        val title = configsCard.findViewById<TextView>(R.id.configurations_title) ?: return
        val arrow = configsCard.findViewById<ImageView>(R.id.configurations_arrow) ?: return

        val configs = packageInfo.configPreferences ?: emptyArray()
        title.text = "Configurations (${configs.size})"

        setupCollapsibleSection(header, content, arrow)

        configs.forEach { config ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_service, content, false)
            itemView.findViewById<TextView>(R.id.service_name)?.text = config.toString()
            content.addView(itemView)
        }
    }

    private fun setupSignatureSection() {
        val header = findViewById<LinearLayout>(R.id.signature_header) ?: return
        val signatureCard = header.parent as? MaterialCardView ?: return
        val content = signatureCard.findViewById<LinearLayout>(R.id.signature_content) ?: return
        val title = signatureCard.findViewById<TextView>(R.id.signature_title) ?: return
        val arrow = signatureCard.findViewById<ImageView>(R.id.signature_arrow) ?: return

        val signatures = packageInfo.signingInfo?.signingCertificateHistory ?: emptyArray()
        title.text = "Signatures (${signatures.size})"

        setupCollapsibleSection(header, content, arrow)

        signatures.forEach { signature ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_service, content, false)
            itemView.findViewById<TextView>(R.id.service_name)?.text = signature.toCharsString()
            content.addView(itemView)
        }
    }

    private fun setupCollapsibleSection(header: View, content: View, arrow: ImageView) {
        // Initialize state to collapsed
        content.visibility = View.GONE
        arrow.rotation = 0f
        
        header.setOnClickListener {
            // Toggle visibility and rotate arrow
            if (content.visibility == View.VISIBLE) {
                content.visibility = View.GONE
                arrow.animate().rotation(0f).setDuration(200)
            } else {
                content.visibility = View.VISIBLE
                arrow.animate().rotation(180f).setDuration(200)
            }
        }
    }

    private fun formatSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> String.format("%d B", size)
        }
    }

    private fun getLaunchMode(mode: Int): String {
        return when (mode) {
            ActivityInfo.LAUNCH_MULTIPLE -> "standard"
            ActivityInfo.LAUNCH_SINGLE_TOP -> "singleTop"
            ActivityInfo.LAUNCH_SINGLE_TASK -> "singleTask"
            ActivityInfo.LAUNCH_SINGLE_INSTANCE -> "singleInstance"
            else -> "unknown"
        }
    }

    private fun getOrientation(orientation: Int): String {
        return when (orientation) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> "unspecified"
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> "landscape"
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> "portrait"
            ActivityInfo.SCREEN_ORIENTATION_USER -> "user"
            ActivityInfo.SCREEN_ORIENTATION_BEHIND -> "behind"
            ActivityInfo.SCREEN_ORIENTATION_SENSOR -> "sensor"
            ActivityInfo.SCREEN_ORIENTATION_NOSENSOR -> "nosensor"
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> "sensorLandscape"
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT -> "sensorPortrait"
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> "reverseLandscape"
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> "reversePortrait"
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR -> "fullSensor"
            ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE -> "userLandscape"
            ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT -> "userPortrait"
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER -> "fullUser"
            ActivityInfo.SCREEN_ORIENTATION_LOCKED -> "locked"
            else -> "unknown"
        }
    }

    private fun buildActivityDetails(activityInfo: ActivityInfo): String {
        return buildString {
            append("Task affinity: ${activityInfo.taskAffinity}\n")
            append("Launch mode: ${getLaunchMode(activityInfo.launchMode)}\n")
            append("Orientation: ${getOrientation(activityInfo.screenOrientation)}\n")
            append("Exported: ${activityInfo.exported}\n")
            append("Enabled: ${activityInfo.enabled}\n")
            append("Direct Boot Aware: ${activityInfo.directBootAware}\n")
        }
    }

    private fun buildServiceDetails(serviceInfo: ServiceInfo): String {
        return buildString {
            append("Exported: ${serviceInfo.exported}\n")
            append("Enabled: ${serviceInfo.enabled}\n")
            append("Direct Boot Aware: ${serviceInfo.directBootAware}\n")
            append("Process Name: ${serviceInfo.processName}\n")
        }
    }

    private fun buildComponentDetails(componentInfo: ComponentInfo): String {
        return buildString {
            append("Exported: ${componentInfo.exported}\n")
            append("Enabled: ${componentInfo.enabled}\n")
            append("Direct Boot Aware: ${componentInfo.directBootAware}\n")
            append("Process Name: ${componentInfo.processName}\n")
        }
    }

    private fun buildProviderDetails(providerInfo: ProviderInfo): String {
        return buildString {
            append("Exported: ${providerInfo.exported}\n")
            append("Enabled: ${providerInfo.enabled}\n")
            append("Direct Boot Aware: ${providerInfo.directBootAware}\n")
            append("Process Name: ${providerInfo.processName}\n")
            append("Authority: ${providerInfo.authority}\n")
            append("Read Permission: ${providerInfo.readPermission}\n")
            append("Write Permission: ${providerInfo.writePermission}\n")
        }
    }

    private fun buildPermissionDetails(permissionInfo: PermissionInfo): String {
        return buildString {
            append("Protection Level: ${getProtectionLevel(permissionInfo)}\n")
            append("Group: ${permissionInfo.group ?: "None"}\n")
            append("Description: ${permissionInfo.loadDescription(packageManager) ?: "None"}\n")
        }
    }

    private fun getProtectionLevel(permissionInfo: PermissionInfo): String {
        val base = when (permissionInfo.protection) {
            PermissionInfo.PROTECTION_NORMAL -> "normal"
            PermissionInfo.PROTECTION_DANGEROUS -> "dangerous"
            PermissionInfo.PROTECTION_SIGNATURE -> "signature"
            PermissionInfo.PROTECTION_INTERNAL -> "internal"
            else -> "unknown"
        }

        val flags = mutableListOf<String>()
        if ((permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_PRIVILEGED) != 0) {
            flags.add("privileged")
        }
        if ((permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_INSTALLER) != 0) {
            flags.add("installer")
        }

        return if (flags.isEmpty()) base else "$base|${flags.joinToString("|")}"
    }
} 