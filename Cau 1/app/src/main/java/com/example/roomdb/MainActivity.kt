package com.example.roomdb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        val studentDao = db.studentDao()

        lifecycleScope.launch {
            val newStudent =
                Student(name = "son", age = 24, address = "District 12, Ho Chi Minh City", gender = "male")
            studentDao.insert(newStudent)
        }

    }
}