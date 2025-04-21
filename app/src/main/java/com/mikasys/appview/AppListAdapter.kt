package com.mikasys.appview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AppListAdapter(private var apps: List<AppInfo>) :
    RecyclerView.Adapter<AppListAdapter.AppListViewHolder>() {

    // Interface for item click events
    interface OnAppClickListener {
        fun onAppClick(packageName: String)
    }

    private var appClickListener: OnAppClickListener? = null

    // Set click listener
    fun setOnAppClickListener(listener: OnAppClickListener) {
        appClickListener = listener
    }

    // ViewHolder holds references to the views in list_item_app.xml
    class AppListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconImageView: ImageView = view.findViewById(R.id.iv_app_icon)
        val nameTextView: TextView = view.findViewById(R.id.tv_app_name)
        val packageTextView: TextView = view.findViewById(R.id.tv_package_name)
        val installDateTextView: TextView = view.findViewById(R.id.tv_app_install_time)
        val updateDateTextView: TextView = view.findViewById(R.id.tv_app_update_time) // Updated reference
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

        // Bind data to the views
        holder.nameTextView.text = appInfo.appName
        holder.packageTextView.text = appInfo.packageName
        holder.versionTextView.text = appInfo.versionName
        // Update to use formatDate
        holder.installDateTextView.text = context.getString(R.string.installed_date_format,AppListUtil.formatDate(appInfo.installTime))
        // Bind update date
        holder.updateDateTextView.text = context.getString(R.string.updated_date_format,AppListUtil.formatDate(appInfo.lastUpdateTime)) // Updated text
        holder.typeTextView.text = if (appInfo.isSystemApp) {
            context.getString(R.string.app_type_system) // Use string resource
        } else {
            context.getString(R.string.app_type_user) // Use string resource
        }

        // Load icon using Glide (or Picasso)
        Glide.with(context)
            .load(appInfo.icon)
            // Optional: Add placeholder and error drawables
            // .placeholder(R.drawable.ic_placeholder)
            // .error(R.drawable.ic_error)
            .into(holder.iconImageView)

        // Set click listener for the entire item
        holder.itemView.setOnClickListener {
            appClickListener?.onAppClick(appInfo.packageName)
        }
    }

    override fun getItemCount(): Int = apps.size

    // Optional: Function to update the list data if it changes (e.g., after refresh)
    fun updateData(newApps: List<AppInfo>) {
        val diffResult = DiffUtil.calculateDiff(AppDiffCallback(apps, newApps))
        apps = newApps
        diffResult.dispatchUpdatesTo(this)
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