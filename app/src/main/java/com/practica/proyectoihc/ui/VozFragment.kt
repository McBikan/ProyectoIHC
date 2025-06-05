package com.practica.proyectoihc.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.practica.proyectoihc.R
import com.practica.proyectoihc.ui.base.BaseMenuFragment
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class VozFragment : BaseMenuFragment() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private var tts: TextToSpeech? = null
    private var tvEstadoVoz: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_voz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verificarPermisos()

        tts = TextToSpeech(requireContext()) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }

        val ivMenu = view.findViewById<View>(R.id.ivMenu)
        setupMenuNavigation(ivMenu)

        val btnMic = view.findViewById<ImageButton>(R.id.btnMicrofono)
        tvEstadoVoz = view.findViewById(R.id.tvEstadoVoz)

        inicializarReconocimientoDeVoz()

        btnMic.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        tvEstadoVoz?.visibility = View.VISIBLE
                        tvEstadoVoz?.text = "Escuchando..."
                        iniciarEscucha()
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            100
                        )
                        Toast.makeText(
                            requireContext(),
                            "Permiso de micrófono requerido",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    tvEstadoVoz?.visibility = View.GONE
                    if (::speechRecognizer.isInitialized) {
                        speechRecognizer.stopListening()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun verificarPermisos() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    private fun inicializarReconocimientoDeVoz() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    private fun iniciarEscucha() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                tvEstadoVoz?.visibility = View.GONE
                val texto = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                texto?.let {
                    enviarTextoAGemini(it)
                }
            }

            override fun onError(error: Int) {
                tvEstadoVoz?.visibility = View.GONE
                val mensaje = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                    SpeechRecognizer.ERROR_CLIENT -> "Error de cliente"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso insuficiente"
                    SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de red agotado"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ninguna coincidencia"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                    SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tiempo de espera agotado"
                    else -> "Error desconocido"
                }
                Log.e("VozFragment", "Error al escuchar ($error): $mensaje")
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(speechIntent)
    }

    private fun enviarTextoAGemini(mensaje: String) {
        val apiKey = "AIzaSyCAH1-np6JYpEr52NOxgdBVMVT1WOabdtU"
        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"

        val json = """
            {
              "contents": [{
                "parts": [{
                  "text": "Eres un consejero emocional empático. Usuario dijo: \"$mensaje\". ¿Cómo le responderías?"
                }]
              }]
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GeminiAPI", "Fallo conexión", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                try {
                    val reply = JSONObject(body)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    activity?.runOnUiThread {
                        convertirTextoAVoz(reply)
                    }
                } catch (e: Exception) {
                    Log.e("GeminiAPI", "Error al procesar respuesta: $body", e)
                }
            }
        })
    }

    private fun convertirTextoAVoz(texto: String) {
        tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer.destroy()
        tts?.shutdown()
    }
}
