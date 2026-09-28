# 🍺 Pubs & Bars Finder

Pubs & Bars Finder is an Android app for discovering pubs and bars nearby, helping users find the best venues to gather and socialise. Users can save their favourite pubs, describe them, add photos and ratings, and see where they are on a map.

## ✨ Features

- Add a pub with a title and description
- List all added pubs
- Edit an existing pub
- Delete a pub
- Console (CLI) menu with full CRUD operations for testing the data layer

## 🛠️ Tech stack

- **Language:** Kotlin
- **Platform:** Android (minimum SDK 24)
- **IDE:** Android Studio
- **Maps:** Google Maps API

## 📁 Project structure

```
app/src/main/java/<package-name>/
├── models/       # Data model and data store
└── main/         # Console (CLI) runner
docs/             # Project documentation
```

This structure will grow as new features are added.

## 🏗️ Design

The data layer is designed around a `PubStore` interface, implemented by an in-memory store (`PubMemStore`). This allows a JSON-based store to replace it later without changing the rest of the app.

![UML class diagram](docs/uml-class-diagram.png)

## 🌿 Git workflow

- Work is planned and tracked with GitHub **issues**, grouped into **milestones**.
- Issues are categorised with **labels** according to the type of work.
- Each feature is developed on its own **branch**, linked to its issue, and merged into `main` through a **pull request**.
- Documentation and small setup changes are committed directly to `main`.
- Commit messages follow the [Conventional Commits](https://www.conventionalcommits.org/) style and reference the related issue.

## 🚀 How to run

1. Clone the repository:
   ```bash
   git clone https://github.com/guilhermeoliveirateo/Pubs-Bars-Finder
   ```
2. Open the project in Android Studio and let Gradle sync.
3. Create or start an Android virtual device (Tools → Device Manager).
4. Run the app with **Run ▶** (Shift + F10).