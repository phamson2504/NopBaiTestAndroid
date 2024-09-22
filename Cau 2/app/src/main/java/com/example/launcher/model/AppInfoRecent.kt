package com.example.launcher.model

import android.graphics.drawable.Drawable

data class AppInfoRecent (val label: String,
                    val packageName: String,
                    val icon: Drawable,
                    val lastTimeUsed: Long ?= 0)