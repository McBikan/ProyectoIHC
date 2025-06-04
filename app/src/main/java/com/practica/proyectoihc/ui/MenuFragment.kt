package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        // Referencia al botón de Evaluación Psicológica
        val btnEvaluacion: LinearLayout = view.findViewById(R.id.opcionEvaluacion)

        btnEvaluacion.setOnClickListener {
            // Aquí navega al TestFragment
            findNavController().navigate(R.id.action_menuFragment_to_testFragment)
        }

        return view
    }
}
