package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        val btnReturn: ImageView = view.findViewById(R.id.btnReturn)
        val btnInicio: LinearLayout = view.findViewById(R.id.opcionInicio)
        val btnEvaluacion: LinearLayout = view.findViewById(R.id.opcionEvaluacion)
        val btnHistorial: LinearLayout = view.findViewById(R.id.opcionHistorial)
        val btnInteraccion: LinearLayout = view.findViewById(R.id.opcionInteraccion)
        val btnFrases: LinearLayout = view.findViewById(R.id.opcionFrases)
        val btnEmociones: LinearLayout = view.findViewById(R.id.opcionEmocion)
        val btnSalir: AppCompatButton = view.findViewById(R.id.btnSalir)

        btnReturn.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_perfilFragment)
        }

        btnInicio.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_perfilFragment)
        }

        btnEvaluacion.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_testFragment)
        }

        btnHistorial.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_HIstorialFragment)
        }

        btnInteraccion.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_vozFragment)
        }

        btnFrases.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_frasesFragment)
        }

        btnEmociones.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_emocionesFragment)
        }

        btnSalir.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_loginFragment)
        }

        return view
    }
}