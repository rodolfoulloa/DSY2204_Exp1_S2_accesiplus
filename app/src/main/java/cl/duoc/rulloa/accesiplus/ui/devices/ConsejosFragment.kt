package cl.duoc.rulloa.accesiplus.ui.devices

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import cl.duoc.rulloa.accesiplus.R

/**
 * Fragment con vistas clásicas (ViewGroup LinearLayout + TextView + Button) que se integra
 * dentro de BuscarDispositivoScreen con AndroidFragment (fragment-compose).
 * Muestra consejos para no perder el dispositivo; rota entre ellos con el botón.
 */
class ConsejosFragment : Fragment(R.layout.fragment_consejos) {

    private var indice = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        indice = savedInstanceState?.getInt(CLAVE_INDICE) ?: 0
        val tipo = requireArguments().getString(ARG_TIPO) ?: "Otro"
        val consejos = consejosPara(tipo)
        val texto = view.findViewById<TextView>(R.id.consejo_texto)
        val contador = view.findViewById<TextView>(R.id.consejo_contador)

        fun mostrar() {
            texto.text = consejos[indice]
            contador.text = "Consejo ${indice + 1} de ${consejos.size}"
        }
        mostrar()
        view.findViewById<Button>(R.id.consejo_siguiente).setOnClickListener {
            indice = (indice + 1) % consejos.size
            mostrar()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CLAVE_INDICE, indice)
    }

    companion object {
        const val ARG_TIPO = "tipo"
        private const val CLAVE_INDICE = "indice"

        /** bundleOf (core-ktx) arma los argumentos sin crear el Bundle a mano. */
        fun argumentos(tipo: String): Bundle = bundleOf(ARG_TIPO to tipo)

        fun consejosPara(tipo: String): List<String> = when (tipo) {
            "Audífono", "Implante coclear" -> listOf(
                "Guárdalo siempre en su estuche al sacártelo, nunca en un bolsillo o servilleta.",
                "Registra la ubicación cada vez que lo dejes fuera de casa.",
                "Revisa la batería cada mañana; una batería baja hace que se apague y se pierda de vista.",
                "Usa un cordón de seguridad cuando hagas deporte."
            )
            "Teléfono", "Tablet" -> listOf(
                "Activa 'Encontrar mi dispositivo' en la configuración de Google.",
                "Registra la ubicación al dejarlo cargando en un lugar nuevo.",
                "Activa la vibración y el flash para notificaciones: no dependas del sonido."
            )
            else -> listOf(
                "Asigna un lugar fijo en casa para cada dispositivo.",
                "Registra la ubicación cada vez que lo dejes en un lugar distinto."
            )
        }
    }
}
