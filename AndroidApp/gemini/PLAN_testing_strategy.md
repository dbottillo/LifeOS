# Testing Strategy Plan - LifeOS Android App

This plan outlines a multi-step approach to introducing testing to the LifeOS Android application, focusing on **Unit Tests** for business logic and **Official Snapshot Tests** for the UI.

## Current State Assessment
- **Architecture:** UI (Compose) -> ViewModel -> Use Case / Repository -> Network / DB (Room).
- **Libraries:** JUnit 5, Mockito, Truth, Turbine, and Coroutines Test are already in the `libs.versions.toml`.
- **Gaps:** No testing implementation exists.
- **UI Testing Potential:** Several `@Preview` annotations are already present in `HomeScreen`, `ReviewScreen`, and `ArticlesScreen`.

---

## Step 1: Infrastructure & Environment Setup
Before writing tests, we need to ensure the environment is ready.

1.  **Add Android Compose Preview Screenshot Testing:** 
    - Integrate the official Google plugin: `com.android.compose.screenshot`.
    - This is the preferred choice because it leverages existing `@Preview` functions, requiring zero extra test code for basic visual verification.
    - Enable `android.experimental.enableScreenshotTest=true` in `gradle.properties`.
2.  **Verify JUnit 5 Integration:** Ensure the `build.gradle.kts` is correctly configured to run JUnit 5 tests.
3.  **Base Test Classes:** Create reusable base classes for **ViewModel Tests**, handling Coroutine Dispatchers (using `TestDispatcher`).

---

## Step 2: Unit Testing Core Logic (High Value, Low Effort)
Start with pure logic components that don't have many dependencies.

1.  **Mappers (`TasksMapper.kt`):**
    - Verify conversion between DB entities (`NotionEntry`) and Domain models.
    - Test edge cases for optional properties (Due date, Price, URL).
2.  **Use Cases (`WeeklyTasksUseCase.kt`):**
    - Test the logic for filtering and categorizing tasks for the weekly view.
3.  **Utility Classes:** Test any date formatting or string manipulation in the `util` package.

---

## Step 3: Repository & ViewModel Testing
Test the coordination between data sources and UI state.

1.  **Repositories (`TasksRepository.kt`):**
    - Mock `NotionEntryDao` and `NotionService`.
    - Verify that data is correctly fetched, cached, and mapped.
    - Test error handling (e.g., network failure).
2.  **ViewModels (`HomeViewModel.kt`, etc.):**
    - Use **Turbine** to test state flows (`StateFlow`).
    - Verify that UI state updates correctly when data is loaded or actions are performed.
    - Test navigation events and error states.

---

## Step 4: Screenshot Testing (Visual Verification)
Ensure UI consistency and prevent regressions using the official library.

1.  **Leverage Existing Previews:** 
    - Use `./gradlew updateDebugScreenshotTest` to generate initial "golden" images from existing previews in `HomeScreen.kt`, `ReviewScreen.kt`, etc.
2.  **Expand Preview Coverage:**
    - Add more `@Preview` variations for Loading, Empty, and Error states to automatically include them in visual testing.
    - Test both Light and Dark modes via preview parameters.
3.  **Validation:** 
    - Run `./gradlew validateDebugScreenshotTest` as part of the CI/CD or local verification process.

---

## Step 5: Worker & Database Testing (Ensuring Reliability)
Test components that interact with the system or have complex DB logic.

1.  **Workers (`AddTaskWorker.kt`):**
    - Use `WorkManager` test helpers to verify that background tasks are triggered and executed correctly.
2.  **DAO Tests (`NotionEntryDao.kt`):**
    - Use In-memory Room database to test complex queries or migrations.

---

## Success Metrics
- **Coverage:** Aim for 70%+ coverage on `feature` and `data` packages.
- **Reliability:** Screenshot tests should be stable and integrated into the development workflow.
- **Ease of Use:** Adding a new visual test should be as simple as adding a new `@Preview`.
