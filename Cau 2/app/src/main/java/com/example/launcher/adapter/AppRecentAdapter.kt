package com.example.launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.launcher.model.AppInfoRecent
import com.example.launcher.R

object RecentAppDiff: DiffUtil.ItemCallback<AppInfoRecent>(){
    override fun areItemsTheSame(oldItem: AppInfoRecent, newItem: AppInfoRecent): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AppInfoRecent, newItem: AppInfoRecent): Boolean {
        return oldItem.packageName == newItem.packageName
    }
}
class AppRecentAdapter :
    ListAdapter<AppInfoRecent, AppRecentAdapter.AppRecentViewHolder>(RecentAppDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppRecentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
        return AppRecentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppRecentViewHolder, position: Int) {
        val item = getItem(position)
        holder.appIcon.setImageDrawable(item.icon)
        holder.appName.text = item.label
        //click to app
        holder.itemView.setOnClickListener {
            val launchIntent = it.context.packageManager.getLaunchIntentForPackage(item.packageName)
            it.context.startActivity(launchIntent)
        }
    }

    class AppRecentViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)
    }
}