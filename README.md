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
    history/
      {pushId}/   tipo (ESCRIBIR | HABLAR), texto, origen?, timestamp (hora del servidor)
```

`history` se llena solo: Escribir guarda cada resultado final del reconocimiento de voz y Hablar
cada texto que se dice en voz alta (texto libre, frase guardada o widget). La app lee los últimos
200 registros ordenados por `timestamp`.

## Reglas de seguridad

El archivo [`database.rules.json`](database.rules.json) contiene las reglas definitivas:

- Todo lo que no sea `users/$uid` está **denegado** (`.read` y `.write` en `false` en la raíz).
- `users/$uid` solo lo puede leer y escribir su dueño: `auth != null && auth.uid === $uid`.
- `.validate` en `profile`, `phrases` y `devices`: campos obligatorios, tipos, largos máximos,
  rangos de latitud/longitud, `email` igual al del token y rechazo de campos desconocidos (`$otro`).
- `history`: `tipo` solo `ESCRIBIR` o `HABLAR`, `texto` de 1 a 2000 caracteres, `origen` dentro
  de los valores conocidos, `timestamp` numérico no futuro y `.indexOn` sobre `timestamp`.

### Publicar las reglas

Con Firebase CLI (`firebase.json` y `.firebaserc` ya apuntan al proyecto `accesiplus`):

```
firebase login
firebase deploy --only database
```

O en la consola de Firebase → **Realtime Database** → pestaña **Reglas**: pegar el contenido de
`database.rules.json` y presionar **Publicar**.

## Compilar y probar

```
./gradlew assembleDebug            # APK de depuración
./gradlew testDebugUnitTest        # JUnit + Mockito + Robolectric
./gradlew connectedDebugAndroidTest   # Compose UI test en emulador
./gradlew koverHtmlReportDebug     # cobertura
./gradlew assembleRelease          # APK firmado (requiere keystore.properties)
```

## Firma

`keystore.properties` (ignorado por git, en la raíz del proyecto) apunta a un keystore guardado
**fuera** del repositorio, en `../keystore-accesiplus/accesiplus.jks` (PKCS12, RSA 2048, alias
`accesiplus`, válido 10 000 días). Ruta relativa a la raíz del proyecto:

```
storeFile=../keystore-accesiplus/accesiplus.jks
storePassword=…
keyAlias=accesiplus
keyPassword=…
```

Si `keystore.properties` no existe, `assembleRelease` genera un APK sin firmar.
Versión publicada: `versionCode 2`, `versionName "1.0.0"`.
