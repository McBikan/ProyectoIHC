package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R

class FrasesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_frases, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivMenu= view.findViewById<ImageView>(R.id.ivMenu)
        setupMenuNavigation(ivMenu)

        val otrosContainer = view.findViewById<LinearLayout>(R.id.otrosContainer)
        val etOtro = view.findViewById<EditText>(R.id.etOtroProblema)
        val btnEnviarOtro = view.findViewById<ImageButton>(R.id.btnEnviarOtro)
        val btnOtros = view.findViewById<Button>(R.id.btnOtros)
        val btnMicro = view.findViewById<ImageButton>(R.id.btnMicrofono)

        btnOtros.setOnClickListener {
            otrosContainer.visibility = View.VISIBLE
            etOtro.requestFocus()
        }

        val contextos = mapOf(
            R.id.btnFamilia to "Problemas Familiares",
            R.id.btnPareja to "Problemas de Pareja",
            R.id.btnAcademico to "Problemas Académicos",
            R.id.btnAlimenticio to "Problemas Alimenticios"
        )

        contextos.forEach { (id, texto) ->
            view.findViewById<Button>(id).setOnClickListener {
                navegarConContexto(texto)
            }
        }

        btnEnviarOtro.setOnClickListener {
            val texto = etOtro.text.toString().trim()
            if (texto.isNotEmpty()) {
                navegarConContexto(texto)
            } else {
                Toast.makeText(requireContext(), "Por favor escribe tu contexto", Toast.LENGTH_SHORT).show()
            }
        }

        btnMicro.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidad de audio próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMenuNavigation(menuIcon: ImageView){
        menuIcon.setOnClickListener {
            navigateToMenu()
        }
    }

    private fun navigateToMenu(){
        findNavController().navigate(R.id.action_frasesFragment_to_menuFragment)
    }

    private fun navegarConContexto(contexto: String) {
        val action = FrasesFragmentDirections.actionFrasesFragmentToFraseResultadoFragment(contexto)
        findNavController().navigate(action)
    }
}
