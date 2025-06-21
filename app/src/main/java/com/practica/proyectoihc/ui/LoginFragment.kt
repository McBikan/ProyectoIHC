package com.practica.proyectoihc.ui

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.practica.proyectoihc.databinding.FragmentLoginBinding
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.practica.proyectoihc.R


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var progressDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFirebase()
        setupListeners()
        checkUserSession()
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(requireContext())
        } catch (e: IllegalStateException) {
            // Ya está inicializado, no hacer nada
        }
        auth = Firebase.auth
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener { validateAndLogin() }
        binding.registerSection.setOnClickListener { navigateToRegister() }
        binding.tvForgotPassword.setOnClickListener { handleForgotPassword() }
    }

    private fun checkUserSession() {
        if (auth.currentUser != null) {
            navigateToProfile()
        }
    }

    private fun validateAndLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (validateInputs(email, password)) {
            authenticateUser(email, password)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                setFieldError(binding.etEmail, "Ingresa tu correo electrónico")
                return false
            }
            !email.isValidEmail() -> {
                setFieldError(binding.etEmail, "Formato de correo inválido")
                return false
            }
            password.isEmpty() -> {
                setFieldError(binding.etPassword, "Ingresa tu contraseña")
                return false
            }
            password.length < 6 -> {
                setFieldError(binding.etPassword, "Contraseña muy corta")
                return false
            }
        }
        return true
    }

    private fun setFieldError(field: EditText, message: String) {
        field.error = message
        field.requestFocus()
    }

    private fun authenticateUser(email: String, password: String) {
        showProgressDialog()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                hideProgressDialog()
                showToast("Inicio de sesión exitoso")
                navigateToProfile()
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()
                handleLoginError(exception.message)
            }
    }


    private fun handleLoginError(errorMessage: String?) {
        val message = when {
            errorMessage?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Credenciales inválidas"
            errorMessage?.contains("invalid-credential") == true -> "Credenciales inválidas"
            errorMessage?.contains("user-not-found") == true -> "Usuario no encontrado"
            errorMessage?.contains("wrong-password") == true -> "Contraseña incorrecta"
            errorMessage?.contains("invalid-email") == true -> "Correo electrónico inválido"
            errorMessage?.contains("user-disabled") == true -> "Cuenta deshabilitada"
            errorMessage?.contains("too-many-requests") == true -> "Demasiados intentos, intenta más tarde"
            errorMessage?.contains("network-request-failed") == true -> "Error de conexión"
            else -> "Error en el inicio de sesión"
        }
        showToast(message)
    }

    private fun handleForgotPassword() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            showToast("Ingresa tu correo electrónico primero")
            binding.etEmail.requestFocus()
            return
        }

        if (!email.isValidEmail()) {
            showToast("Ingresa un correo válido")
            binding.etEmail.requestFocus()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Recuperar contraseña")
            .setMessage("¿Enviar enlace de recuperación a $email?")
            .setPositiveButton("Enviar") { _, _ ->
                sendPasswordResetEmail(email)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun sendPasswordResetEmail(email: String) {
        showProgressDialog()

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                hideProgressDialog()
                showToast("Enlace enviado a tu correo")
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()
                val message = when {
                    exception.message?.contains("user-not-found") == true -> "Correo no registrado"
                    else -> "Error al enviar enlace"
                }
                showToast(message)
            }
    }

    private fun navigateToProfile() {
        findNavController().navigate(R.id.action_loginFragment_to_perfilFragment)
    }

    private fun navigateToRegister() {
        findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
    }

    private fun showProgressDialog() {
        progressDialog = AlertDialog.Builder(requireContext())
            .setTitle("Iniciando sesión")
            .setMessage("Verificando credenciales...")
            .setCancelable(false)
            .create()
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgressDialog()
        _binding = null
    }
}