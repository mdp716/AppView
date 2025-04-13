package com.mikasys.appview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Correct import
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Consider using Coroutines or AsyncTask for background loading
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), AppListAdapter.OnAppClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter
    private var appList: List<AppInfo> = emptyList()

    // Coroutine scope for background tasks
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup RecyclerView
        recyclerView = findViewById(R.id.rv_app_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add dividers between items
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // Initialize adapter with empty list initially
        appListAdapter = AppListAdapter(appList)
        appListAdapter.setOnAppClickListener(this) // Set the click listener
        recyclerView.adapter = appListAdapter

        // Load app list in the background
        loadApps()
    }

    private fun loadApps() {
        // Show loading indicator (optional)
        activityScope.launch(Dispatchers.IO) { // Run fetching on background thread
            val loadedApps = AppListUtil.getInstalledApps(this@MainActivity)
            withContext(Dispatchers.Main) { // Switch back to main thread to update UI
                appList = loadedApps.sortedBy { it.appName.lowercase() } // Sort apps alphabetically
                appListAdapter.updateData(appList)
                // Hide loading indicator (optional)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        // Setup SearchView if needed
        // val searchItem = menu.findItem(R.id.action_search)
        // val searchView = searchItem.actionView as SearchView
        // searchView.setOnQueryTextListener(...)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
                loadApps() // Reload the list
                true
            }
            R.id.action_sort -> {
                Toast.makeText(this, "Sort clicked (implement sorting logic)", Toast.LENGTH_SHORT).show()
                // Implement different sorting options here
                true
            }
            R.id.action_search -> {
                // Handled by SearchView setup in onCreateOptionsMenu usually
                true
            }
            R.id.action_more -> {
                Toast.makeText(this, "More clicked (implement more options)", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel() // Cancel coroutines when activity is destroyed
    }

    // Implementation of OnAppClickListener interface
    override fun onAppClick(packageName: String) {
        // Open app detail activity when an app is clicked
        val intent = AppDetailActivity.createIntent(this, packageName)
        startActivity(intent)
    }
}