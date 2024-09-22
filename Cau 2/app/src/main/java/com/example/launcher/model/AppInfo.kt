package com.example.launcher.model

data class AppInfo(
    val label: String,
    val packageName: String,
    val iconBase64: String,
)

sealed class AppInLauncher{
    data class Folder(val id: Long, val name: String, val apps: MutableList<AppInfo>): AppInLauncher()
    data class App(val item: AppInfo): AppInLauncher()
}