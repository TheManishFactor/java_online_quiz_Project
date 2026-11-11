# Online Quiz Application

A desktop quiz platform built with plain Java, JavaFX and SQLite for the Advanced Java
course. Students register, attempt timed multiple-choice quizzes and review their history;
administrators manage quizzes, questions, students and performance statistics.

## Documentation

| Document | Contents |
| --- | --- |
| `PROJECT_REPORT.md` | Full report: problem, objectives, architecture, modules, testing, conclusion |
| `ADVANCED_JAVA_SYLLABUS_MAPPING.md` | Every syllabus unit mapped to files, demos and tests |
| `DATABASE_DESIGN.md` | Schema, constraints, relationships, indexes, transactions, test isolation |
| `TESTING_REPORT.md` | Commands, 133 checks, bug list, UI/DB verification, limitations |
| `USER_GUIDE.md` | Student and administrator workflows, export and troubleshooting |
| `SCREENSHOT_CHECKLIST.md` | The 17 required submission screenshots and how to capture them |

## Technology stack

| Layer | Technology |
| --- | --- |
| Language | Java 11 |
| UI | JavaFX 17 (programmatic layouts, CSS theme) |
| Build | Apache Maven |
| Persistence | SQLite via JDBC (`PreparedStatement`) |
| Core Java | Collections Framework, generics, multithreading, file I/O, custom exceptions |

No Spring or other UI/persistence framework is used: the goal is to demonstrate the core
Advanced Java syllabus directly.

## Prerequisites

- JDK 11 or newer (`java -version`)
- Apache Maven 3.6+ (`mvn -version`)

JavaFX is pulled from Maven Central, so no separate JavaFX SDK installation is needed.

## Build and run

```bash
mvn clean compile          # compile
mvn clean test             # run all five self-checking suites (133 checks)
mvn javafx:run             # launch the JavaFX application
mvn clean package          # run tests, then build target/OnlineQuizApplication.jar
```

`mvn test` is a real build gate: the suites are forked JVMs, and a failed check exits
with status 1, which fails the build. A defect was deliberately injected once to
confirm this behaviour (see `TESTING_REPORT.md`).

### Model test

The Phase 2 model layer has a console test with self-checking assertions (also run
automatically by `mvn test`):

```bash
mvn clean test-compile
java -cp "target/classes;target/test-classes" com.onlinequiz.util.ModelTest
```

On Linux/macOS use `:` instead of `;` in the classpath.

### Exception test and demonstration

Phase 3 adds a self-checking test and a guided demonstration:

```bash
mvn clean test-compile
java -cp "target/classes;target/test-classes" com.onlinequiz.util.ExceptionTest
java -cp "target/classes;target/test-classes" com.onlinequiz.util.ExceptionDemo
```

`ExceptionTest` prints PASS/FAIL for each scenario and exits with status 1 when a check
fails. `ExceptionDemo` walks through every exception concept with explanatory output.

### Phase 4 database tests, JDBC demo and collections

The database tests and JDBC demo need the SQLite driver on the runtime classpath. Let
Maven write the dependency classpath once, then run the console classes:

```bash
mvn clean test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

java -cp "target/classes;target/test-classes;$(cat target/classpath.txt)" com.onlinequiz.util.DatabaseTest
java -cp "target/classes;target/test-classes;$(cat target/classpath.txt)" com.onlinequiz.util.JDBCDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.util.CollectionsTest
java -cp "target/classes;target/test-classes" com.onlinequiz.collections.CollectionsDemo
```

On Linux/macOS use `:` instead of `;` in the classpath.

### Phase 5 application tests and demos

`Phase5Test` checks registration validation, login, role-based navigation, quiz loading,
answer navigation, submission, the countdown timer, result calculation and persistence,
administrator CRUD, byte-stream export/import, generics, background tasks and logout.

```bash
mvn clean test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

java -cp "target/classes;target/test-classes;$(cat target/classpath.txt)" com.onlinequiz.util.Phase5Test

java -cp "target/classes;target/test-classes" com.onlinequiz.generics.GenericDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.threads.ThreadDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.io.FileIODemo
java -cp "target/classes;target/test-classes" com.onlinequiz.util.EventHandlingDemo
```

