package com.practica.proyectoihc.ui

import com.practica.proyectoihc.ui.base.BaseMenuFragment
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.practica.proyectoihc.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread
import androidx.navigation.fragment.findNavController
import org.json.JSONObject


class EmocionesFragment : BaseMenuFragment() {

    private val sampleRate = 16000
    private val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val recordDurationMs = 4000

    private lateinit var audioFile: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emociones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivMenu = view.findViewById<View>(R.id.ivMenu)
        setupMenuNavigation(ivMenu)

        val btnMicrofono = view.findViewById<ImageButton>(R.id.btnMicrofono)

        btnMicrofono.setOnClickListener {
            verificarPermisoYGrabar()
        }
    }

    private fun verificarPermisoYGrabar() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)

        } else {
            grabarYEnviarWav()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                grabarYEnviarWav()
            } else {
                Toast.makeText(requireContext(), "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun grabarYEnviarWav() {
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioEncoding,
            bufferSize
        )

        val audioData = ByteArray(bufferSize)
        val pcmStream = ByteArrayOutputStream()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("Permisos", "Permiso no otorgado justo antes de grabar")
            Toast.makeText(requireContext(), "Permiso no otorgado", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            audioRecord.startRecording()
        } catch (e: SecurityException) {
            Log.e("Permisos", "❌ No se puede grabar: ${e.message}")
            Toast.makeText(requireContext(), "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "🎤 Grabando 4 segundos...", Toast.LENGTH_SHORT).show()

        thread {
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < recordDurationMs) {
                val read = audioRecord.read(audioData, 0, audioData.size)
                if (read > 0) {
                    pcmStream.write(audioData, 0, read)
                }
            }
            audioRecord.stop()
            audioRecord.release()

            // Escribir archivo .wav
            audioFile = File(requireContext().cacheDir, "audio.wav")
            writeWavFile(audioFile, pcmStream.toByteArray())
            enviarAudioAHuggingFace(audioFile)
        }
    }

    private fun writeWavFile(file: File, pcmData: ByteArray) {
        val totalDataLen = pcmData.size + 36
        val totalAudioLen = pcmData.size
        val channels = 1
        val byteRate = 16 * sampleRate * channels / 8

        val header = ByteArrayOutputStream()
        val out = DataOutputStream(BufferedOutputStream(FileOutputStream(file)))

        header.write("RIFF".toByteArray())
        header.write(intToByteArray(totalDataLen))
        header.write("WAVE".toByteArray())
        header.write("fmt ".toByteArray())
        header.write(intToByteArray(16))
        header.write(shortToByteArray(1))
        header.write(shortToByteArray(channels.toShort()))
        header.write(intToByteArray(sampleRate))
        header.write(intToByteArray(byteRate))
        header.write(shortToByteArray((channels * 2).toShort()))
        header.write(shortToByteArray(16))
        header.write("data".toByteArray())
        header.write(intToByteArray(totalAudioLen))

        out.write(header.toByteArray())
        out.write(pcmData)
        out.close()
    }

    private fun intToByteArray(value: Int): ByteArray =
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

    private fun shortToByteArray(value: Short): ByteArray =
        ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()

    private fun enviarAudioAHuggingFace(file: File) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())

        val endpoints = mapOf(
            "ingles" to "https://McBikan-DeteccionEmociones.hf.space/predict",
            "espanol" to "https://McBikan-DeteccionEmociones.hf.space/predict_es"
        )

        val resultados = mutableMapOf<String, JSONObject>()
        var respuestasRecibidas = 0

        endpoints.forEach { (idioma, url) ->
            val request = Request.Builder()
                .url(url)
                .post(requestFile)
                .addHeader("Content-Type", "audio/wav")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HF_API", "❌ Error en $idioma: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseText = response.body?.string()
                    Log.i("HF_API", "✅ [$idioma] Respuesta: $responseText")

                    try {
                        val json = JSONObject(responseText ?: "")
                        val emociones = json.getJSONObject("emociones")

                        synchronized(resultados) {
                            resultados[idioma] = emociones
                            respuestasRecibidas++

                            if (respuestasRecibidas == endpoints.size) {
                                val resultadoFinal = JSONObject(resultados as Map<*, *>).toString()

                                requireActivity().runOnUiThread {
                                    val bundle = Bundle().apply {
                                        putString("resultado_emocion", resultadoFinal)
                                    }
                                    findNavController().navigate(
                                        R.id.action_emocionesFragment_to_resultadoFragment,
                                        bundle
                                    )
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("HF_API", "❌ Error procesando JSON de $idioma: ${e.message}")
                    }
                }
            })
        }
    }

    fun Double.format(digits: Int) = "%.${digits}f".format(this)


}
