import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

// local.properties no se versiona: guarda el SDK y las credenciales del usuario de prueba
// que usan los tests instrumentados (accesiplus.pruebaCorreo / accesiplus.pruebaClave).
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "cl.duoc.rulloa.accesiplus"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.rulloa.accesiplus"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Se pasan como argumentos del runner (am instrument -e), no quedan dentro del APK
        propiedadesLocales.getProperty("accesiplus.pruebaCorreo")?.let {
            testInstrumentationRunnerArguments["pruebaCorreo"] = it
        }
        propiedadesLocales.getProperty("accesiplus.pruebaClave")?.let {
            testInstrumentationRunnerArguments["pruebaClave"] = it
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        // Robolectric necesita los recursos de Android (layouts, strings) en las pruebas JVM
        unitTests.isIncludeAndroidResources = true
    }
}

// Cobertura con Kover: se excluye código generado que no tiene lógica propia
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*ComposableSingletons*",
                    "*.BuildConfig",
                    "*_Impl", "*_Impl\$*",
                    "*.ui.theme.*"
                )
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Firebase (el BoM fija versiones compatibles entre sí)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.kotlinx.coroutines.play.services)

    // Ubicación (BuscarDispositivo) y diseño adaptativo (WindowSizeClass)
    implementation(libs.play.services.location)
    implementation(libs.androidx.compose.material3.window.size)

    // Componentes Android: Room (caché + ContentProvider), Palette y Fragment en Compose
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.fragment.compose)

    // Pruebas JVM: JUnit, Mockito (mockito-kotlin), corrutinas de prueba y Robolectric
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)

    // Pruebas instrumentadas en el emulador: Compose UI test + Espresso
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
