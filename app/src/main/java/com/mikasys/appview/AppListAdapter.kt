package com.mikasys.appview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AppListAdapter(
    private var apps: List<AppInfo>,
    private val onItemClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private var filteredApps = apps.toList()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.app_icon)
        val nameView: TextView = view.findViewById(R.id.app_name)
        val versionView: TextView = view.findViewById(R.id.app_version)
        val typeView: TextView = view.findViewById(R.id.app_type)
        val packageView: TextView = view.findViewById(R.id.app_package)
        val installDateView: TextView = view.findViewById(R.id.install_date)
        val updateDateView: TextView = view.findViewById(R.id.update_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = filteredApps[position]
        holder.iconView.setImageDrawable(app.icon)
        holder.nameView.text = app.name
        holder.versionView.text = app.versionName
        holder.typeView.text = if (app.isSystemApp) "System" else "User"
        holder.packageView.text = app.packageName
        holder.installDateView.text = dateFormat.format(app.installDate)
        holder.updateDateView.text = dateFormat.format(app.lastUpdateTime)
        
        holder.itemView.setOnClickListener { onItemClick(app) }
    }

    override fun getItemCount() = filteredApps.size

    fun updateList(newApps: List<AppInfo>) {
        apps = newApps
        filter("")
    }

    fun filter(query: String) {
        filteredApps = apps.filter { app ->
            app.name.contains(query, ignoreCase = true) ||
            app.packageName.contains(query, ignoreCase = true)
        }
        notifyDataSetChanged()
    }

    fun sort(by: SortCriteria) {
        filteredApps = when (by) {
            SortCriteria.NAME -> filteredApps.sortedBy { it.name }
            SortCriteria.PACKAGE -> filteredApps.sortedBy { it.packageName }
            SortCriteria.TYPE -> filteredApps.sortedBy { it.isSystemApp }
            SortCriteria.DATE -> filteredApps.sortedBy { it.installDate }
            SortCriteria.SIZE -> filteredApps.sortedBy { it.size }
        }
        notifyDataSetChanged()
    }
}

enum class SortCriteria {
    NAME, PACKAGE, TYPE, DATE, SIZE
} 