`Phase5Test` deletes and recreates `target/test-databases/online_quiz_phase5.db`, so it can
be run repeatedly. `EventHandlingDemo` opens a small AWT window to show the delegation
event model; the other demos are console-only.

### Launching the desktop application

```bash
mvn javafx:run
```

On the first run the database contains no administrator. The Welcome screen then shows
**Set up the first administrator**: choose a name, email and password of at least eight
characters. The password is hashed with PBKDF2-HMAC-SHA256 and stored through
`UserService`; the setup screen disappears and refuses to run again once an administrator
exists. No administrator password is hardcoded in the source.

Students can register from the Welcome screen. They cannot select the administrator role,
and the navigation manager blocks every administrator screen for a student (and every
student screen for an administrator).

### Database location

- Application database: `online_quiz.db` in the project working directory. It is created
  automatically when the JavaFX application starts.
- Database tests: `target/test-databases/online_quiz_test.db`. The file is deleted and
  recreated before every test run, so repeated runs never collide with old records.
- Phase 5 tests: `target/test-databases/online_quiz_phase5.db`, recreated on every run.
- Phase 5 exports: chosen in a file dialog, with the default location `exports/`.
- JDBC demonstration: `target/jdbc_demo.db`. It uses a dedicated demo table and never
  touches real quiz data.

Phase 4 did not seed an administrator account. Phase 5 adds the safe first-run bootstrap
described above: an administrator can only be created while none exists.

## Project layout

```text
java_endsem_project/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/onlinequiz/
    │   ├── Main.java              # JavaFX entry point (database + services + navigation)
    │   ├── model/                 # User, Student, Admin, Quiz, Question, Result
    │   ├── database/              # Connection manager and schema initializer
    │   ├── repository/            # JDBC CRUD repositories (PreparedStatement/ResultSet)
    │   ├── service/               # Registration, quiz and result business logic
    │   ├── collections/           # CollectionsDemo executable tutorial
    │   ├── controller/            # Screen controllers + testable attempt session/submission
    │   ├── view/                  # Programmatic JavaFX screens, shell and UI factory
    │   ├── navigation/            # Screen router, role policy and screen lifecycle
    │   ├── threads/               # Countdown timer, background tasks, ThreadDemo
    │   ├── io/                    # Byte-stream exporter, importer, FileIODemo
    │   ├── generics/              # Generic repository, pair, utilities, GenericDemo
    │   ├── interfaces/            # Service and repository contracts
    │   ├── exception/             # Custom exceptions
    │   └── util/                  # Hashing, validation, event handling demo, test entry points
    └── resources/
        └── css/theme.css          # Dark navy/blue-purple JavaFX theme
```

The JavaFX layer follows a strict direction:

```text
View  ->  Controller  ->  Service  ->  Repository  ->  JDBC  ->  SQLite
```

Controllers never contain SQL and never touch JDBC directly.

## Domain model

```
Person (abstract, shared identity behaviour)
└── User (abstract: id, name, email, passwordHash, role + dashboard contract)
    ├── Student (attempt statistics)
    └── Admin (admin level + quiz administration)

Quiz 1 --- * Question (stored in an ArrayList)
Result records one user's score for one quiz
```

## PHASE 3 – EXCEPTION HANDLING

An exception is an object that describes something going wrong while a program runs.
Instead of returning error codes, Java stops the current method and looks for a `catch`
block that can handle the problem. Phase 3 uses exceptions for genuine failure situations
only; ordinary decisions such as "is this answer correct?" stay as normal `if` logic.

### Checked versus unchecked

| Kind | Meaning | Used for | Examples |
| --- | --- | --- | --- |
| Checked | The compiler forces the caller to catch it or declare `throws` | Recoverable outcomes the caller must plan for | `InvalidLoginException`, `QuizNotFoundException`, `QuizSubmissionException` |
| Unchecked | Extends `RuntimeException`; no `throws` clause required | Programming errors and broken invariants that should fail fast | `ValidationException`, `InvalidQuestionException` |

