//forcing a commit again
package com.mikasys.appview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), AppListAdapter.OnAppClickListener {

    companion object {
        const val REFRESHING_MESSAGE = "Refreshing..."
        const val SORT_CLICKED_MESSAGE = "Sort clicked (implement sorting logic)"
        const val MORE_CLICKED_MESSAGE = "More clicked (implement more options)"
    }

    private lateinit var rvAppList: RecyclerView
    private lateinit var ivLoading: ImageView
    private lateinit var appListAdapter: AppListAdapter
    private var appList: List<AppInfo> = emptyList()
    private var filteredAppList: List<AppInfo> = emptyList()
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the Toolbar as the SupportActionBar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize views
        rvAppList = findViewById(R.id.rv_app_list)
        ivLoading = findViewById(R.id.iv_loading)

        // Set up RecyclerView
        rvAppList.layoutManager = LinearLayoutManager(this)

        // Create the adapter
        appListAdapter = AppListAdapter(emptyList())
        appListAdapter.setOnAppClickListener(this)
        rvAppList.adapter = appListAdapter
        rvAppList.visibility = View.GONE // Hide RecyclerView initially

        // Show loading icon
        ivLoading.visibility = View.VISIBLE

        // Load data
        loadApps()
    }

    private fun loadApps() {
        ivLoading.visibility = View.VISIBLE
        rvAppList.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val loadedApps = AppListUtil.getInstalledApps(this@MainActivity)
                withContext(Dispatchers.Main) {
                    appList = loadedApps.sortedBy { it.appName.trim().lowercase() }
                    filteredAppList = appList
                    appListAdapter.updateData(filteredAppList)
                    ivLoading.visibility = View.GONE
                    rvAppList.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to load apps: ${e.message}", Toast.LENGTH_SHORT).show()
                    ivLoading.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText?.trim())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                Toast.makeText(this, REFRESHING_MESSAGE, Toast.LENGTH_SHORT).show()
                loadApps()
                true
            }

            R.id.action_sort -> {
                Toast.makeText(this, SORT_CLICKED_MESSAGE, Toast.LENGTH_SHORT).show()
                // Implement sorting logic here
                true
            }

            R.id.action_search -> true
            R.id.action_more -> {
                Toast.makeText(this, MORE_CLICKED_MESSAGE, Toast.LENGTH_SHORT).show()
                // Implement more options logic here
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAppClick(packageName: String) {
        val intent = AppDetailActivity.createIntent(this, packageName)
        startActivity(intent)
    }

    private fun filterApps(query: String?) {
        filteredAppList = if (query.isNullOrBlank()) {
            appList
        } else {
            appList.filter {
                it.appName.contains(query, ignoreCase = true)
            }
        }
        appListAdapter.updateData(filteredAppList)
    }
}