package cl.duoc.rulloa.accesiplus.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.rulloa.accesiplus.AccesiPlusApp
import cl.duoc.rulloa.accesiplus.di.AppContainer
import cl.duoc.rulloa.accesiplus.ui.auth.AuthViewModel
import cl.duoc.rulloa.accesiplus.ui.devices.DeviceViewModel
import cl.duoc.rulloa.accesiplus.ui.historial.HistorialViewModel
import cl.duoc.rulloa.accesiplus.ui.phrases.PhraseViewModel
import cl.duoc.rulloa.accesiplus.ui.profile.ProfileViewModel
import cl.duoc.rulloa.accesiplus.ui.tutorial.TutorialViewModel

/** Obtiene el contenedor de dependencias desde la Application. */
private fun CreationExtras.contenedor(): AppContainer =
    (this[APPLICATION_KEY] as AccesiPlusApp).container

/**
 * Fábrica de ViewModels con el DSL viewModelFactory { initializer { } } de lifecycle-viewmodel.
 * Los ViewModels reciben interfaces de repositorio: nunca tocan Firebase directamente.
 */
object FabricaViewModels {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val c = contenedor()
            AuthViewModel(c.authRepository, c.userRepository, c.haptica)
        }
        initializer {
            val c = contenedor()
            ProfileViewModel(c.authRepository, c.userRepository)
        }
        initializer {
            val c = contenedor()
            PhraseViewModel(c.authRepository, c.phraseRepository, c.voz, c.historialRepository, c.haptica)
        }
        initializer {
            val c = contenedor()
            HistorialViewModel(c.authRepository, c.historialRepository, c.voz, c.haptica)
        }
        initializer {
            val c = contenedor()
            TutorialViewModel(c.authRepository, c.preferencias)
        }
        initializer {
            val c = contenedor()
            DeviceViewModel(c.authRepository, c.deviceRepository, c.ubicacion, c.haptica)
        }
    }
}
