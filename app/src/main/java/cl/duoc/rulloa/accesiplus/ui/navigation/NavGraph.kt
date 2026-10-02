package cl.duoc.rulloa.accesiplus.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import cl.duoc.rulloa.accesiplus.ui.FabricaViewModels
import cl.duoc.rulloa.accesiplus.ui.auth.AuthViewModel
import cl.duoc.rulloa.accesiplus.ui.auth.EstadoSesion
import cl.duoc.rulloa.accesiplus.ui.auth.LoginScreen
import cl.duoc.rulloa.accesiplus.ui.auth.RecoverPasswordScreen
import cl.duoc.rulloa.accesiplus.ui.auth.RegisterScreen
import cl.duoc.rulloa.accesiplus.ui.main.MainScreen
import cl.duoc.rulloa.accesiplus.ui.profile.ProfileScreen
import cl.duoc.rulloa.accesiplus.ui.profile.ProfileViewModel

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
    const val PERFIL = "perfil"
}

@Composable
fun AccesiPlusNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(factory = FabricaViewModels.Factory)
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
                MainScreen(onPerfil = { navController.navigate(Rutas.PERFIL) })
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
}
