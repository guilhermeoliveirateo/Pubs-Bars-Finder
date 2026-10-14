# 🤖 AI Usage

This document describes how AI assistance was used during the development of Pubs & Bars Finder, what it produced, what I wrote myself, and how I checked and understood the results.

## Tool

**Claude** (Anthropic), used through a single ongoing conversation for the whole project. It acted mainly as a **tutor and reviewer**: explaining concepts, reviewing code I wrote, and helping plan the work. For some parts, it provided code or documents directly, which I then adapted, tested and integrated.

## How it was used

### 1. Planning and project organisation
- Breaking the assignment into GitHub **issues**, **milestones** and **labels**, and keeping a day-by-day schedule up to date.
- Defining the **Git workflow**: one branch per feature issue, pull requests linked with `Closes #N`, and Conventional Commits.
- Drafting issue descriptions, pull request descriptions and commit messages, which I reviewed before using.
- Discussing priorities and scope, for example which extra features fitted before the deadline.

### 2. Learning and explanations
I asked for explanations whenever something was new to me, before using it. Topics included:
- Kotlin: `val` vs `var`, immutability and `copy()`, null safety, collection functions (`find`, `filter`, `indexOfFirst`, `removeIf`, `joinToString`), enums, extension functions, `companion object`, `by lazy`.
- Android: activities and intents (explicit and implicit), ViewBinding, `onResume()`, options menus, `MaterialAlertDialogBuilder`, `RecyclerView` adapters and view holders, `ConstraintLayout` and `LinearLayout` weights, resources and night-mode colours.
- UML: relationship types (realization, association, dependency, composition) and multiplicities.

### 3. Code reviews
Most of the Kotlin code was written by me and then reviewed. The reviews pointed out bugs and improvements, which I fixed myself, for example:
- Errors that were shown but never cleared on the form.
- A delete action that also printed "not found" after a successful delete.
- Using the wrong ViewBinding class in a new activity.
- Missing `.show()` on a dialog and a missing MIME type on the share intent.
- An `update()` signature that had been changed by an IDE quick fix.

### 4. Code and resources provided by the AI
Some parts were provided more directly, as complete snippets or files, which I then adapted to my project, tested and committed:
- Gradle and version catalog changes for converting the project from Jetpack Compose to XML Views, and the converted `MainActivity`.
- A corrected version of my add/edit form layout, and later layout fixes (amenity columns, constraints).
- `colors.xml` with role-based names, and the dark mode colours in `res/values-night/colors.xml`.
- Helper code such as `showFieldError()`, the `labelRes()` mapping for amenities, the live validation helpers, and the multi-line description fix for the cards.
- The first drafts of README, the manual test plan, and the demo video script with sample data.

### 5. Design
- A mockup of the add/edit screen, which I used as the reference for the XML layout.
- Suggestions for colour naming, spacing and layout to fit the form on screen.

### 6. Debugging
Help diagnosing errors, unresolved resource references, unconstrained views, a branch missing merged work, and dark mode contrast problems.

## What I did myself

- **Wrote the core code**: `PubModel`, the `PubStore` interface, `PubMemStore`, the console menu (`Main.kt`), `PubData`, `loadPub()`, `savePub()`, the delete, search and share logic.
- **Built the layouts and resources**: the form, list item and main screen layouts, strings, drawables and menus, with reviews and corrections.
- **Made the decisions**: using XML Views instead of Compose, keeping the model immutable, adding the console menu, choosing the extra features (sharing, amenities, search, dark mode), and dropping or postponing others (venue type, amenity filter, rating and favourites, a custom action bar colour).
- **Tested everything**: I ran every feature on the emulator and went through the full manual test plan. Testing found real bugs, such as a validation error disappearing for an invalid value and multi-line descriptions displaying badly on the cards, which were then fixed.
- **Managed the repository**: issues, branches, commits, pull requests, merges, labels, milestones and projects.

## How I checked and understood the output

- I ran and tested every change before committing it, and used Logcat to confirm what the app was doing.
- When code was provided, I asked for an explanation of each part until I could understand and explain it myself, and changed it where needed. Some suggestions I chose not to follow, or reverted after trying them.

## What I learned

Using AI as a tutor helped me understand not just how to write the code, but why it is structured that way: programming to interfaces, keeping data immutable, separating the adapter from the screens through a listener interface, and naming resources by role so features like dark mode become simple. It also helped me organise the project professionally with issues, branches and pull requests. The responsibility for the final code, the decisions and the testing remained mine.