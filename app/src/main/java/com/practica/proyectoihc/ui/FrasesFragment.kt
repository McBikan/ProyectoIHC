package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R
import com.practica.proyectoihc.ui.base.BaseMenuFragment

class FrasesFragment : BaseMenuFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_frases, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menú hamburguesa
        val ivMenu = view.findViewById<View>(R.id.ivMenu)
        setupMenuNavigation(ivMenu)

        // Elementos de layout
        val otrosContainer = view.findViewById<LinearLayout>(R.id.otrosContainer)
        val etOtro = view.findViewById<EditText>(R.id.etOtroProblema)
        val btnEnviarOtro = view.findViewById<ImageButton>(R.id.btnEnviarOtro)
        val btnOtros = view.findViewById<Button>(R.id.btnOtros)
        val btnMicro = view.findViewById<ImageButton>(R.id.btnMicrofono)

        // Mostrar el cuadro de texto cuando se presiona "Otros"
        btnOtros.setOnClickListener {
            otrosContainer.visibility = View.VISIBLE
            etOtro.requestFocus()
        }

        // Botones predefinidos con sus textos
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

        // Enviar el texto personalizado con el botón de avioncito (btnEnviarOtro)
        btnEnviarOtro.setOnClickListener {
            val texto = etOtro.text.toString().trim()
            if (texto.isNotEmpty()) {
                navegarConContexto(texto)
            } else {
                Toast.makeText(requireContext(), "Por favor escribe tu contexto", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de micrófono (reservado para funcionalidad futura)
        btnMicro.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidad de audio próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navegarConContexto(contexto: String) {
        val action = FrasesFragmentDirections.actionFrasesFragmentToFraseResultadoFragment(contexto)
        findNavController().navigate(action)
    }
}
