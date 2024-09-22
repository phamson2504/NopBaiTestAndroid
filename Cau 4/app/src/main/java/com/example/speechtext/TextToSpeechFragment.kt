package com.example.speechtext

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.speechtext.databinding.FragmentTextToSpeechBinding
import java.util.Locale


class TextToSpeechFragment : Fragment(R.layout.fragment_text_to_speech), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech
    private var _binding: FragmentTextToSpeechBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextToSpeechBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textToSpeech = TextToSpeech(requireContext(), this)

        binding.micSpeak.setOnClickListener {
            val text = binding.edTextToSpeech.text.toString()
            if (text.isNotEmpty()) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech.shutdown()
        _binding = null
    }

}