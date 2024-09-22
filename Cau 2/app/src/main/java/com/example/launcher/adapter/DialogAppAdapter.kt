package com.example.launcher.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.launcher.model.AppInfo
import com.example.launcher.databinding.AppItemBinding

object  DialogAppDiff: DiffUtil.ItemCallback<AppInfo>(){
    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem.packageName == newItem.packageName
    }
}

class DialogAppAdapter ( private val onAppDelete: (AppInfo) -> Unit): ListAdapter<AppInfo, DialogAppAdapter.DialogAppViewHolder>(
    DialogAppDiff
) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DialogAppViewHolder {
        val binding = AppItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DialogAppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DialogAppViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        //click to app
        holder.itemView.setOnClickListener {
            val launchIntent = it.context.packageManager.getLaunchIntentForPackage(item.packageName)
            it.context.startActivity(launchIntent)
        }
        holder.itemView.setOnLongClickListener {
            showDeleteConfirmation(it.context, item)
            true
        }
    }
    inner class DialogAppViewHolder(private val binding: AppItemBinding) : RecyclerView.ViewHolder(binding.root)  {
        fun bind(app: AppInfo) {
            binding.appName.text = app.label
            binding.appIcon.setImageDrawable(base64ToDrawable(app.iconBase64, itemView.context))
        }
    }

    fun base64ToDrawable(base64: String, context: Context): Drawable {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        return BitmapDrawable(context.resources, bitmap)
    }
    private fun showDeleteConfirmation(context: Context, app: AppInfo) {
        AlertDialog.Builder(context)
            .setTitle("Delete App")
            .setMessage("Are you sure you want to delete ${app.label} from this folder?")
            .setPositiveButton("Yes") { _, _ ->
                onAppDelete(app)
            }
            .setNegativeButton("No", null)
            .show()
    }
}