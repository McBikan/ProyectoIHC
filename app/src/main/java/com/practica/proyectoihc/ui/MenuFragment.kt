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

        // Referencias a los elementos del menú
        val btnReturn: ImageView = view.findViewById(R.id.btnReturn)
        val btnInicio: LinearLayout = view.findViewById(R.id.opcionInicio)
        val btnEvaluacion: LinearLayout = view.findViewById(R.id.opcionEvaluacion)
        val btnHistorial: LinearLayout = view.findViewById(R.id.opcionHistorial)
        val btnInteraccion: LinearLayout = view.findViewById(R.id.opcionInteraccion)
        val btnFrases: LinearLayout = view.findViewById(R.id.opcionFrases)
        val btnEmociones: LinearLayout = view.findViewById(R.id.opcionEmocion)
        val btnSalir: AppCompatButton = view.findViewById(R.id.btnSalir)

        // Configurar listeners para cada opción del menú

        // Botón de retorno (esquina superior derecha)
        btnReturn.setOnClickListener {
            // Puedes navegar al perfil o hacer un popBackStack según tu lógica
            findNavController().navigate(R.id.action_menuFragment_to_perfilFragment)
        }

        // Navegar a PerfilFragment (Inicio)
        btnInicio.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_perfilFragment)
        }

        // Navegar a TestFragment (Evaluación Psicológica)
        btnEvaluacion.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_testFragment)
        }

        // Navegar a HistorialFragment
        btnHistorial.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_HIstorialFragment)
        }

        // Navegar a VozFragment (Interacción por voz)
        btnInteraccion.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_vozFragment)
        }

        // Navegar a FrasesFragment (Frases Motivadoras)
        btnFrases.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_frasesFragment)
        }

        // Navegar a EmocionesFragment (Detección de emociones)
        btnEmociones.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_emocionesFragment)
        }



        // Botón Salir - Navegar de vuelta al LoginFragment
        btnSalir.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_loginFragment)
        }

        return view
    }
}