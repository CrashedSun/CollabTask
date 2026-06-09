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
- Projeto Firebase configurado para Authentication e Firestore

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

## Configurar Firebase

1. Crie um projeto no console do Firebase.
2. Adicione um app Android com o package `com.topespinf.collabtask`.
3. Baixe o arquivo `google-services.json` e substitua o arquivo placeholder em `app/google-services.json`.
4. Ative **Authentication** e habilite **Email/Password** e **Google**.
5. Ative **Cloud Firestore**.
6. Adicione o SHA-1/SHA-256 do app no Firebase e baixe novamente o `google-services.json` depois disso.
6. Crie as coleções:
   - `users` com documentos por `uid`
   - `tasks` com documentos por tarefa
7. Use estas regras iniciais no Firestore:

```txt
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Estrutura do Firestore

- `users/{uid}`: `name`, `email`, `role`, `createdAt` *(Timestamp)*
- `tasks/{taskId}`: `title`, `description`, `ownerId`, `ownerName`, `status`, `comments`, `participants`, `milestones`, `createdAt` *(Timestamp)*, `updatedAt` *(Timestamp)*
- `milestones[]`: `id`, `title`, `completed`, `completedById`, `completedByName`, `completedAt` *(Timestamp?)*

