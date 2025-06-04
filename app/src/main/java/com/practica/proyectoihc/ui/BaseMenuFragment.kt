package com.practica.proyectoihc.ui.base

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R

abstract class BaseMenuFragment : Fragment() {
    fun setupMenuNavigation(menuView: View) {
        menuView.setOnClickListener {
            findNavController().navigate(R.id.action_global_menuFragment)
        }
    }
}