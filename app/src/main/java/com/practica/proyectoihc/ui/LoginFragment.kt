package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.practica.proyectoihc.databinding.FragmentLoginBinding
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import com.practica.proyectoihc.R


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        val btnRegistro: LinearLayout = view.findViewById(R.id.registerSection)

        btnRegistro.setOnClickListener {
            // Aquí navega al TestFragment
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email == "admin" && password == "admin") {
                findNavController().navigate(R.id.action_loginFragment_to_perfilFragment)
            } else {
                Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
