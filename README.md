# CollabTask Android

Aplicativo Android em Kotlin + Jetpack Compose baseado nas telas de referencia:
- Home (landing)
- Tarefas
- Perfil
- Redefinicao de senha

## Estrutura principal

- `app/src/main/java/com/topespinf/collabtask/MainActivity.kt`
- `app/src/main/java/com/topespinf/collabtask/ui/CollabTaskApp.kt`
- `app/src/main/java/com/topespinf/collabtask/navigation/AppNavGraph.kt`
- `app/src/main/java/com/topespinf/collabtask/ui/components/CollabBottomBar.kt`
- `app/src/main/java/com/topespinf/collabtask/ui/screens/`

## Requisitos

- Android SDK configurado
- JDK 21 (Android Studio JBR funciona)

## Executar testes locais

No PowerShell:

```powershell
Set-Location "C:\Users\DarkFeathers\Documents\Programas\ColabTask"
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
.\gradlew.bat :app:testDebugUnitTest --no-daemon
```

## Rodar no Android Studio

1. Abra a pasta do projeto.
2. Sincronize o Gradle.
3. Execute o app no emulador/dispositivo.

