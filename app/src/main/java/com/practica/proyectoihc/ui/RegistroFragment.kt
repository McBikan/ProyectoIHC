package com.practica.proyectoihc.ui

import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.practica.proyectoihc.R
import com.practica.proyectoihc.databinding.FragmentRegistroBinding

class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    private var progressDialog: AlertDialog? = null

    private val pickImageLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            loadImageIntoView(it)
        }
    }

    private val takePictureLauncher: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            selectedImageUri?.let { loadImageIntoView(it) }
        } else {
            showToast("No se tomó ninguna foto")
        }
    }

    private val permissionLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, Boolean> ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[android.Manifest.permission.READ_MEDIA_IMAGES] ?: false
        } else {
            permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        }

        if (cameraGranted && storageGranted) {
            showImagePickerDialog()
        } else {
            showToast("Permisos necesarios para seleccionar imagen")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFirebase()
        setupListeners()
    }

    private fun initializeFirebase() {
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener { validateAndRegister() }
        binding.btnPhoto.setOnClickListener { handleImageSelection() }
        binding.ivPhoto.setOnClickListener { handleImageSelection() }
        binding.btnReturn.setOnClickListener { navigateToLoginFragment() }
    }

    private fun validateAndRegister() {
        val firstName = binding.etName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!validateInputs(firstName, lastName, email, password)) return

        showProgressDialog()
        createUserAccount(email, password, firstName, lastName)
    }

    private fun validateInputs(firstName: String, lastName: String, email: String, password: String): Boolean {
        when {
            firstName.isEmpty() -> {
                setFieldError(binding.etName, "Ingresa tu nombre")
                return false
            }
            lastName.isEmpty() -> {
                setFieldError(binding.etLastName, "Ingresa tu apellido")
                return false
            }
            email.isEmpty() -> {
                setFieldError(binding.etEmail, "Ingresa tu correo electrónico")
                return false
            }
            !email.isValidEmail() -> {
                setFieldError(binding.etEmail, "Formato de correo inválido")
                return false
            }
            else -> {
                val passwordError = password.getPasswordValidationError()
                if (passwordError != null) {
                    setFieldError(binding.etPassword, passwordError)
                    return false
                }
            }
        }
        return true
    }

    private fun setFieldError(field: EditText, message: String) {
        field.error = message
        field.requestFocus()
    }

    private fun createUserAccount(email: String, password: String, firstName: String, lastName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { user ->
                    processImageUpload(user.uid, firstName, lastName)
                } ?: run {
                    hideProgressDialog()
                    handleRegistrationError("Error al crear usuario")
                }
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()
                val message = when {
                    exception.message?.contains("email-already-in-use") == true -> "El correo ya está registrado"
                    exception.message?.contains("weak-password") == true -> "La contraseña es muy débil"
                    exception.message?.contains("invalid-email") == true -> "Formato de correo inválido"
                    exception.message?.contains("network-request-failed") == true -> "Error de conexión"
                    else -> exception.message ?: "Error desconocido"
                }
                handleRegistrationError(message)
            }
    }

    private fun processImageUpload(userId: String, firstName: String, lastName: String) {
        if (selectedImageUri != null) {
            uploadProfileImage(userId) { imageUrl ->
                saveUserData(userId, firstName, lastName, imageUrl)
            }
        } else {
            saveUserData(userId, firstName, lastName, null)
        }
    }

    private fun uploadProfileImage(userId: String, onComplete: (String?) -> Unit) {
        selectedImageUri?.let { uri ->
            val imageRef = storage.reference.child("profile_images/$userId.jpg")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        onComplete(downloadUri.toString())
                    }.addOnFailureListener {
                        onComplete(null)
                    }
                }
                .addOnFailureListener {
                    showToast("Error al subir imagen")
                    onComplete(null)
                }
        } ?: onComplete(null)
    }

    private fun saveUserData(userId: String, firstName: String, lastName: String, imageUrl: String?) {
        val userData = createUserDataMap(firstName, lastName, imageUrl)

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                hideProgressDialog()
                showToast("Registro exitoso")
                navigateToLoginFragment()
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()
                showToast("Error al guardar datos: ${exception.message}")
            }
    }

    private fun createUserDataMap(firstName: String, lastName: String, imageUrl: String?) = hashMapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "profileImageUrl" to (imageUrl ?: ""),
        "createdAt" to FieldValue.serverTimestamp()
    )

    private fun handleRegistrationError(message: String) {
        hideProgressDialog()
        showToast("Error en registro: $message")
    }

    private fun navigateToLoginFragment() {
        findNavController().popBackStack()
    }

    private fun handleImageSelection() {
        when {
            hasRequiredPermissions() -> showImagePickerDialog()
            shouldShowPermissionRationale() -> showPermissionRationale()
            else -> requestPermissions()
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        return cameraPermission && storagePermission
    }

    private fun shouldShowPermissionRationale(): Boolean {
        val cameraRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)
        val storageRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return cameraRationale || storageRationale
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permisos necesarios")
            .setMessage("Para seleccionar una foto, necesitamos acceso a tu galería y cámara.")
            .setPositiveButton("Aceptar") { _, _ -> requestPermissions() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Galería", "Cámara", "Cancelar")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar imagen")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> takePictureFromCamera()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun takePictureFromCamera() {
        try {
            val photoUri = createImageUri()
            selectedImageUri = photoUri
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            showToast("Error al acceder a la cámara")
        }
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "profile_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("No se pudo crear URI para la imagen")
    }

    private fun loadImageIntoView(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_user_icon)
            .error(R.drawable.ic_user_icon)
            .circleCrop()
            .into(binding.ivPhoto)
    }

    private fun showProgressDialog() {
        progressDialog = AlertDialog.Builder(requireContext())
            .setTitle("Procesando")
            .setMessage("Registrando usuario...")
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

    private fun String.getPasswordValidationError(): String? {
        return when {
            length < 6 -> "Mínimo 6 caracteres"
            isEmpty() -> "Contraseña requerida"
            else -> null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgressDialog()
        _binding = null
    }
}