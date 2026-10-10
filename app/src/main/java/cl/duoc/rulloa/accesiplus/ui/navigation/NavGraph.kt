package cl.duoc.rulloa.accesiplus.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import cl.duoc.rulloa.accesiplus.data.model.FrasesRapidas
import cl.duoc.rulloa.accesiplus.ui.hablar.ColoresCategoria
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import cl.duoc.rulloa.accesiplus.ui.FabricaViewModels
import cl.duoc.rulloa.accesiplus.ui.auth.AuthViewModel
import cl.duoc.rulloa.accesiplus.ui.auth.EstadoSesion
import cl.duoc.rulloa.accesiplus.ui.auth.LoginScreen
import cl.duoc.rulloa.accesiplus.ui.auth.RecoverPasswordScreen
import cl.duoc.rulloa.accesiplus.ui.auth.RegisterScreen
import cl.duoc.rulloa.accesiplus.ui.devices.BuscarDispositivoScreen
import cl.duoc.rulloa.accesiplus.ui.devices.DeviceViewModel
import cl.duoc.rulloa.accesiplus.ui.escribir.EscribirScreen
import cl.duoc.rulloa.accesiplus.ui.hablar.HablarScreen
import cl.duoc.rulloa.accesiplus.ui.historial.HistorialScreen
import cl.duoc.rulloa.accesiplus.ui.historial.HistorialViewModel
import cl.duoc.rulloa.accesiplus.ui.home.HomeMenuScreen
import cl.duoc.rulloa.accesiplus.ui.home.TamanoVentana
import cl.duoc.rulloa.accesiplus.ui.phrases.PhraseViewModel
import cl.duoc.rulloa.accesiplus.ui.profile.ProfileScreen
import cl.duoc.rulloa.accesiplus.ui.profile.ProfileViewModel

/** Pedido que llega desde el widget \"Frase rápida\". */
data class SolicitudWidget(val frase: String?)

/** Rutas como constantes (mitigación del riesgo "navegación mal estructurada"). */
object Rutas {
    // Grafo público: solo se puede ver sin sesión
    const val GRAFO_AUTH = "auth"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"

    // Grafo protegido: requiere sesión activa
    const val GRAFO_APP = "app"
    const val HOME = "home"
    const val ESCRIBIR = "escribir"
    const val ARG_FRASE = "frase"
    const val HABLAR = "hablar?$ARG_FRASE={$ARG_FRASE}"
    const val BUSCAR = "buscar_dispositivo"
    const val PERFIL = "perfil"
    const val HISTORIAL = "historial"

    fun hablar(frase: String? = null) =
        if (frase == null) "hablar" else "hablar?$ARG_FRASE=${Uri.encode(frase)}"
}

/**
 * @param solicitudWidget toque en el widget: abre Hablar y, si trae frase, la dice.
 * @param contenidoBuscar contenido extra para BuscarDispositivo (Fragment de consejos).
 */
