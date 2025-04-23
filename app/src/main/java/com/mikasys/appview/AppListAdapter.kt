package com.mikasys.appview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AppListAdapter(
    private var apps: List<AppInfo>,
    private val onItemClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppListViewHolder>() {

    // ViewHolder holds references to the views in list_item_app.xml
    class AppListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconImageView: ImageView = view.findViewById(R.id.iv_app_icon)
        val nameTextView: TextView = view.findViewById(R.id.tv_app_name)
        val packageTextView: TextView = view.findViewById(R.id.tv_package_name)
        val installDateTextView: TextView = view.findViewById(R.id.tv_app_install_time)
        val updateDateTextView: TextView = view.findViewById(R.id.tv_app_update_time)
        val versionTextView: TextView = view.findViewById(R.id.tv_app_version)
        val typeTextView: TextView = view.findViewById(R.id.tv_app_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppListViewHolder {
        // Inflate the layout for each item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_app, parent, false)
        return AppListViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppListViewHolder, position: Int) {
        val appInfo = apps[position]
        val context = holder.itemView.context

        // Add debug logging
        android.util.Log.d("AppListAdapter", "Binding app: ${appInfo.appName}")
        
        // Bind data to the views with forced visibility
        holder.nameTextView.apply {
            text = appInfo.appName
            visibility = View.VISIBLE
        }
        
        holder.packageTextView.apply {
            text = appInfo.packageName
            visibility = View.VISIBLE
        }
        
        holder.versionTextView.apply {
            text = appInfo.versionName
            visibility = View.VISIBLE
        }
        
        holder.installDateTextView.apply {
            text = context.getString(R.string.installed_format, AppListUtil.formatDate(appInfo.installTime))
            visibility = View.VISIBLE
        }
        
        holder.updateDateTextView.apply {
            text = context.getString(R.string.last_updated_format, AppListUtil.formatDate(appInfo.lastUpdateTime))
            visibility = View.VISIBLE
        }
        
        holder.typeTextView.apply {
            text = if (appInfo.isSystemApp) {
                context.getString(R.string.system_app)
            } else {
                context.getString(R.string.user_app)
            }
            visibility = View.VISIBLE
        }

        // Load icon using Glide
        Glide.with(context)
            .load(appInfo.icon)
            .into(holder.iconImageView)

        // Set click listener for the entire item
        holder.itemView.setOnClickListener {
            onItemClick(appInfo)
        }
    }

    override fun getItemCount(): Int = apps.size

    // Optional: Function to update the list data if it changes (e.g., after refresh)
    fun updateData(newApps: List<AppInfo>) {
        val diffResult = DiffUtil.calculateDiff(AppDiffCallback(apps, newApps))
        apps = newApps
        diffResult.dispatchUpdatesTo(this)
    }

    private var originalApps: List<AppInfo> = emptyList()
    private var currentQuery: String = ""

    fun updateList(newApps: List<AppInfo>) {
        originalApps = newApps
        filter(currentQuery) // Apply current filter to new data
    }

    fun filter(query: String) {
        currentQuery = query
        val filteredList = if (query.isEmpty()) {
            originalApps
        } else {
            originalApps.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
        updateData(filteredList)
    }

    fun sort(criteria: SortCriteria) {
        val sortedList = when (criteria) {
            SortCriteria.NAME -> apps.sortedBy { it.appName.lowercase() }
            SortCriteria.PACKAGE -> apps.sortedBy { it.packageName.lowercase() }
            SortCriteria.TYPE -> apps.sortedBy { it.isSystemApp }
            SortCriteria.DATE -> apps.sortedByDescending { it.installTime }
            SortCriteria.SIZE -> apps.sortedByDescending { it.versionCode }
        }
        updateData(sortedList)
    }

    class AppDiffCallback(
        private val oldList: List<AppInfo>,
        private val newList: List<AppInfo>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldApp = oldList[oldItemPosition]
            val newApp = newList[newItemPosition]
            return oldApp.packageName == newApp.packageName
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldApp = oldList[oldItemPosition]
            val newApp = newList[newItemPosition]
            return oldApp == newApp
        }
    }
}