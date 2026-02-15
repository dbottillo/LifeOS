# Feature: Select Parent Item in Task Composer

## Description
This feature introduces the ability to link a new task to an existing Notion entity, referred to as a "parent item," directly from the task composer. Users will be able to search for and select a Notion entity, and its ID will be associated with the new task when it's sent to the Notion API. This plan focuses solely on adding a parent item to a *new* task, not editing existing tasks.

## Plan of Changes

### 1. User Interface (UI) - `TaskComposerScreen.kt`, `TaskComposerActivity.kt`
*   **Utilize Existing "Parent Item" Section:**
    *   Leverage the existing "Parent item placeholder section with a button" in `TaskComposerScreen.kt`.
    *   The existing button in this section will be used to trigger the parent item search and selection flow.
*   **Implement Search and Selection UI:**
    *   When the user taps the existing button, a new screen or a dialog should appear, allowing the user to search for Notion entities.
    *   This search UI will include:
        *   An input field for the user to type search queries.
        *   A mechanism to display search results (e.g., a `LazyColumn` for a list of Notion entities).
        *   A way to select an entity from the search results.
        *   A way to dismiss the search UI.
*   **Display Selected Parent:**
    *   Once a parent item is selected, its title or a unique identifier should be displayed within the existing "Parent item placeholder section" in `TaskComposerScreen.kt`, replacing the placeholder text and indicating that a parent has been chosen.
    *   Provide an option (e.g., a clear icon or button) within this section to clear the selected parent.

### 2. ViewModel (`TaskComposerViewModel.kt`)
*   **State Management for Parent Selection:**
    *   Add a new `MutableStateFlow` or `MutableState` to `TaskComposerViewModel.kt` to hold the currently selected parent Notion entity (or at least its ID and display name).
    *   Add a `MutableStateFlow` or `MutableState` to manage the search query for parent items and the list of search results.
*   **Handle UI Events:**
    *   Create functions in the ViewModel to handle user interaction with the "Select Parent" UI element (e.g., `onSelectParentClicked()`).
    *   Create a function to handle search query changes (e.g., `onParentSearchQueryChanged(query: String)`). This function will likely debounce the input to avoid excessive network calls.
    *   Create a function to handle the selection of a parent item from the search results (e.g., `onParentSelected(parentEntity: NotionEntity)`).
    *   Create a function to clear the selected parent (e.g., `onClearParentSelected()`).
*   **Integrate with Data Layer (New Use Case):**
    *   Call a new use case (e.g., `SearchNotionEntitiesUseCase`) to fetch Notion entities based on the search query.
    *   Update the ViewModel's state with the search results.
*   **Update Task Creation Logic:**
    *   Modify the `createTask` function to include the `parent_id` from the ViewModel's state in the task payload sent to the Notion API.

### 3. Data/Domain Layer - (e.g., `data/repository`, `domain/usecase`, `model`)
*   **New Use Case:**
    *   Create a new use case, `SearchNotionEntitiesUseCase`, that will be responsible for orchestrating the search for Notion entities. This use case will likely depend on a new or existing repository interface.
*   **Repository Interface:**
    *   Define or extend an interface (e.g., `NotionRepository`) with a new function like `searchNotionEntities(query: String): Flow<List<NotionEntity>>`.
*   **Data Models:**
    *   Ensure there's a suitable data model (`NotionEntity` or similar) to represent the Notion entities returned by the search, containing at least the ID and a displayable title/name.

### 4. Network Layer - (e.g., `network/service`, `network/api`)
*   **Notion API Client Extension:**
    *   If not already present, extend the existing Notion API service interface (e.g., `NotionApiService`) with a new endpoint to search for Notion entities. This might involve a GET request with a query parameter.
    *   The endpoint should return a list of Notion entities that match the search query.
*   **Notion API DTOs:**
    *   Define necessary Data Transfer Objects (DTOs) for the request and response of the search API call, mapping them to the domain `NotionEntity` model.

### Files Likely to be Modified or Created:
*   `app/src/main/java/com/dbottillo/lifeos/feature/composer/TaskComposerScreen.kt` (UI changes, event handling)
*   `app/src/main/java/com/dbottillo/lifeos/feature/composer/TaskComposerViewModel.kt` (State management, business logic, use case calls)
*   `app/src/main/java/com/dbottillo/lifeos/feature/composer/TaskComposerActivity.kt` (Potentially for navigating to a search screen or showing a dialog)
*   `app/src/main/java/com/dbottillo/lifeos/domain/usecase/SearchNotionEntitiesUseCase.kt` (New use case)
*   `app/src/main/java/com/dbottillo/lifeos/data/NotionRepository.kt` (New function in interface)
*   `app/src/main/java/com/dbottillo/lifeos/data/NotionRepositoryImpl.kt` (Implementation of new function)
*   `app/src/main/java/com/dbottillo/lifeos/network/NotionApiService.kt` (New API endpoint)
*   `app/src/main/java/com/dbottillo/lifeos/network/model/NotionEntityDto.kt` (New DTOs if required)
*   `app/src/main/java/com/dbottillo/lifeos/model/NotionEntity.kt` (New or updated domain model)
*   Potentially new Composables for the search UI.
