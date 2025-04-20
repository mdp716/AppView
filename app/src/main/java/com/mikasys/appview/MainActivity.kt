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

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AppListAdapter
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize views
        recyclerView = findViewById(R.id.appList)
        progressBar = findViewById(R.id.progressBar)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppListAdapter(emptyList()) { appInfo ->
            // Handle app click - launch AppDetailActivity
            startActivity(AppDetailActivity.createIntent(this, appInfo.packageName))
        }
        recyclerView.adapter = adapter

        // Load apps
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        mainScope.launch {
            try {
                // Show loading
                progressBar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE

                // Load apps in background
                val apps = withContext(Dispatchers.IO) {
                    packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                        .mapNotNull { packageInfo ->
                            packageInfo.applicationInfo?.let { appInfo ->
                                try {
                                    AppInfo(
                                        name = appInfo.loadLabel(packageManager)?.toString() 
                                            ?: packageInfo.packageName,
                                        packageName = packageInfo.packageName,
                                        icon = appInfo.loadIcon(packageManager),
                                        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                                        installDate = Date(packageInfo.firstInstallTime),
                                        lastUpdateTime = Date(packageInfo.lastUpdateTime),
                                        size = appInfo.sourceDir?.let { File(it).length() } ?: 0L,
                                        versionName = packageInfo.versionName ?: ""
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                        .sortedBy { it.name.lowercase() }
                }

                // Update UI
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateList(apps)  // Changed from updateApps to updateList
            } catch (e: Exception) {
                e.printStackTrace()
                progressBar.visibility = View.GONE
                // You might want to show an error message to the user here
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
                // Handle About action
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