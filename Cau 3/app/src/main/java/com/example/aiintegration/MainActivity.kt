package com.example.aiintegration

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.aiintegration.databinding.ActivityMainBinding
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var generativeModel: GenerativeModel
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding?.let { setContentView(it.root) }



        binding?.buttonSend?.setOnClickListener {
            binding?.txtResult?.text = ""
            binding?.progressBar?.visibility = ProgressBar.VISIBLE
            lifecycleScope.launch(Dispatchers.IO){
                performGenerate()
            }
        }
        loadModel()
    }

    private suspend fun performGenerate(){
        try {
            withContext(Dispatchers.Main) {
                binding?.apply {
                    llResult.isVisible = false
                    progressBar.isVisible = true
                    buttonSend.isEnabled = false
                }

            }
            val query = binding?.editTextSearch?.text.toString()
            val response = generativeModel.generateContent(query)
            withContext(Dispatchers.Main) {
                binding?.apply {
                    tvQuestion.text = query
                    txtResult.text = response.text
                    editTextSearch.text.clear()
                    buttonSend.isEnabled = true
                    llResult.isVisible = true
                    progressBar.isVisible = false

                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding?.progressBar?.isVisible = false
            }
        }
    }

    private fun loadModel(){
        generativeModel =
            GenerativeModel(
                // Specify a Gemini model appropriate for your use case
                modelName = "gemini-1.5-flash",
                // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                apiKey = Constants.apiKey)
    }
}