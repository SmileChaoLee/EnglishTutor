# Implementation Plan - English Tutor Chat UI with MVI/MVVM

Implement a chat interface for the English Tutor app using MVVM and MVI patterns. The UI will feature a scrollable chat history, an input box for questions, and a submit button.

## Proposed Changes

### [Dependencies]

#### [libs.versions.toml](file:///home/chaolee/AndroidStudioProjects/EnglishTutor/gradle/libs.versions.toml)
- Add `androidx-lifecycle-viewmodel-compose` dependency.

#### [build.gradle.kts](file:///home/chaolee/AndroidStudioProjects/EnglishTutor/app/build.gradle.kts)
- Add `androidx-lifecycle-viewmodel-compose` to dependencies.

### [Data Models]

#### [ChatMessage.kt](file:///home/chaolee/AndroidStudioProjects/EnglishTutor/app/src/main/java/com/smile/englishtutor/models/ChatMessage.kt)
- [NEW] Define `ChatMessage` data class: `val text: String`, `val isUser: Boolean`.

### [MVI Components]

#### [ChatIntent.kt](file:///home/chaolee/AndroidStudioProjects/EnglishTutor/app/src/main/java/com/smile/englishtutor/mvi/ChatIntent.kt)
- [NEW] Define `ChatIntent` sealed class: `UpdateInput(val text: String)`, `SendMessage`.

#### [ChatState.kt](file:///home/chaolee/AndroidStudioProjects/EnglishTutor/app/src/main/java/com/smile/englishtutor/mvi/ChatState.kt)
- [NEW] Define `ChatState` data class: `val messages: List<ChatMessage>`, `val inputText: String`, `val isLoading: Boolean`.

### [MVVM Components]

#### [ChatViewModel.kt](file:///home/chaolee/AndroidStudioProjects/EnglishTutor/app/src/main/java/com/smile/englishtutor/viewmodels/ChatViewModel.kt)
- [NEW] Implement `ChatViewModel` inheriting from `ViewModel`.
- Manage `ChatState` using `MutableStateFlow`.
- Handle `ChatIntent` through a `handleIntent` method.
- Use `viewModelScope.launch` and `withContext(Dispatchers.IO)` to call `RestApiSync.getAgentResponse`.

### [UI Components]

#### [ChatScreen.kt](file:///home/chaolee/AndroidStudioProjects/EnglishTutor/app/src/main/java/com/smile/englishtutor/ui/ChatScreen.kt)
- [NEW] Implement `ChatScreen` composable.
- `LazyColumn` for chat history.
- Bottom bar with `TextField` and `IconButton` (submit).

#### [MainActivity.kt](file:///home/chaolee/AndroidStudioProjects/EnglishTutor/app/src/main/java/com/smile/englishtutor/MainActivity.kt)
- Replace `Greeting` with `ChatScreen`.
- Initialize `ChatViewModel`.

## Verification Plan

### Manual Verification
- Deploy the app to the emulator using `deploy` tool.
- Interact with the UI:
    - Type a question in the input box.
    - Click the submit button.
    - Verify that the question appears in the chat history.
    - Verify that the AI response appears.
    - Verify that the chat history scrolls when many messages are added.
