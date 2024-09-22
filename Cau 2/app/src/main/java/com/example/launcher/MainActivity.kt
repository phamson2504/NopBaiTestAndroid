package com.example.launcher

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.launcher.adapter.AppAdapter
import com.example.launcher.adapter.AppRecentAdapter
import com.example.launcher.adapter.DialogAppAdapter
import com.example.launcher.databinding.ActivityMainBinding
import com.example.launcher.db.FolderManager
import com.example.launcher.model.AppInLauncher
import com.example.launcher.model.AppInfo
import com.example.launcher.model.AppInfoRecent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(), AppAdapter.Callbacks {
    private lateinit var binding: ActivityMainBinding
    private var appAdapter: AppAdapter? = null
    private var recentAppAdapter: AppRecentAdapter? = null
    private lateinit var folderManager: FolderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the FolderManager
        folderManager = FolderManager(this)

        recentAppAdapter = AppRecentAdapter()
        binding.recentAppsRecyclerView.adapter = recentAppAdapter
        binding.recentAppsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        appAdapter = AppAdapter().apply {
            callbacks = this@MainActivity
        }
        val gridLayoutManager = GridLayoutManager(this, 4)
        binding.allAppsRecyclerView.layoutManager = gridLayoutManager
        binding.allAppsRecyclerView.adapter = appAdapter
    }

    override fun onResume() {
        super.onResume()
        checkUsageStatsPermission()
    }

    override fun onClickFolder(item: AppInLauncher.Folder) {
        showAppsInFolder(item)
    }

    override fun onClickApp(item: AppInfo) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(item.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onLongClickApp(item: AppInfo) {

        val folders = folderManager.loadFolders()
        val folderNames = folders.map { it.name }

        val builder = AlertDialog.Builder(this)
            .setTitle("Select one")
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice)
        arrayAdapter.addAll("Add to new folder")
        arrayAdapter.addAll(folderNames)
        builder.setAdapter(arrayAdapter, object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when (p1) {
                    0 -> createNewFolder(item)
                    else ->{
                        val selectedFolder = folders[p1-1]
                        addToFolder(selectedFolder, item)
                    }
                }
            }
        })
        builder.create().show()
    }
    private fun addToFolder(folder: AppInLauncher.Folder, app: AppInfo) {
        folder.apps.add(app)

        val updatedFolders = folderManager.loadFolders().map {
            if (it.id == folder.id) folder else it
        }
        folderManager.saveFolders(updatedFolders)

        AlertDialog.Builder(this)
            .setTitle("App Added")
            .setMessage("${app.label} added to folder '${folder.name}'")
            .setPositiveButton("OK", null)
            .show()

        reloadDataForAllApps()
    }

    private fun createNewFolder(app: AppInfo) {
        val input = EditText(this)
        input.hint = "Folder name"

        AlertDialog.Builder(this)
            .setTitle("Create New Folder")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val folderName = input.text.toString()

                if (folderName.isBlank()) {
                    Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                } else if (folderManager.isFolderExists(folderName)) {
                    Toast.makeText(this, "Folder with the same name already exists", Toast.LENGTH_SHORT).show()
                } else {
                    val newFolder = AppInLauncher.Folder(
                        id = System.currentTimeMillis(),
                        name = folderName,
                        apps = mutableListOf(app)
                    )

                    folderManager.addFolder(newFolder)
                    reloadDataForAllApps()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()

    }
    private fun getInstalledApps(): MutableList<AppInfo> {
        val appList = mutableListOf<AppInfo>()
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val applications = packageManager.queryIntentActivities(intent, 0)

        for (appInfo in applications) {
            val label: String = appInfo.loadLabel(packageManager).toString()
            val packageName: String = appInfo.activityInfo.packageName
            val icon: Drawable = appInfo.loadIcon(packageManager)

            val iconBase64 = drawableToBase64(icon)

            val appInformation = AppInfo(label, packageName, iconBase64 )
            appList.add(appInformation)
        }
        return appList
    }

    private fun usageStatsToAppInfo(
        context: Context,
        usageStats: UsageStats,
        installedApps: List<AppInfo>
    ): AppInfoRecent? {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(usageStats.packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            val appIcon = applicationInfo.loadIcon(packageManager)

            //Check if the app exists in the installed list
            val isInstalled = installedApps.any { it.packageName == usageStats.packageName }

            if (isInstalled) {
                AppInfoRecent(
                    packageName = usageStats.packageName,
                    label = appName,
                    icon = appIcon,
                    lastTimeUsed = usageStats.lastTimeUsed
                )
            } else {
                null
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null //Returns null if no app information is found
        }
    }

    private fun getApps() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()

        // Get a list of UsageStats in the last 10 minus
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 1000 * 60 * 10,
            currentTime
        )

        lifecycleScope.launch(Dispatchers.IO){
            withContext(Dispatchers.Main){
                binding.loadingRecent.isVisible = true
                binding.loading.isVisible = true
                binding.allAppsRecyclerView.isVisible = false
            }
            val apps = getInstalledApps()
            val appInLauncher = mutableListOf<AppInLauncher>()
            val allAppInFolder = folderManager.loadFolders().flatMap { it.apps }
            appInLauncher.addAll(folderManager.loadFolders())
            apps.forEach { appInfo ->
                if (!allAppInFolder.contains(appInfo)){
                    appInLauncher.add(AppInLauncher.App(appInfo))
                }

            }

            withContext(Dispatchers.Main){
                binding.loading.isVisible = false
                binding.allAppsRecyclerView.isVisible = true
                appAdapter?.submitList(appInLauncher)
            }

            val recents = usageStatsList
                .mapNotNull { usageStatsToAppInfo(this@MainActivity, it, apps) }
                .sortedByDescending { it.lastTimeUsed }

            withContext(Dispatchers.Main){
                binding.loadingRecent.isVisible = false
                recentAppAdapter?.submitList(recents)
                if(recents.isEmpty()){
                    binding.noRecentAppsText.isVisible = true
                    binding.recentAppsRecyclerView.isVisible = false
                }else{
                    binding.noRecentAppsText.isVisible = false
                    binding.recentAppsRecyclerView.isVisible = true
                }
            }
        }
    }

    private fun reloadDataForAllApps(){
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                binding.loading.isVisible = true
                binding.allAppsRecyclerView.isVisible = false
            }
            val apps = getInstalledApps()
            val appInLauncher = mutableListOf<AppInLauncher>()
            val allAppInFolder = folderManager.loadFolders().flatMap { it.apps }
            appInLauncher.addAll(folderManager.loadFolders())
            apps.forEach { appInfo ->
                if (!allAppInFolder.contains(appInfo)) {
                    appInLauncher.add(AppInLauncher.App(appInfo))
                }
            }

            withContext(Dispatchers.Main) {
                binding.loading.isVisible = false
                binding.allAppsRecyclerView.isVisible = true
                appAdapter?.submitList(appInLauncher.toMutableList())
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun checkUsageStatsPermission() {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(),
            packageName
        )

        if (mode != AppOpsManager.MODE_ALLOWED) {
            Toast.makeText(this, "Please grant usage access", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        } else {
            getApps()
        }
    }
    private fun drawableToBase64(drawable: Drawable): String {
        val bitmap = drawableToBitmap(drawable)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun showAppsInFolder(folder: AppInLauncher.Folder) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_folder_apps, null)
        dialogBuilder.setView(dialogView)

        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerViewFolderApps)
        val gridLayoutManager = GridLayoutManager(this, 4)
        recyclerView.layoutManager = gridLayoutManager

        recyclerView.adapter = DialogAppAdapter { appToDelete ->
            folder.apps.remove(appToDelete)
            folderManager.updateFolder(folder)
            (recyclerView.adapter as DialogAppAdapter).submitList(folder.apps.toList())
            reloadDataForAllApps()
        }.apply {
            submitList(folder.apps.toList())
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

}