`ValidationException` extends `IllegalArgumentException`, so existing defensive checks and
the Phase 2 `ModelTest` keep working while the application gains a precise domain type.

### Custom exception hierarchy

```
Exception (checked)
├── InvalidLoginException        wrong email or password
├── QuizNotFoundException        requested quiz is not available
└── QuizSubmissionException      quiz cannot be graded (missing/blank/malformed answers)

RuntimeException
└── IllegalArgumentException
    └── ValidationException      generic input rule violation
        └── InvalidQuestionException   blank question/option or answer outside A-D
```

### Core language features used

- **try-catch** wraps risky code and recovers without crashing.
- **Multiple catch blocks** handle different exception types separately.
- **Nested try** lets an inner block handle one failure while the outer block guards the rest.
- **finally** always runs, which is where cleanup belongs.
- **throw** raises an exception deliberately when a rule is violated.
- **throws** documents a checked exception in a method signature.
- **Propagation** lets an exception travel up the call stack until a caller handles it.
- **Exception wrapping** keeps the original exception as the `cause`, so technical detail is
  not lost while the user sees a short, safe message.
- **`ExceptionHandler`** separates the friendly user message from the technical log and never
  shows internal messages for unexpected errors.

### Concept-to-file mapping

| Concept | Where to look |
| --- | --- |
| Basic try-catch | `ExceptionDemo.demonstrateBasicTryCatch()` |
| Multiple catch blocks | `ExceptionDemo.demonstrateMultipleCatch()` |
| Nested try | `ExceptionDemo.demonstrateNestedTry()` |
| finally | `ExceptionDemo.demonstrateFinally()` |
| throw | `ExceptionDemo.demonstrateThrow()` |
| throws | `User.authenticate`, `Quiz.findById`, `Result.evaluate` |
| Built-in exceptions | `ExceptionDemo.demonstrateBuiltInExceptions()` |
| Five custom exceptions | `com.onlinequiz.exception` package |
| Exception propagation | `ExceptionDemo.demonstratePropagation()` |
| Checked vs unchecked | `ExceptionDemo.demonstrateCheckedVsUnchecked()` |
| Safe user messages / logging | `com.onlinequiz.util.ExceptionHandler` |
| Validation integration | `Validation`, `User`, `Student`, `Admin`, `Quiz`, `Question`, `Result` |
| Automated checks | `ExceptionTest` (18 checks) |

## PHASE 4 – JDBC, SQLITE AND COLLECTIONS

### JDBC in plain language

**What is JDBC?** JDBC (Java Database Connectivity) is the standard Java API for talking
to relational databases. The application calls JDBC interfaces, a driver translates those
calls into the database's own protocol, and the database executes the SQL. The application
therefore does not depend on SQLite-specific classes.

**What is SQLite?** SQLite is a small relational database stored in a single file. It needs
no server process, which keeps the project easy to run: `online_quiz.db` is the whole
database.

**What is a JDBC driver?** A driver is the library that implements the JDBC interfaces for
one database product. This project uses `org.xerial:sqlite-jdbc`, declared in `pom.xml`, and
`DatabaseConnection` loads `org.sqlite.JDBC` explicitly so a missing driver fails with a
clear message.

**What is `Connection`?** A `Connection` is an open session with the database. It is the
starting point for every statement and must be closed when work is finished.
`DatabaseConnection.getConnection()` returns one and enables SQLite foreign keys.

**What is `Statement`?** A `Statement` executes a fixed SQL string. It is suitable for DDL
such as `CREATE TABLE`, where no user values are involved.

**What is `PreparedStatement`?** A `PreparedStatement` is a precompiled SQL statement with
`?` placeholders. Values are bound with `setString`, `setInt`, and so on. All repository
queries and inserts use it.

**What is `ResultSet`?** A `ResultSet` is the cursor returned by a `SELECT`. `next()` moves
to the next row and typed getters such as `getInt("id")` read the columns.

**What are CRUD operations?** Create, Read, Update and Delete: `INSERT`, `SELECT`,
`UPDATE` and `DELETE`. Users, quizzes and questions support all four operations.
Results are immutable attempt records, so they support insert and read only.

