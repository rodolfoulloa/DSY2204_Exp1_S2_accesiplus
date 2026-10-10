package cl.duoc.rulloa.accesiplus.domain

import cl.duoc.rulloa.accesiplus.data.model.UserProfile

/** Etapa en que está el usuario dentro de Escribir o Hablar (paso 6 del flujo: acción guiada). */
enum class EtapaAccion { INICIO, ESCUCHANDO, PROCESANDO, LISTO, ERROR }

/** Indicación breve del siguiente paso según el estado de la pantalla (lógica pura, probada con JUnit). */
object GuiaAccion {

    /** El error tiene prioridad: es lo primero que el usuario debe resolver. */
    fun etapaEscribir(escuchando: Boolean, procesando: Boolean, hayError: Boolean, completada: Boolean): EtapaAccion = when {
        hayError -> EtapaAccion.ERROR
        escuchando -> EtapaAccion.ESCUCHANDO
        procesando -> EtapaAccion.PROCESANDO
        completada -> EtapaAccion.LISTO
        else -> EtapaAccion.INICIO
    }

    fun mensajeEscribir(etapa: EtapaAccion): String = when (etapa) {
        EtapaAccion.INICIO -> "Paso 1: toca «Escuchar» y acerca el teléfono a quien te habla."
        EtapaAccion.ESCUCHANDO -> "Escuchando… Pide que hablen claro y cerca del teléfono. Toca «Detener» al terminar."
        EtapaAccion.PROCESANDO -> "Procesando lo que se dijo. Espera un momento…"
        EtapaAccion.LISTO -> "Listo: el texto quedó en tu historial. Puedes verlo en grande, escuchar otra vez o volver al menú."
        EtapaAccion.ERROR -> "Hubo un problema. Lee el aviso en rojo e intenta otra vez, o escribe con el teclado."
    }

    /** [preparando] = el motor de voz aún no termina de iniciar; [hablando] = está diciendo una frase. */
    fun etapaHablar(preparando: Boolean, hablando: Boolean, hayError: Boolean, completada: Boolean): EtapaAccion = when {
        hayError -> EtapaAccion.ERROR
        hablando || preparando -> EtapaAccion.PROCESANDO
        completada -> EtapaAccion.LISTO
        else -> EtapaAccion.INICIO
    }

    fun mensajeHablar(etapa: EtapaAccion, hayTexto: Boolean, preparando: Boolean = false): String = when (etapa) {
        EtapaAccion.INICIO, EtapaAccion.ESCUCHANDO ->
            if (hayTexto) "Paso 2: toca «Decir en voz alta»."
            else "Paso 1: escribe lo que quieres decir o elige una frase rápida."
        EtapaAccion.PROCESANDO ->
            if (preparando) "Preparando la voz…" else "Diciendo en voz alta… Muestra la pantalla a la otra persona."
        EtapaAccion.LISTO -> "Listo: se dijo en voz alta y quedó en tu historial. Puedes decir otra frase o volver al menú."
        EtapaAccion.ERROR -> "Hubo un problema con la voz. Muestra la frase en pantalla grande a la otra persona."
    }
}

/** Saludo del menú principal (paso 2 del flujo). */
object Saludos {

    /**
     * Primer nombre del perfil; si el perfil no tiene nombre (o no existe), la parte del correo
     * antes de la @. Así el saludo nunca queda sin nombre cuando hay datos del usuario.
     */
    fun nombre(perfil: UserProfile?, correo: String?): String? =
        perfil?.name?.trim()?.substringBefore(' ')?.takeIf { it.isNotEmpty() }
            ?: correo?.substringBefore('@')?.trim()?.takeIf { it.isNotEmpty() }

    fun texto(nombre: String?): String = if (nombre == null) "Hola" else "Hola, $nombre"
}
