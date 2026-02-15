# Plan to Reorder Parent Items by Usage

This document outlines the plan to modify the `GetLocalNotionParentsUseCase` to sort parent items by their frequency of use, rather than alphabetically.

## 1. Analysis

I have investigated the codebase and found the following:

*   The database entity `NotionEntry` (`app/src/main/java/com/dbottillo/lifeos/db/NotionEntry.kt`) contains a `parentId` field, which links an entry to its parent.
*   The `GetLocalNotionParentsUseCase` (`app/src/main/java/com/dbottillo/lifeos/feature/composer/GetLocalNotionParentsUseCase.kt`) is the entry point for this logic.
*   The `NotionEntryDao` (`app/src/main/java/com/dbottillo/lifeos/db/NotionEntryDao.kt`) contains the relevant query method `searchParents(query: String)`.
*   The current query is:
    ```sql
    SELECT * FROM notionEntry WHERE type IN ('Folder', 'Area') AND title LIKE '%' || :query || '%' ORDER BY title ASC
    ```

## 2. Plan

### 2.1. Formulate a New SQL Query

I will replace the current query in `NotionEntryDao.searchParents` with a new one. The goal is to sort parents based on how many child entries they have.

**Proposed Query:**

```sql
SELECT N.* FROM notionEntry N
LEFT JOIN (
    SELECT parentId, COUNT(*) AS child_count
    FROM notionEntry
    WHERE parentId IS NOT NULL
    GROUP BY parentId
) AS C ON N.uid = C.parentId
WHERE N.type IN ('Folder', 'Area') AND N.title LIKE '%' || :query || '%'
ORDER BY C.child_count DESC, N.title ASC
```

**Query Explanation:**

1.  The inner query `(SELECT parentId, COUNT(*) ...)` calculates the number of children (`child_count`) for each `parentId`.
2.  A `LEFT JOIN` is used to join the `notionEntry` table (`N`) with the child count results (`C`). This ensures that parent entries with zero children are still included in the results.
3.  The `WHERE` clause remains the same, filtering for entries of type 'Folder' or 'Area' and matching the search query.
4.  The `ORDER BY` clause is updated to first sort by `child_count` in descending order (most used parents first) and then by `title` alphabetically as a secondary sorting criterion.

### 2.2. Update DAO and Use Case

*   I will update the `searchParents` method in `NotionEntryDao` to use this new query.
*   No changes are expected to be needed in the `GetLocalNotionParentsUseCase` or the `TasksRepository` as they just pass the data along.