**What is a database transaction?** A transaction groups several statements so that either
all of them are saved (`commit`) or none of them are (`rollback`). Adding a question and
updating `quizzes.total_questions` happen in one transaction; sample-data seeding also
uses one.

**Why use `PreparedStatement` instead of `Statement`?** Placeholders separate SQL code from
data, so a value such as `"x'; DROP TABLE users; --"` is treated as plain text rather than
SQL. That prevents SQL injection and also avoids manual quoting errors.

**Exception handling and closing resources:** every JDBC call can throw `SQLException`.
Repositories declare it, services translate it into a clear application failure, and all
`Connection`, `PreparedStatement` and `ResultSet` objects are closed with try-with-resources.

### Database schema

| Table | Important columns and constraints |
| --- | --- |
| `users` | `id` primary key, `email` unique and not null, `password_hash` not null, `role` restricted to STUDENT/ADMIN |
| `quizzes` | `id` primary key, `title`/`category` not null, `duration >= 1`, `total_questions >= 0` |
| `questions` | `id` primary key, `quiz_id` foreign key to `quizzes` with `ON DELETE CASCADE`, answer restricted to A-D |
| `results` | `id` primary key, foreign keys to `users` and `quizzes`, score/percentage checks, both parent deletes cascade |

Indexes cover `questions.quiz_id`, `quizzes.category`, `results.student_id` and
`results.quiz_id`. Two sample quizzes and their questions are inserted only when the
`quizzes` table is empty, so restarting the application never duplicates them.

Passwords are hashed with PBKDF2-HMAC-SHA256 (`PasswordHasher`). Each password gets a random
salt and 65,536 iterations; the stored value has the form
`pbkdf2$iterations$salt$hash`. Plain-text passwords are never stored.

### Collections in plain language

**What is the Collections Framework?** It is a set of ready-made data structures and the
interfaces they implement (`List`, `Set`, `Map`, `Iterator`). Choosing the right structure
makes code shorter and faster than hand-written arrays in most cases.

| Interface | Meaning | Main implementations in this project |
| --- | --- | --- |
| `List` | Ordered sequence, duplicates allowed, indexed access | `ArrayList`, `LinkedList`, `Vector` |
| `Set` | No duplicate elements | `HashSet` (unordered), `TreeSet` (sorted) |
| `Map` | Key-value pairs with unique keys | `HashMap` (unordered), `Hashtable` (synchronized) |

**ArrayList vs LinkedList:** `ArrayList` is backed by an array, so `get(index)` is fast and
iteration is cache-friendly; it is used for questions and result lists. `LinkedList` is a
chain of nodes, so adding/removing at the ends is cheap; it is demonstrated for next/previous
question navigation.

**Vector:** an older synchronized `List`. Individual operations are thread-safe, which costs
performance. It is shown in the educational demo with quiz results.

**HashSet vs TreeSet:** `HashSet` uses hashing for fast membership checks and no ordering;
it stores unique emails and active session ids. `TreeSet` keeps elements sorted; it produces
the alphabetically sorted category list.

**HashMap vs Hashtable:** both store key-value pairs. `HashMap` allows one null key and is
the normal choice; `ResultService` groups results by student id with it. `Hashtable` is the
older synchronized version and rejects null keys/values; it is demonstrated with quiz
statistics.

**What is Iterator?** `Iterator` is a cursor with `hasNext()` and `next()` that can traverse
any `Collection` without knowing its implementation. It also supports safe `remove()` during
iteration. `ResultService` uses it to group results, and the demo traverses questions.

### Concept-to-file mapping

