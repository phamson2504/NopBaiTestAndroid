package com.example.launcher.db

import android.content.Context
import android.content.SharedPreferences
import com.example.launcher.model.AppInLauncher
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FolderManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("folder_storage", Context.MODE_PRIVATE)

    private val gson = Gson()


    fun saveFolders(folders: List<AppInLauncher.Folder>) {
        val json = gson.toJson(folders)
        sharedPreferences.edit().putString("folders", json).apply()
    }

    fun loadFolders(): List<AppInLauncher.Folder> {
        val json = sharedPreferences.getString("folders", null) ?: return emptyList()
        val type = object : TypeToken<List<AppInLauncher.Folder>>() {}.type
        return gson.fromJson(json, type)
    }

    fun updateFolder(updatedFolder: AppInLauncher.Folder) {
        val allFolder = loadFolders().toMutableList()
        val existingFolderIndex = allFolder.indexOfFirst { it.name == updatedFolder.name }
        if (existingFolderIndex != -1) {
            allFolder[existingFolderIndex] = updatedFolder
        }
        if (allFolder[existingFolderIndex].apps.size == 0 ){
            allFolder.removeAt(existingFolderIndex)
        }
        saveFolders(allFolder)
    }

    fun addFolder(folder: AppInLauncher.Folder) {
        val folders = loadFolders().toMutableList()
        if (!folders.any { it.name == folder.name }) {
            folders.add(folder)
            saveFolders(folders)
        } else {
            throw IllegalArgumentException("Folder with the same name already exists")
        }
    }

    fun isFolderExists(name: String): Boolean {
        val folders = loadFolders()
        return folders.any { it.name == name }
    }

}