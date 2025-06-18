package com.practica.proyectoihc.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.practica.proyectoihc.R
import com.practica.proyectoihc.ui.base.BaseMenuFragment

class FrasesFragment :  BaseMenuFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_frases, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivMenu = view.findViewById<View>(R.id.ivMenu)
        setupMenuNavigation(ivMenu)
    }
}
