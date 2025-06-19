package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R
import com.practica.proyectoihc.ui.base.BaseMenuFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class TestFragment : BaseMenuFragment() {
    private lateinit var tvPregunta: TextView
    private lateinit var rgOpciones: RadioGroup
    private lateinit var btnSiguiente: AppCompatButton
    private lateinit var tvProgreso: TextView
    private var preguntas = mutableListOf<Pregunta>()
    private var respuestas = mutableListOf<String>()
    private var preguntaActual = 0
    private var mostrandoPreguntas = false

    data class Pregunta(
        val texto: String,
        val opciones: List<String>
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        val btnEmpezar: AppCompatButton = view.findViewById(R.id.btnEmpezar)

        setupPreguntasUI(view)

        btnEmpezar.setOnClickListener {
            btnEmpezar.visibility = View.GONE
            view.findViewById<TextView>(R.id.tvSubtitulo).visibility = View.GONE
            view.findViewById<TextView>(R.id.tvInfo).visibility = View.GONE
            mostrarElementosPreguntas()
            generarPreguntas()
        }

        return view
    }

    private fun setupPreguntasUI(view: View) {
        val layout = view.findViewById<LinearLayout>(R.id.layout_principal) // Falta un layout

        tvProgreso = TextView(requireContext()).apply {
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
            visibility = View.GONE
        }

        tvPregunta = TextView(requireContext()).apply {
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(16, 16, 16, 16)
            visibility = View.GONE
        }

        rgOpciones = RadioGroup(requireContext()).apply {
            setPadding(16, 8, 16, 16)
            visibility = View.GONE
        }

        btnSiguiente = AppCompatButton(requireContext()).apply {
            text = "Siguiente"
            setBackgroundResource(R.drawable.boton_con_borde)
            setTextColor(resources.getColor(android.R.color.white))
            textSize = 18f
            setPadding(32, 16, 32, 16)
            visibility = View.GONE
            setOnClickListener { siguientePregunta() }
        }

        // Agregar las vistas al layout
        layout.apply {
            addView(tvProgreso)
            addView(tvPregunta)
            addView(rgOpciones)
            addView(btnSiguiente)
        }
    }

    // Alternativa si no conoces el ID exacto del LinearLayout contenedor:
    private fun setupPreguntasUIAlternativo(view: View) {
        // Encontrar el TextView del título y obtener su parent
        val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
        val layout = tvTitulo.parent as LinearLayout

        tvProgreso = TextView(requireContext()).apply {
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
            visibility = View.GONE
        }

        tvPregunta = TextView(requireContext()).apply {
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(16, 16, 16, 16)
            visibility = View.GONE
        }

        rgOpciones = RadioGroup(requireContext()).apply {
            setPadding(16, 8, 16, 16)
            visibility = View.GONE
        }

        btnSiguiente = AppCompatButton(requireContext()).apply {
            text = "Siguiente"
            setBackgroundResource(R.drawable.boton_con_borde)
            setTextColor(resources.getColor(android.R.color.white))
            textSize = 18f
            setPadding(32, 16, 32, 16)
            visibility = View.GONE
            setOnClickListener { siguientePregunta() }
        }

        layout.apply {
            addView(tvProgreso)
            addView(tvPregunta)
            addView(rgOpciones)
            addView(btnSiguiente)
        }
    }

    private fun mostrarElementosPreguntas() {
        tvProgreso.visibility = View.VISIBLE
        tvPregunta.visibility = View.VISIBLE
        rgOpciones.visibility = View.VISIBLE
        btnSiguiente.visibility = View.VISIBLE
        mostrandoPreguntas = true
    }

    private fun generarPreguntas() {
        lifecycleScope.launch {
            try {
                val preguntasGeneradas = withContext(Dispatchers.IO) {
                    llamarGeminiAPI()
                }
                if (preguntasGeneradas.isNotEmpty()) {
                    preguntas = preguntasGeneradas.toMutableList()
                    mostrarPregunta()
                } else {
                    Toast.makeText(context, "Error al generar preguntas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun llamarGeminiAPI(): List<Pregunta> {
        val apiKey = "TU_API_KEY_AQUI"
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")

        val prompt = """
        Genera exactamente 15 preguntas de evaluación psicológica para determinar rasgos de personalidad.
        Cada pregunta debe tener 4 opciones de respuesta (A, B, C, D).
        
        Responde en formato JSON con esta estructura:
        {
          "preguntas": [
            {
              "texto": "pregunta aquí",
              "opciones": ["opción A", "opción B", "opción C", "opción D"]
            }
          ]
        }
        
        Las preguntas deben evaluar diferentes aspectos como extroversión, neuroticismo, apertura, amabilidad y responsabilidad.
        """

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        return withContext(Dispatchers.IO) {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            val response = connection.inputStream.bufferedReader().readText()
            parsearRespuestaGemini(response)
        }
    }

    private fun parsearRespuestaGemini(response: String): List<Pregunta> {
        try {
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val jsonContent = JSONObject(content.trim())
            val preguntasArray = jsonContent.getJSONArray("preguntas")

            val preguntasList = mutableListOf<Pregunta>()
            for (i in 0 until preguntasArray.length()) {
                val preguntaObj = preguntasArray.getJSONObject(i)
                val texto = preguntaObj.getString("texto")
                val opcionesArray = preguntaObj.getJSONArray("opciones")

                val opciones = mutableListOf<String>()
                for (j in 0 until opcionesArray.length()) {
                    opciones.add(opcionesArray.getString(j))
                }

                preguntasList.add(Pregunta(texto, opciones))
            }

            return preguntasList
        } catch (e: Exception) {
            return emptyList()
        }
    }

    private fun mostrarPregunta() {
        if (preguntaActual < preguntas.size) {
            val pregunta = preguntas[preguntaActual]

            tvProgreso.text = "Pregunta ${preguntaActual + 1} de ${preguntas.size}"
            tvPregunta.text = pregunta.texto

            rgOpciones.removeAllViews()
            pregunta.opciones.forEachIndexed { index, opcion ->
                val radioButton = RadioButton(requireContext()).apply {
                    text = opcion
                    id = index
                    textSize = 16f
                    setPadding(8, 8, 8, 8)
                }
                rgOpciones.addView(radioButton)
            }

            btnSiguiente.text = if (preguntaActual == preguntas.size - 1) "Finalizar" else "Siguiente"
        }
    }

    private fun siguientePregunta() {
        val selectedId = rgOpciones.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(context, "Por favor selecciona una respuesta", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton = rgOpciones.findViewById<RadioButton>(selectedId)
        respuestas.add(selectedRadioButton.text.toString())

        preguntaActual++
        if (preguntaActual < preguntas.size) {
            mostrarPregunta()
        } else {
            analizarRespuestas()
        }
    }

    private fun analizarRespuestas() {
        lifecycleScope.launch {
            try {
                val analisis = withContext(Dispatchers.IO) {
                    analizarConGemini()
                }
                val bundle = Bundle().apply {
                    putString("resultado", analisis)
                    putStringArrayList("respuestas", ArrayList(respuestas))
                }
                findNavController().navigate(R.id.action_testFragment_to_psicoFragment, bundle)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al analizar respuestas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun analizarConGemini(): String {
        val apiKey = "TU_API_KEY_AQUI"
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")

        val respuestasTexto = respuestas.joinToString(separator = "\n") { "- $it" }
        val prompt = """
        Analiza las siguientes respuestas de un test de personalidad y proporciona un análisis detallado:
        
        Respuestas:
        $respuestasTexto
        
        Proporciona un análisis que incluya:
        1. Rasgos de personalidad principales
        2. Fortalezas identificadas
        3. Áreas de mejora
        4. Recomendaciones personalizadas
        
        El análisis debe ser comprensivo, positivo y constructivo, en aproximadamente 200-300 palabras.
        """

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        return withContext(Dispatchers.IO) {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            val response = connection.inputStream.bufferedReader().readText()
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ivMenu = view.findViewById<View>(R.id.ivMenu)
        if (ivMenu != null) {
            setupMenuNavigation(ivMenu)
        }
    }
}