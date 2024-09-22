package com.example.launcher.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.launcher.databinding.AppFolderBinding
import com.example.launcher.databinding.AppItemBinding
import android.util.Base64
import com.example.launcher.model.AppInLauncher
import com.example.launcher.model.AppInfo
import com.example.launcher.R

object AppDiffUtil: DiffUtil.ItemCallback<AppInLauncher>(){
    override fun areItemsTheSame(oldItem: AppInLauncher, newItem: AppInLauncher): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AppInLauncher, newItem: AppInLauncher): Boolean {
        return false
    }
}

class AppAdapter : ListAdapter<AppInLauncher, RecyclerView.ViewHolder>(AppDiffUtil) {

    companion object{
        private const val FOLDER_TYPE = 1
        private const val APP_TYPE = 2
    }
    var callbacks: Callbacks? = null

    interface Callbacks{
        fun onClickFolder(item: AppInLauncher.Folder)
        fun onClickApp(item: AppInfo)
        fun onLongClickApp(item: AppInfo)
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item is AppInLauncher.Folder) FOLDER_TYPE else APP_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            FOLDER_TYPE -> {
                val binding = AppFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                FolderViewHolder(binding)
            }
            else -> {
                val binding = AppItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AppViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is AppViewHolder -> holder.bind(getItem(position) as AppInLauncher.App)
            is FolderViewHolder -> holder.bind(getItem(position) as AppInLauncher.Folder)
        }
    }

    inner class AppViewHolder(private val binding: AppItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInLauncher.App) {
            val appInfo = app.item
            binding.appName.text = appInfo.label
            binding.appIcon.setImageDrawable(base64ToDrawable(app.item.iconBase64, itemView.context))

            itemView.setOnClickListener {
                callbacks?.onClickApp(appInfo)
            }
            itemView.setOnLongClickListener {
                callbacks?.onLongClickApp(appInfo)
                true
            }
        }
    }

    inner class FolderViewHolder(private val binding: AppFolderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(folder: AppInLauncher.Folder) {
            binding.folderName.text = folder.name
            if (folder.apps.isNotEmpty()) {
                binding.folderIcon.setImageDrawable(base64ToDrawable(folder.apps.first().iconBase64, itemView.context))
            }else{
                binding.folderIcon.setImageResource(R.drawable.ic_launcher_background)
            }
            if (folder.apps.size > 1) {
                binding.folderIcon2.setImageDrawable(base64ToDrawable(folder.apps[1].iconBase64, itemView.context))
            }else{
                binding.folderIcon2.setImageResource(R.drawable.ic_launcher_background)
            }

            if (folder.apps.size > 2) {
                binding.folderIcon3.setImageDrawable(base64ToDrawable(folder.apps[2].iconBase64, itemView.context))
            }else{
                binding.folderIcon3.setImageResource(R.drawable.ic_launcher_background)
            }

            if (folder.apps.size > 3) {
                binding.folderIcon4.setImageDrawable(base64ToDrawable(folder.apps[3].iconBase64, itemView.context))
            }else{
                binding.folderIcon4.setImageResource(R.drawable.ic_launcher_background)
            }

            itemView.setOnClickListener {
                callbacks?.onClickFolder(folder)
            }
        }
    }
    fun base64ToDrawable(base64: String, context: Context): Drawable {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        return BitmapDrawable(context.resources, bitmap)
    }
}
