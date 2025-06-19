package com.practica.proyectoihc.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R

class PsicoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(resources.getColor(R.color.fondo_claro))
            setPadding(24, 24, 24, 24)
        }

        val mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val titulo = TextView(requireContext()).apply {
            text = "RESULTADOS DE TU EVALUACIÓN"
            textSize = 24f
            setTextColor(resources.getColor(R.color.morado_fuerte))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val resultado = arguments?.getString("resultado") ?: "No se pudo obtener el resultado del análisis."

        val tvResultado = TextView(requireContext()).apply {
            text = resultado
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(16, 16, 16, 32)
            setLineSpacing(8f, 1.0f)
        }

        val btnVolver = AppCompatButton(requireContext()).apply {
            text = "Volver al Test"
            setBackgroundResource(R.drawable.boton_con_borde)
            setTextColor(resources.getColor(android.R.color.white))
            textSize = 18f
            setPadding(32, 16, 32, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                topMargin = 16
            }
            setOnClickListener {
                findNavController().navigate(R.id.action_psicoFragment_to_testFragment)
            }
        }

        mainLayout.addView(titulo)
        mainLayout.addView(tvResultado)
        mainLayout.addView(btnVolver)

        view.addView(mainLayout)

        return view
    }
}