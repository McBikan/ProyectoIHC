package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// import android.widget.LinearLayout // Ya no necesitas importar LinearLayout si btnEmpezar no es uno
import androidx.appcompat.widget.AppCompatButton // <--- ¡IMPORTA ESTO!
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R
import com.practica.proyectoihc.ui.base.BaseMenuFragment

class TestFragment :  BaseMenuFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        // Cambia el tipo de LinearLayout a AppCompatButton
        val btnEmpezar: AppCompatButton = view.findViewById(R.id.btnEmpezar) // <-- CORREGIDO

        btnEmpezar.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_testFragment_to_psicoFragment)
            } catch (e: Exception) {
                println("Error de navegación: ${e.message}")
                e.printStackTrace()
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivMenu = view.findViewById<View>(R.id.ivMenu)

        if (ivMenu != null) {
            setupMenuNavigation(ivMenu)
        } else {
            android.util.Log.e("TestFragment", "Error: ivMenu no encontrado en el layout fragment_test.xml")
        }
    }
}