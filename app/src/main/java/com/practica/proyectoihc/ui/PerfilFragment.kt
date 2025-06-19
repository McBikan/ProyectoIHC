package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
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
        setupListeners()
        loadUserData()
    }

    private fun setupListeners() {
        binding.ivProfileImage.setOnClickListener {
            showProfileOptions()
        }

        binding.layoutAntecedentes.setOnClickListener {
            showAntecedenteOptions()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
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
                    binding.tvNombreValor.text = firstName.uppercase()
                    binding.tvApellidoValor.text = lastName.uppercase()

                    loadAntecedentes(userId)

                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.default_user)
                            .error(R.drawable.default_user)
                            .into(binding.ivProfileImage)
                    }
                } else {
                    showToast("Datos de usuario no encontrados")
                    navigateToLogin()
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al cargar datos: ${e.message}")
                navigateToLogin()
            }
    }

    private fun loadAntecedentes(userId: String) {
        db.collection("users").document(userId).collection("antecedentes")
            .get()
            .addOnSuccessListener { documents ->
                binding.tvAntecedente1.visibility = View.GONE
                binding.tvAntecedente2.visibility = View.GONE
                binding.tvAntecedente3.visibility = View.GONE

                val antecedentes = documents.mapNotNull { it.getString("descripcion") }
                    .take(3)

                antecedentes.forEachIndexed { index, antecedente ->
                    when (index) {
                        0 -> {
                            binding.tvAntecedente1.text = "• ${antecedente.uppercase()}"
                            binding.tvAntecedente1.visibility = View.VISIBLE
                        }
                        1 -> {
                            binding.tvAntecedente2.text = "• ${antecedente.uppercase()}"
                            binding.tvAntecedente2.visibility = View.VISIBLE
                        }
                        2 -> {
                            binding.tvAntecedente3.text = "• ${antecedente.uppercase()}"
                            binding.tvAntecedente3.visibility = View.VISIBLE
                        }
                    }
                }

                if (antecedentes.isEmpty()) {
                    binding.tvAntecedente1.text = "• SIN ANTECEDENTES REGISTRADOS"
                    binding.tvAntecedente1.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                binding.tvAntecedente1.text = "• ERROR AL CARGAR ANTECEDENTES"
                binding.tvAntecedente1.visibility = View.VISIBLE
            }
    }

    private fun showProfileOptions() {
        val options = arrayOf("Editar Foto", "Ver Perfil Completo", "Cerrar Sesión", "Cancelar")
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones de Perfil")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> editProfileImage()
                    1 -> showFullProfile()
                    2 -> logoutUser()
                    3 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun showAntecedenteOptions() {
        val options = arrayOf("Agregar Antecedente", "Editar Antecedentes", "Cancelar")
        AlertDialog.Builder(requireContext())
            .setTitle("Antecedentes Médicos")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> addAntecedente()
                    1 -> editAntecedentes()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun editProfileImage() {
        showToast("Función de editar foto próximamente")
    }

    private fun showFullProfile() {
        showToast("Función de perfil completo próximamente")
    }

    private fun addAntecedente() {
        val input = EditText(requireContext())
        input.hint = "Ingrese antecedente médico"

        AlertDialog.Builder(requireContext())
            .setTitle("Agregar Antecedente")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val antecedente = input.text.toString().trim()
                if (antecedente.isNotEmpty()) {
                    saveAntecedente(antecedente)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editAntecedentes() {
        showToast("Función de editar antecedentes próximamente")
    }

    private fun saveAntecedente(antecedente: String) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        val antecedenteData = hashMapOf(
            "descripcion" to antecedente,
            "fechaCreacion" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(userId).collection("antecedentes")
            .add(antecedenteData)
            .addOnSuccessListener {
                showToast("Antecedente agregado")
                loadAntecedentes(userId)
            }
            .addOnFailureListener {
                showToast("Error al agregar antecedente")
            }
    }

    private fun logoutUser() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                auth.signOut()
                navigateToLogin()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_global_menuFragment)
        findNavController().navigate(R.id.action_menuFragment_to_loginFragment)
    }

    private fun navigateToMenu() {
        findNavController().navigate(R.id.action_perfilFragment_to_menuFragment)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
