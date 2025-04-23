package com.mikasys.appview

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ProgressBar
import kotlinx.coroutines.*
import java.util.Date
import java.io.File
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AppListAdapter
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set status bar to light mode (dark icons)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true  // This makes the status bar icons dark
        
        // Initialize views
        recyclerView = findViewById(R.id.rv_app_list)
        progressBar = findViewById(R.id.iv_loading)
        
        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinator_layout)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply margin to AppBarLayout instead of padding
            val appBarLayout = findViewById<AppBarLayout>(R.id.appbar_layout)
            val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            params.topMargin = insets.top
            appBarLayout.layoutParams = params
            
            // Ensure RecyclerView doesn't go under system bars
            recyclerView.setPadding(0, 0, 0, insets.bottom)
            
            windowInsets
        }

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppListAdapter(emptyList()) { appInfo ->
            startActivity(AppDetailActivity.createIntent(this, appInfo.packageName))
        }
        recyclerView.adapter = adapter

        // Load apps
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        mainScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE

                val apps = withContext(Dispatchers.IO) {
                    packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                        .mapNotNull { packageInfo ->
                            packageInfo.applicationInfo?.let { appInfo ->
                                try {
                                    AppInfo(
                                        appName = appInfo.loadLabel(packageManager).toString(),
                                        packageName = packageInfo.packageName,
                                        versionName = packageInfo.versionName ?: "",
                                        versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                            packageInfo.longVersionCode
                                        } else {
                                            @Suppress("DEPRECATION")
                                            packageInfo.versionCode.toLong()
                                        },
                                        installTime = packageInfo.firstInstallTime,
                                        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                                        icon = appInfo.loadIcon(packageManager),
                                        lastUpdateTime = packageInfo.lastUpdateTime
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                        .sortedBy { it.appName.lowercase() }
                }

                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateList(apps)
            } catch (e: Exception) {
                e.printStackTrace()
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        // Set up search functionality
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_by_name -> {
                adapter.sort(SortCriteria.NAME)
                true
            }
            R.id.sort_by_package -> {
                adapter.sort(SortCriteria.PACKAGE)
                true
            }
            R.id.sort_by_type -> {
                adapter.sort(SortCriteria.TYPE)
                true
            }
            R.id.sort_by_date -> {
                adapter.sort(SortCriteria.DATE)
                true
            }
            R.id.sort_by_size -> {
                adapter.sort(SortCriteria.SIZE)
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About AppView")
            .setMessage("AppView is a simple application viewer that helps you manage and explore installed applications on your device.")
            .setPositiveButton("OK", null)
            .show()
    }
}