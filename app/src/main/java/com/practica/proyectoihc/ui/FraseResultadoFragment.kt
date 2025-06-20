package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.practica.proyectoihc.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class FraseResultadoFragment : Fragment() {

    private val args: FraseResultadoFragmentArgs by navArgs()
    private lateinit var tvResultado: TextView

    private val client = OkHttpClient()
    private val GROQ_API_KEY = "gsk"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_frase_resultado, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvResultado = view.findViewById(R.id.tvResultado)

        val contexto = args.fraseContexto
        view.postDelayed({
            generarFraseMotivacional(contexto)
        }, 1500) // 1.5 segundos
    }

    private fun generarFraseMotivacional(contexto: String) {
        val prompt = """
            Eres un acompañante emocional compasivo. 
            Dame una frase breve, cálida y poderosa para reconfortar emocionalmente a alguien que está pasando por lo siguiente: $contexto.
        
            La frase debe:
            - Ser original y no parecer cliché.
            - Evocar calma, esperanza o alivio emocional.
            - Estar escrita en español.
            - Ser apropiada para ser leída en voz alta por una app de ayuda emocional.
        """.trimIndent()


        val json = JSONObject().apply {
            put("model", "llama3-8b-8192")
            val messagesArray = org.json.JSONArray().put(
                JSONObject().put("role", "user").put("content", prompt)
            )
            put("messages", messagesArray)
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $GROQ_API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    tvResultado.text = "Error al generar frase: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val result = JSONObject(body)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    activity?.runOnUiThread {
                        tvResultado.text = result.trim()
                    }
                } else {
                    activity?.runOnUiThread {
                        tvResultado.text = "Error al obtener respuesta de Groq"
                    }
                }
            }
        })
    }
}
