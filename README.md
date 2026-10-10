# AccesiPlus

App Android (Kotlin + Jetpack Compose + Material 3) para personas con discapacidad sensorial
auditiva: permite **escribir** (voz a texto o teclado) y **hablar** (texto a voz) en el entorno
cotidiano, guardar frases y registrar dónde quedaron sus dispositivos (audífono, implante, teléfono).

- Paquete / applicationId: `cl.duoc.rulloa.accesiplus`
- minSdk 24 · targetSdk 37
- Back end: Firebase Authentication (correo/contraseña) + Realtime Database (proyecto `accesiplus`)
- Versión actual: **1.1.0** (`versionCode 3`)

## Descarga e instalación

La app se distribuye con **Firebase App Distribution**:

**[Descargar AccesiPlus en App Distribution](https://appdistribution.firebase.dev/i/6b3a888a4f074495)**

1. Abre el enlace desde el teléfono Android, escribe tu correo de Google y acepta participar
   como tester. No necesitas una invitación previa.
2. Revisa tu correo, abre la invitación de Firebase App Distribution e inicia sesión con esa
   misma cuenta. Toca **Descargar** en la versión más reciente (1.1.0).
3. Abre el archivo descargado. Android pedirá permiso para instalar apps de orígenes desconocidos:
   - Android 8 o superior: **Ajustes → Apps → Acceso especial → Instalar apps desconocidas**,
     elige el navegador (por ejemplo, Chrome) y activa **Permitir de esta fuente**.
   - Android 7: **Ajustes → Seguridad → Orígenes desconocidos**.
4. Vuelve atrás y toca **Instalar**. Si ya tenías una versión anterior, se instala como
   actualización y conservas tus datos (el APK está firmado con el mismo certificado).
5. Abre AccesiPlus e inicia sesión o crea una cuenta. Se necesita conexión a internet.

Requisitos: Android 7.0 (API 24) o superior.

## Novedades de la versión

### 1.1.0

- **Historial automático**: Escribir guarda cada texto escuchado y Hablar cada frase dicha en voz
  alta, sin tocar "Guardar". Se ve por día, con filtros Todos / Escribir / Hablar, opción de
  repetir en voz alta y de eliminar con "Deshacer". Funciona sin conexión.
- **Ayuda y tutorial**: tutorial de bienvenida de cuatro páginas la primera vez que entras, pantalla
  de Ayuda con el paso a paso de cada función, consejos para el micrófono y qué hacer sin conexión
  (desde el menú o desde el ícono **?** de la barra superior).
- **Guía paso a paso** en Escribir y Hablar, saludo con tu nombre y botón **Volver al menú** al
  terminar cada acción.
- **Vibración**: un toque corto al terminar, doble al guardar y largo ante un error, para no depender
  del sonido. Se puede apagar en **Mi perfil → Vibración**.

### 1.0.0

- Inicio de sesión, registro y recuperación de contraseña con Firebase.
- Escribir (voz a texto), Hablar (texto a voz con frases rápidas y propias), Buscar dispositivo con
  ubicación, widget "Frase rápida" y diseño adaptable a teléfono y tablet.

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
Versión publicada: `versionCode 3`, `versionName "1.1.0"`. Todas las versiones se firman con el
mismo certificado (`CN=Rodolfo Ulloa, OU=DSY2204`), requisito para instalarse como actualización.
