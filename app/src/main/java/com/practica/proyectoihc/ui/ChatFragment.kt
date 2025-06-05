package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.practica.proyectoihc.R

class ChatFragment : Fragment() {

    private lateinit var logTextView: TextView
    private lateinit var scrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        logTextView = view.findViewById(R.id.tvLogs)
        scrollView = view.findViewById(R.id.scrollLogs)
        return view
    }

    fun agregarLog(mensaje: String) {
        logTextView.append("$mensaje\n")
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    companion object {
        private var instancia: ChatFragment? = null

        fun obtenerInstancia(): ChatFragment? = instancia

        fun agregarLogGlobal(mensaje: String) {
            instancia?.agregarLog(mensaje)
        }
    }

    override fun onResume() {
        super.onResume()
        instancia = this
    }

    override fun onPause() {
        super.onPause()
        if (instancia == this) {
            instancia = null
        }
    }
}
