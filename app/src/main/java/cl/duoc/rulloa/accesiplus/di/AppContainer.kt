package cl.duoc.rulloa.accesiplus.di

import android.content.Context
import cl.duoc.rulloa.accesiplus.data.connectivity.AndroidConnectivityObserver
import cl.duoc.rulloa.accesiplus.data.connectivity.ConnectivityObserver
import cl.duoc.rulloa.accesiplus.data.location.FusedUbicacionProvider
import cl.duoc.rulloa.accesiplus.data.location.UbicacionProvider
import cl.duoc.rulloa.accesiplus.data.tts.TtsService
import cl.duoc.rulloa.accesiplus.data.tts.Voz
import cl.duoc.rulloa.accesiplus.data.remote.RutasFirebase
import cl.duoc.rulloa.accesiplus.data.repository.AuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.DeviceRepository
import cl.duoc.rulloa.accesiplus.data.repository.FirebaseAuthRepository
import cl.duoc.rulloa.accesiplus.data.repository.FirebaseDeviceRepository
import cl.duoc.rulloa.accesiplus.data.repository.FirebasePhraseRepository
import cl.duoc.rulloa.accesiplus.data.repository.FirebaseUserRepository
import cl.duoc.rulloa.accesiplus.data.repository.PhraseRepository
import cl.duoc.rulloa.accesiplus.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

/**
 * Inyección de dependencias manual: crea una sola instancia de cada servicio.
 * Firebase.auth y Firebase.database son extensiones Kotlin (antes Firebase KTX).
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val rutas by lazy { RutasFirebase(Firebase.database) }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository(Firebase.auth) }
    val userRepository: UserRepository by lazy { FirebaseUserRepository(rutas) }
    val phraseRepository: PhraseRepository by lazy { FirebasePhraseRepository(rutas) }
    val deviceRepository: DeviceRepository by lazy { FirebaseDeviceRepository(rutas) }
    val connectivityObserver: ConnectivityObserver by lazy { AndroidConnectivityObserver(appContext) }
    val voz: Voz by lazy { TtsService(appContext) }
    val ubicacion: UbicacionProvider by lazy { FusedUbicacionProvider(appContext) }
}
