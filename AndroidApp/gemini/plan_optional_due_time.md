# Plan: Optional Time Selection for Due Date

This plan outlines the changes required to add an optional time selection to the "Due date" picker in the task composer screen.

## Goals
- Allow users to select an optional time when setting a due date for a task.
- Display the selected time in the composer UI.
- Correctily sync the date and time to Notion.
- Ensure existing tasks with time are correctly loaded and displayed.

## Proposed Changes

### 1. ViewModel Updates (`TaskComposerViewModel.kt`)
- Update `ComposerState.Data` to include:
    - `selectedDueTime: Pair<Int, Int>? = null` (representing hour and minute).
- Update `init` logic to extract time from `NotionEntry` when editing a task. `NotionEntryDateMapper` already parses the date; we might need to adjust it or the VM logic to extract the hour and minute.
- Add new events/functions:
    - `onTimeSelected(hour: Int, minute: Int)`: Updates the state with the selected time.
    - `onClearTime()`: Removes the selected time.
- Update `saveLifeOs` and `editTask` to pass the `hasTime` information.

### 2. UI Updates (`TaskComposerScreen.kt`)
- Create a unified `DueDateTimePickerModal`.
- This modal will manage its own internal state: `ShowingDate` or `ShowingTime`.
- **Flow**:
    1. Initially display the standard Material3 `DatePicker`.
    2. Add a "Set Time" switch or button in the `DatePickerDialog`'s action area (or just above the buttons).
    3. If "Set Time" is active, the "OK" button becomes "Next".
    4. Clicking "Next" animates/switches the dialog content to a Material3 `TimePicker`.
    5. The user can then select the time and click "Confirm" to save both, or "Back" to return to date selection.
- Update `DueDatePicker` component:
    - Display both date and time if `selectedDueTime` is present (e.g., "Due: 02/03/2026 14:30").
    - Provide a "Clear" icon to reset both date and time, or just time.
- Update `ComposerState.Data.formattedDate` to use a logic that includes time if present.

### 3. Repository and Worker Updates
- **`TaskManager.kt`**:
    - Update `addTask` to include `hasTime: Boolean`.
    - Add `ADD_PAGE_HAS_TIME` to `workDataOf`.
- **`AddTaskWorker.kt`**:
    - Retrieve `hasTime` from `inputData`.
    - Pass `hasTime` to `tasksRepository.addTask`.
- **`TasksRepository.kt`**:
    - Update `addTask` and `editTask` signatures to include `hasTime: Boolean`.
    - Define a new `SimpleDateFormat` for ISO 8601 (e.g., `yyyy-MM-dd'T'HH:mm:ss.SSSXXX`).
    - In `prepareApiPropertiesToCreate` and `prepareApiPropertiesForEdit`:
        - Use the ISO 8601 formatter if `hasTime` is true.
        - Use the existing `dateFormat` (`yyyy-MM-dd`) if `hasTime` is false.

### 4. Mapper Updates (`TasksMapper.kt`)
- `NotionEntryDateMapper` already handles parsing ISO dates.
- We might need to ensure that the `Date` object returned by `map` is used correctly in the ViewModel to populate both `selectedDueDate` (the day) and `selectedDueTime` (the time).

## Verification Plan

### Automated Tests
- Update `TaskComposerViewModelTest` to verify time selection and clearing.
- Update `TasksRepositoryTest` (if it exists) or create a test to verify that `addTask`/`editTask` sends the correct string format to Notion based on the `hasTime` flag.
- Verify `NotionEntryDateMapper` handles various date/time formats from Notion.

### Manual Verification
- Create a new task with only a date: verify it appears correctly in Notion without time.
- Create a new task with date and time: verify it appears correctly in Notion with the exact time.
- Edit an existing task to add a time: verify the update.
- Edit an existing task to remove a time: verify the update.
- Verify the UI looks consistent with Material3 guidelines.