| Concept | Java file |
| --- | --- |
| JDBC driver loading | `database/DatabaseConnection.java`, `util/JDBCDemo.java` |
| Connection management and foreign keys | `database/DatabaseConnection.java` |
| Schema, constraints, indexes, seed data | `database/DatabaseInitializer.java` |
| `Statement` for DDL | `DatabaseInitializer`, `JDBCDemo` |
| `PreparedStatement` inserts/updates/deletes | `repository/*.java`, `JDBCDemo` |
| `ResultSet` reads | `repository/*.java`, `JDBCDemo` |
| CRUD repositories | `repository/UserRepository`, `QuizRepository`, `QuestionRepository`, `ResultRepository` |
| Transactions (commit/rollback) | `QuestionRepository`, `DatabaseInitializer`, `JDBCDemo` |
| SQLException translation | service classes (`IllegalStateException` with cause) |
| Resource closing | try-with-resources throughout the database layer |
| Password hashing | `util/PasswordHasher.java`, `service/UserService.java` |
| ArrayList | `QuestionRepository`, `QuizRepository`, `ResultRepository`, `CollectionsDemo` |
| LinkedList | `collections/CollectionsDemo.java` (question navigation) |
| Vector | `collections/CollectionsDemo.java` (quiz results) |
| HashSet | `service/UserService.java` (active sessions), `CollectionsDemo` |
| TreeSet | `service/QuizService.java` (`getCategories`), `CollectionsDemo` |
| HashMap | `service/ResultService.java` (`groupResultsByStudent`), `CollectionsDemo` |
| Hashtable | `collections/CollectionsDemo.java` (quiz statistics) |
| Iterator | `service/ResultService.java`, `CollectionsDemo`, `CollectionsTest` |
| Executable tests | `util/DatabaseTest.java` (22 checks), `util/CollectionsTest.java` (8 checks) |

## PHASE 5 – JAVAFX GUI, MULTITHREADING, FILE HANDLING, EVENT HANDLING AND GENERICS

Phase 5 turns the console/database project into a complete desktop application. Every
button, form, navigation path, database operation, quiz attempt and result is wired to
the existing Phase 1–4 services; the model, repository and exception layers were not
rewritten.

### Screens

| Screen | Class | Audience |
| --- | --- | --- |
| Welcome | `view/WelcomeView` | Public |
| Registration | `view/RegisterView` | Public |
| Login | `view/LoginView` | Public |
| First-run administrator setup | `view/AdminSetupView` | Public (only while no admin exists) |
| Student dashboard | `view/StudentDashboardView` | Student |
| Quiz selection | `view/QuizSelectionView` | Student |
| Quiz attempt | `view/QuizAttemptView` | Student |
| Attempt result and review | `view/ResultView` | Student |
| Student history | `view/HistoryView` | Student |
| Administrator dashboard | `view/AdminDashboardView` | Administrator |
| Quiz management | `view/QuizManagementView` | Administrator |
| Question management | `view/QuestionManagementView` | Administrator |
| Student management | `view/StudentManagementView` | Administrator |
| Results and analytics | `view/AnalyticsView` | Administrator |

The screens use a shared `AppShell` sidebar (`view/AppShell`) with role-aware menu items,
a reusable `UiFactory` for controls and messages, and one dark navy/blue-purple theme
(`resources/css/theme.css`) with rounded cards, hover lift and fade-in transitions.

### Navigation and session security

`navigation/NavigationManager` owns the stage, scene, current user and current screen. It
does four things on every transition:

1. Checks `AccessPolicy.canAccess(user, area)` before showing a screen. A denied request
   is redirected to the correct dashboard, so a student can never reach an administrator
   screen (and the reverse).
2. Calls `ScreenLifecycle.onHide()` on the previous screen, which stops the attempt timer
   and prevents late background updates.
3. Replaces the scene root and applies the fade transition.
4. Keeps the authenticated `User` in one place. `logout()` removes the active session in
   `UserService` and returns to the Welcome screen.

### Multithreading

| Concept | Where | What it demonstrates |
| --- | --- | --- |
| `Runnable` + `Thread` | `threads/QuizTimerThread` | Daemon countdown thread; volatile remaining time; `stop()` interrupts the worker |
| Safe UI updates | `controller/QuizAttemptController` | Timer callbacks marshalled with `Platform.runLater` |
| Automatic submission | `QuizAttemptController` + `controller/AttemptSubmission` | Timer expiry submits exactly once |
| Background work | `threads/BackgroundTask` | `Callable` on a daemon worker; success/failure callbacks delivered through an executor (the UI uses `Platform::runLater`) |
| `Thread` subclass | `threads/ThreadDemo` | Overriding `run()` |
| Lifecycle and priorities | `threads/ThreadDemo` | NEW/RUNNABLE/TIMED_WAITING/TERMINATED and priority values |
| Synchronization | `threads/ThreadDemo` | Synchronized counter with four incrementing threads |
| Inter-thread communication | `threads/ThreadDemo` | Bounded box using `wait()`, `notifyAll()` |

