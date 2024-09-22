package com.example.speechtext

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.speechtext.databinding.FragmentSpeechToTextBinding
import java.util.Locale


class SpeechToTextFragment : Fragment(R.layout.fragment_speech_to_text) {

    private var _binding: FragmentSpeechToTextBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeechToTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE
            )
        }

        binding.micSpeak.setOnClickListener {
            startSpeechRecognition()
        }
    }

    private fun startSpeechRecognition() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }

        val packageManager = requireActivity().packageManager
        val intents = packageManager.queryIntentActivities(speechIntent, 0)
        if (intents.isNotEmpty()) {
            startActivityForResult(speechIntent, REQUEST_CODE)
        } else {
            Toast.makeText(
                requireContext(),
                "No voice recognition service available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.txtResult.text = results?.get(0) ?: "No speech recognized"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


