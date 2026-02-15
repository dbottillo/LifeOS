# Project: LifeOS Android app

## What's this app

This is a personal side projects of mine. It's a personal app which is essentially a client of a specific Notion database where I store all my projects, tasks and general info.

The notion database has the following properties:

- Type: Task, Resource, Folder, Area, Bookmark, Goal
- Status: Backlog, Focus, Recurring, Archive, Done
- Parent item: a link to another database entry, this is for example how a task belongs to a folder
- Due: optional, to indicate when the entry is due
- URL: optional, an external URL
- Tag: optional, only used for Shopping entries
- Price: optional, only used for Shopping entries

## General Instructions

- The lack of modularisation is intentional, I'm the only one working on this app and I don't expect to grow it massively.
- Try to prompt and use the latest Android architecture guidelines.
- The app is distributed via an APK on Google Drive folder, no Play Store. That means that if there is an issue with the database I can just re-install it from scratch easily.
- Try to use clean architecture when possible: UI -> Use case/Repository -> Network/database.
- When asked to create a plan, do create a markdown file inside the gemini/ folder as the result.
- When creating or updating a plan, do not ask to implement it straight away. Just leave that to me in a follow up prompt.
