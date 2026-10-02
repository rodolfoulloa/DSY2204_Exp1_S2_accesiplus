package cl.duoc.rulloa.accesiplus.data.repository

import kotlin.coroutines.cancellation.CancellationException

/**
 * Igual que runCatching, pero deja pasar la cancelación de corrutinas
 * (si se capturara, un ViewModel destruido seguiría ejecutando código).
 */
suspend fun <T> resultado(bloque: suspend () -> T): Result<T> = try {
    Result.success(bloque())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
