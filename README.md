# The List

A simple collaborative shopping list app.

https://github.com/user-attachments/assets/9a825a0e-a845-4da6-b649-d2af8f5ed07b

## Targets
- Android
- Web (WASM)
- Desktop (JVM)

## Features

- Multi users
- Real time (when user A adds or removes an item, user B sees the change immediately)
- Very simple and intuitive UI:
  - Items in the list at the top
  - Available items at bottom, most frequent first
- Search bar at the bottom, also used to create a new item

## Architecture

- Written
  in [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/get-started.html) / [Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/)
- Backend hosted on [Supabase](https://supabase.com/)
- Uses the Supabase SDK to access the Rest and realtime APIs
- Server component to generate item thumbnails (using OpenAI) with [Ktor](https://ktor.io/)
- Frontend: MVVM with ViewModels and Repositories
- [Coil](https://github.com/coil-kt/coil) for image loading