Quiz data, dashboards, management tables, exports and analytics load through
`BackgroundTask`, so the JavaFX Application Thread is never blocked by SQLite.

### File handling (byte streams)

| Class | Stream usage |
| --- | --- |
| `io/ResultExporter` | `OutputStream` / `FileOutputStream`, UTF-8 bytes, try-with-resources |
| `io/ResultImporter` | `InputStream` / `FileInputStream` + `ByteArrayOutputStream` |
| `io/FileIODemo` | Create, write, read, close and `IOException` handling in one console demo |

The history screen exports the signed-in student's attempt report through a `FileChooser`
and shows the written path. Reports contain the student's name, email, role and quiz
performance only; password hashes are never written.

### Event handling

The JavaFX application uses the delegation model directly: buttons raise `ActionEvent`,
the attempt screen listens for `KeyEvent` (LEFT/RIGHT and 1–4), quiz cards use hover
`MouseEvent` transitions, and `QuizAttemptView` registers listeners on a single
`ToggleGroup`. `navigation/NavigationManager` handles the first-run setup link according to
the database state.

`util/EventHandlingDemo` is a separate AWT demonstration (the main UI stays JavaFX). It
shows the event source, event object, listener registration, `ActionListener`,
`KeyListener`/`KeyAdapter`, `MouseListener`/`MouseAdapter` and `WindowAdapter`, with
comments explaining each part.

### Generics

| Class | Coverage |
| --- | --- |
| `generics/GenericRepository<T extends Identifiable>` | Generic class with one bounded type parameter |
| `generics/GenericPair<F, S>` | Two independent type parameters and a type-swapping `swap()` |
| `generics/GenericUtils` | Generic methods `display`, `find`, `count` and bounded `max` |
| `generics/GenericDemo` | Executable demonstration of all of the above |

The application uses generics practically: the quiz selection controller caches loaded
quizzes in `GenericRepository<Quiz>`, the administrator dashboard counts students with
`GenericUtils.count`, and quiz lookups use `GenericUtils.find`.

### Validation and data safety

- Registration validates the name, normalizes the email, enforces an eight-character
  minimum password and requires a matching confirmation (`util/RegistrationValidator`).
- Passwords are hashed by `PasswordHasher` before reaching the repository; the repository
  also rejects plaintext values.
- Duplicate emails, missing quizzes/questions and invalid submissions produce precise
  custom exceptions and friendly messages.
- `AttemptSubmission` is synchronized and stateful, so the Submit button and timer expiry
  cannot save two results for one attempt.
- Administrator screens expose names, emails and performance only. Password hashes never
  reach a view or an exported report.

### Concept-to-file mapping

| Concept | Java file |
| --- | --- |
| Scene and stage management | `navigation/NavigationManager`, `Main` |
| Programmatic layouts | `view/*` (`BorderPane`, `VBox`, `HBox`, `StackPane`, `FlowPane`, `GridPane`, `ScrollPane`) |
| Controls | `TableView`, `TabPane`, `ComboBox`, `ProgressBar`, `RadioButton`, `ToggleGroup`, `TextArea`, `PasswordField`, `FileChooser` |
| CSS styling | `resources/css/theme.css`, `view/UiFactory` |
| Reusable cards, messages and dialogs | `view/UiFactory`, `view/StatCard`, `view/QuizCard` |
| Role-based routing | `navigation/AccessPolicy`, `navigation/ScreenArea` |
| Screen cleanup | `navigation/ScreenLifecycle` |
| Countdown and auto-submit | `threads/QuizTimerThread`, `controller/QuizAttemptController` |
| Background loading | `threads/BackgroundTask`, all dashboard/management controllers |
| Thread concepts demo | `threads/ThreadDemo` |
| Byte streams | `io/ResultExporter`, `io/ResultImporter`, `io/FileIODemo` |
| JavaFX events | all controllers/views; `QuizAttemptController` key shortcuts |
| AWT delegation model and adapters | `util/EventHandlingDemo` |
| Generic classes and methods | `generics/GenericRepository`, `GenericPair`, `GenericUtils` |
| Testable attempt state | `controller/QuizAttemptSession`, `controller/AttemptSubmission` |
| Phase 5 tests | `test/java/com/onlinequiz/util/Phase5Test.java` (48 checks) |

