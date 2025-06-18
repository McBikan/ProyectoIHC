package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.practica.proyectoihc.R
import com.practica.proyectoihc.databinding.FragmentPerfilBinding
import com.practica.proyectoihc.ui.base.BaseMenuFragment

class PerfilFragment : BaseMenuFragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore

        setupMenuNavigation(binding.ivMenu)

        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.action_perfilFragment_to_loginFragment)
            return
        }

        val userId = currentUser.uid

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl")

                    binding.tvTituloNombre.text = firstName.uppercase()
                    binding.tvNombreValor.text = firstName
                    binding.tvApellidoValor.text = lastName

                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.default_user)
                            .error(R.drawable.default_user)
                            .circleCrop()
                            .into(binding.ivProfileImage)
                    }
                } else {
                    showToast("Datos de usuario no encontrados")
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al cargar datos: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
