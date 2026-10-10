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

// keystore.properties (no versionado) apunta al keystore guardado fuera del repositorio
val propiedadesFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
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
        versionCode = 3
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Se pasan como argumentos del runner (am instrument -e), no quedan dentro del APK
        propiedadesLocales.getProperty("accesiplus.pruebaCorreo")?.let {
            testInstrumentationRunnerArguments["pruebaCorreo"] = it
        }
        propiedadesLocales.getProperty("accesiplus.pruebaClave")?.let {
            testInstrumentationRunnerArguments["pruebaClave"] = it
        }
    }

    signingConfigs {
        // Solo se crea si existe keystore.properties; sin él, el APK release queda sin firmar
        if (propiedadesFirma.getProperty("storeFile") != null) {
            create("release") {
                storeFile = rootProject.file(propiedadesFirma.getProperty("storeFile"))
                storePassword = propiedadesFirma.getProperty("storePassword")
                keyAlias = propiedadesFirma.getProperty("keyAlias")
                keyPassword = propiedadesFirma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            // R8: reduce y optimiza el código (Compose sin optimizar corre bastante más lento)
            optimization {
                enable = true
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

// Muestra cada prueba (PASSED / FAILED) en la consola
tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
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
