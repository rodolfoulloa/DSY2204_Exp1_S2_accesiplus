# AccesiPlus

App Android (Kotlin + Jetpack Compose + Material 3) para personas con discapacidad sensorial
auditiva: permite **escribir** (voz a texto o teclado) y **hablar** (texto a voz) en el entorno
cotidiano, guardar frases y registrar dónde quedaron sus dispositivos (audífono, implante, teléfono).

- Paquete / applicationId: `cl.duoc.rulloa.accesiplus`
- minSdk 24 · targetSdk 37
- Back end: Firebase Authentication (correo/contraseña) + Realtime Database (proyecto `accesiplus`)

## Arquitectura

```
UI (Compose) → ViewModel (StateFlow, viewModelScope) → Repository (interfaces) → Firebase / Room
```

- `data/repository`: `AuthRepository`, `UserRepository`, `PhraseRepository`, `DeviceRepository`.
  Usan corrutinas, `await()` (kotlinx-coroutines-play-services) y `callbackFlow`.
- `data/remote/RutasFirebase.kt`: **único** lugar que crea referencias a la base de datos.
  Todas cuelgan de `users/{uid}`.
- Los ViewModels reciben interfaces y nunca usan Firebase directamente (se prueban con Mockito).
- `AccesiPlusApp` activa la persistencia offline (`setPersistenceEnabled(true)`).

## Estructura de datos (Realtime Database)

Todos los datos de la app viven bajo el usuario autenticado. No hay nodos en la raíz ni compartidos.
Las frases rápidas por categoría están en el código (`data/model/FrasesRapidas.kt`).

```
users/
  {uid}/
    profile/      name, email, role, gender, createdAt
    phrases/
      {phraseId}/ text, category, favorite, uses, createdAt, updatedAt
    devices/
      {deviceId}/ name, type, notes, lat, lon, locationAt, createdAt
```

## Reglas de seguridad

El archivo [`database.rules.json`](database.rules.json) contiene las reglas definitivas:

- Todo lo que no sea `users/$uid` está **denegado** (`.read` y `.write` en `false` en la raíz).
- `users/$uid` solo lo puede leer y escribir su dueño: `auth != null && auth.uid === $uid`.
- `.validate` en `profile`, `phrases` y `devices`: campos obligatorios, tipos, largos máximos,
  rangos de latitud/longitud, `email` igual al del token y rechazo de campos desconocidos (`$otro`).

### Publicar las reglas (manual)

La base se creó en **modo de prueba** (abierta por 30 días). Antes de entregar:

1. Abrir la consola de Firebase → **Realtime Database** → pestaña **Reglas**.
2. Pegar el contenido de `database.rules.json`.
3. Presionar **Publicar**.

## Compilar y probar

```
./gradlew assembleDebug            # APK de depuración
./gradlew testDebugUnitTest        # JUnit + Mockito + Robolectric
./gradlew connectedDebugAndroidTest   # Compose UI test en emulador
./gradlew koverHtmlReportDebug     # cobertura
./gradlew assembleRelease          # APK firmado (requiere keystore.properties)
```

## Firma

`keystore.properties` (ignorado por git) apunta a un keystore fuera del repositorio:

```
storeFile=../../keystore-accesiplus/accesiplus.jks
storePassword=…
keyAlias=accesiplus
keyPassword=…
```
