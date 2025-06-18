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
        val btnInicio: LinearLayout = view.findViewById(R.id.opcionInicio)
        val btnInteraccion: LinearLayout = view.findViewById(R.id.opcionInteraccion)
        val btnFrases: LinearLayout = view.findViewById(R.id.opcionFrases)
        val btnHistorial: LinearLayout = view.findViewById(R.id.opcionHistorial)
        val btnEmociones : LinearLayout = view.findViewById(R.id.opcionEmocion)

        btnEvaluacion.setOnClickListener {
            // Aquí navega al TestFragment
            findNavController().navigate(R.id.action_menuFragment_to_testFragment)
        }
        // Navegar a PerfilFragment
        btnInicio.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_perfilFragment)
        }
        // Navegar a VozFragment
        btnInteraccion.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_vozFragment)
        }
        // Navegar a PerfilFragment
        btnFrases.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_frasesFragment)
        }
        // Navegar a HistorialFragment
        btnHistorial.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_HIstorialFragment)
        }
        // Navegar a EmocionesFragment
        btnEmociones.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_emocionesFragment)
        }

        return view
    }
}
