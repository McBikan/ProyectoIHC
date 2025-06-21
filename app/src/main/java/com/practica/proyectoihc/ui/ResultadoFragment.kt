package com.practica.proyectoihc.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.practica.proyectoihc.R
import org.json.JSONObject

class ResultadoFragment : Fragment() {

    companion object {
        private const val ARG_RESULTADO = "resultado_emocion"

        fun newInstance(resultado: String): ResultadoFragment {
            val fragment = ResultadoFragment()
            val args = Bundle()
            args.putString(ARG_RESULTADO, resultado)
            fragment.arguments = args
            return fragment
        }

        fun Double.format(digits: Int) = "%.${digits}f".format(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_resultado, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resultadoJson = arguments?.getString(ARG_RESULTADO) ?: "{}"
        Log.d("ResultadoFragment", "Resultado recibido: $resultadoJson")

        val iconos = mapOf(
            "neutral" to "😐",
            "calm" to "😌",
            "happy" to "😄",
            "sad" to "😢",
            "angry" to "😠",
            "fearful" to "😨",
            "disgust" to "🤢",
            "surprised" to "😲",
            "happiness" to "😄",
            "sadness" to "😢",
            "anger" to "😠",
            "fear" to "😨",
            "bored" to "🥱",
            "excited" to "🤩"
        )

        val resultadoBuilder = StringBuilder("Resultados por modelo:\n\n")

        try {
            val json = JSONObject(resultadoJson)
            val modelos = json.keys()

            for (modelo in modelos) {
                val emociones = json.getJSONObject(modelo)
                resultadoBuilder.append("🔷 Modelo: $modelo\n")
                val keys = emociones.keys()
                for (key in keys) {
                    val valor = emociones.getDouble(key)
                    val emoji = iconos[key] ?: ""
                    resultadoBuilder.append("   $emoji $key: ${(valor * 100).format(2)}%\n")
                }
                resultadoBuilder.append("\n")
            }

        } catch (e: Exception) {
            resultadoBuilder.clear()
            resultadoBuilder.append("Error procesando resultados.")
        }

        val tvResultado = view.findViewById<TextView>(R.id.tvResultadoFinal)
        tvResultado.text = resultadoBuilder.toString()
    }

}