## Syllabus coverage

The application is built to exercise all five Advanced Java units:

1. **Fundamentals and inheritance** - abstract `User` hierarchy, interfaces, packages,
   access modifiers, constructors, `static`/`final`.
2. **Polymorphism and exceptions** - overloading/overriding, dynamic dispatch,
   try-catch-finally, nested try, custom checked exceptions.
3. **Multithreading, I/O and events** - `Thread`/`Runnable` countdown, synchronization,
   result export via byte streams, JavaFX and AWT event handling.
4. **JDBC and collections** - full CRUD on four tables, `ArrayList`, `LinkedList`,
   `Vector`, `HashSet`, `TreeSet`, `HashMap`, `Hashtable`, `Iterator`.
5. **Generics and advanced GUI** - generic repository/result types, layouts, controls,
   CSS styling and scene management.

## Build phases

| Phase | Scope | Status |
| --- | --- | --- |
| 1 | Maven project, dependencies, folder structure | Complete |
| 2 | Models, inheritance, interfaces | Complete |
| 3 | Exception handling | Complete |
| 4 | SQLite schema, JDBC CRUD and collections | Complete |
| 5 | JavaFX application, multithreading, file handling, event handling and generics | Complete |
| 6 | Full testing, bug fixing, UI polish, documentation and submission preparation | Complete |

Phase 5 absorbed the remaining items of the original outline: all screens, both
dashboards, administrator management, integration and the Phase 5 test suite are delivered
in this phase.

## PHASE 6 – TESTING, BUG FIXING, DOCUMENTATION AND SUBMISSION

Phase 6 added no new product features. It verified the Phase 1–5 implementation,
fixed defects, made the build test-gated and produced the submission documents.

### Final verification

| Item | Result |
| --- | --- |
| `mvn clean test` | 133 checks passed (37 model + 18 exception + 22 database + 8 collections + 48 Phase 5), `BUILD SUCCESS` |
| `mvn clean package` | Tests passed, `target/OnlineQuizApplication.jar` built, `BUILD SUCCESS` |
| JavaFX launch | `mvn javafx:run` opened the real application window at the Welcome screen |
| Screen navigation | 14 screens rendered and navigated against a test DB with no uncaught exceptions |
| Application database | Read-only inspection: 4 tables, 4 indexes, 2 quizzes, 6 questions, consistent counts, 0 foreign-key violations; user data not modified |

### Defects fixed

- Timer expiry could be ignored while the manual submit confirmation dialog was open.
- `dialogOpen` could stay stuck after a dialog failure.
- Analytics failures silently displayed zero statistics.
- Quiz/question create and update did not report database runtime failures.
- A swallowed administrator-check failure on the Welcome screen.
- Question Management editor clipped at the bottom.
- Authentication cards stretched to full window height.
- Result review could clip on shorter windows.
- Duplicate "Results" and "Analytics" administrator menu entries.
- Long quiz options did not wrap.
- `mvn test` previously executed zero checks.

All unexpected errors are logged through `ExceptionHandler`; no empty catch blocks were
added. Details and evidence are in `TESTING_REPORT.md`.

### Known limitations

- The jar is not a self-contained JavaFX runtime; use `mvn javafx:run`.
- Single-user desktop application backed by a local SQLite file.
- Multiple attempts per quiz are allowed and stored separately.
- No password reset, email verification or account lockout.
- UI verification is a screen-construction/navigation probe plus a real launch, not an
  automated click-through of every dialog and file chooser. See `TESTING_REPORT.md`
  for manual acceptance checks.