@Composable
fun AccesiPlusNavHost(
    navController: NavHostController,
    tamano: TamanoVentana,
    authViewModel: AuthViewModel = viewModel(factory = FabricaViewModels.Factory),
    solicitudWidget: SolicitudWidget? = null,
    onSolicitudWidgetAtendida: () -> Unit = {},
    contenidoBuscar: @Composable () -> Unit = {}
) {
    val sesion by authViewModel.sesion.collectAsStateWithLifecycle()

    if (sesion == EstadoSesion.Cargando) {
        Box(Modifier.fillMaxSize().testTag("cargando_sesion"), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (sesion is EstadoSesion.Activa) Rutas.GRAFO_APP else Rutas.GRAFO_AUTH
    ) {
        navigation(route = Rutas.GRAFO_AUTH, startDestination = Rutas.LOGIN) {
            composable(Rutas.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onRegisterClick = { navController.navigate(Rutas.REGISTRO) },
                    onRecoverPasswordClick = { navController.navigate(Rutas.RECUPERAR) }
                )
            }
            composable(Rutas.REGISTRO) {
                RegisterScreen(viewModel = authViewModel, onBackToLogin = { navController.popBackStack() })
            }
            composable(Rutas.RECUPERAR) {
                RecoverPasswordScreen(viewModel = authViewModel, onBackToLogin = { navController.popBackStack() })
            }
        }
        navigation(route = Rutas.GRAFO_APP, startDestination = Rutas.HOME) {
            composable(Rutas.HOME) {
                val perfilVm: ProfileViewModel = viewModel(factory = FabricaViewModels.Factory)
                HomeMenuScreen(
                    perfilViewModel = perfilVm,
                    tamano = tamano,
                    onEscribir = { navController.navigate(Rutas.ESCRIBIR) },
                    onHablar = { navController.navigate(Rutas.hablar()) },
                    onBuscar = { navController.navigate(Rutas.BUSCAR) },
                    onHistorial = { navController.navigate(Rutas.HISTORIAL) },
                    onPerfil = { navController.navigate(Rutas.PERFIL) }
                )
            }
            composable(Rutas.ESCRIBIR) {
                val vm: PhraseViewModel = viewModel(factory = FabricaViewModels.Factory)
                EscribirScreen(viewModel = vm, onVolver = { navController.popBackStack() })
            }
            composable(
                Rutas.HABLAR,
                arguments = listOf(navArgument(Rutas.ARG_FRASE) { type = NavType.StringType; nullable = true })
            ) { entrada ->
                val vm: PhraseViewModel = viewModel(factory = FabricaViewModels.Factory)
                val context = LocalContext.current
                // Palette se calcula una vez por pantalla, fuera del hilo principal
                val colores by produceState(emptyMap<String, Color>()) {
                    value = ColoresCategoria.calcular(context, FrasesRapidas.nombres)
                }
                HablarScreen(
                    viewModel = vm,
                    onVolver = { navController.popBackStack() },
                    fraseInicial = entrada.arguments?.getString(Rutas.ARG_FRASE),
                    colorCategoria = { colores[it] }
                )
            }
            composable(Rutas.BUSCAR) {
                val vm: DeviceViewModel = viewModel(factory = FabricaViewModels.Factory)
                BuscarDispositivoScreen(
                    viewModel = vm,
                    onVolver = { navController.popBackStack() },
                    contenidoExtra = contenidoBuscar
                )
            }
            composable(Rutas.HISTORIAL) {
                val vm: HistorialViewModel = viewModel(factory = FabricaViewModels.Factory)
                HistorialScreen(viewModel = vm, onVolver = { navController.popBackStack() })
            }
            composable(Rutas.PERFIL) {
                val vm: ProfileViewModel = viewModel(factory = FabricaViewModels.Factory)
                ProfileScreen(viewModel = vm, onVolver = { navController.popBackStack() })
            }
        }
    }

    // Guardia de sesión: si la sesión cambia, se reemplaza toda la pila de navegación.
    // Así no se puede volver con "Atrás" a una pantalla protegida después de cerrar sesión.
    LaunchedEffect(sesion) {
        val actual = navController.currentBackStackEntry?.destination ?: return@LaunchedEffect
        val enAuth = actual.hierarchy.any { it.route == Rutas.GRAFO_AUTH }
        val destino = when {
            sesion is EstadoSesion.Activa && enAuth -> Rutas.GRAFO_APP
            sesion is EstadoSesion.SinSesion && !enAuth -> Rutas.GRAFO_AUTH
            else -> null
        }
        if (destino != null) {
            navController.navigate(destino) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Toque en el widget: solo con sesión activa (si no, queda esperando el login)
    LaunchedEffect(solicitudWidget, sesion) {
        if (solicitudWidget != null && sesion is EstadoSesion.Activa) {
            // Nueva entrada de Hablar sobre el Home (si ya estaba abierta, se reemplaza con la nueva frase)
            navController.navigate(Rutas.hablar(solicitudWidget.frase)) { popUpTo(Rutas.HOME) }
            onSolicitudWidgetAtendida()
        }
    }
}
