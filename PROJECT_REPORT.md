# Online Quiz Application — Project Report

**Course:** Advanced Java
**Project:** Online Quiz Application (desktop)
**Technology:** Java 11, JavaFX 17, JDBC, SQLite, Maven
**Repository folder:** `java_endsem_project`
**Report date:** 2026-11-11

---

## 1. Introduction

The Online Quiz Application is a desktop program that lets students take timed
multiple-choice quizzes and lets an administrator manage the quiz catalogue, the
student directory and the resulting performance statistics. It is built with plain
Java and the standard Advanced Java APIs: JavaFX for the interface, JDBC with SQLite
for persistence, threads for the countdown timer and background loading, byte streams
for result export, and generics for reusable data structures.

The project deliberately avoids Spring, Hibernate, JPA and other frameworks. Every
database call is written with JDBC (`Connection`, `PreparedStatement`, `ResultSet`),
so the implementation demonstrates the syllabus directly instead of hiding it behind
an ORM. The application is self-contained: Maven downloads JavaFX and the SQLite
driver, and the whole database is a single file (`online_quiz.db`).

The work was delivered in six phases: project setup, domain model, exception
handling, JDBC/collections, the JavaFX application, and finally testing, bug fixing,
documentation and submission preparation.

## 2. Problem statement

Paper quizzes and spreadsheet-based marking are slow and error-prone. Teachers have
to distribute questions, collect answers, mark them by hand and keep records, while
students receive feedback late. A small desktop application can automate the whole
cycle:

- students register and sign in with a personal account;
- quizzes are answered under a visible countdown and submitted automatically when
  time runs out;
- scores, percentages and grades are calculated immediately and stored;
- students can review their answers and export their attempt history;
- administrators can maintain quizzes and questions and inspect class performance.

The problem addressed by this project is therefore: **provide a reliable, secure and
easy-to-use quiz workflow with persistent storage, immediate scoring and
role-separated administration, using only core and Advanced Java technologies.**

## 3. Objectives

1. Implement a complete student workflow: registration, login, quiz selection,
   timed attempt, automatic/manual submission, scoring, review, history and export.
2. Implement a complete administrator workflow: authentication, dashboard, quiz
   CRUD, question CRUD, student directory with search, results and analytics.
3. Persist all data in SQLite through JDBC with primary keys, foreign keys, checks,
   indexes and transactions.
4. Enforce security: PBKDF2 password hashing, `PreparedStatement` everywhere,
   role-based navigation and duplicate-submission prevention.
5. Demonstrate the five Advanced Java units with real, working code rather than
   isolated snippets.
6. Deliver a stable, documented and submission-ready desktop application with an
   automated, repeatable test suite.
7. Keep the UI consistent, readable and free of clipped or overlapping controls.

## 4. Technologies used

| Area | Technology | Version / notes |
| --- | --- | --- |
| Language | Java | 11 (`maven.compiler.release = 11`) |
| UI toolkit | JavaFX | 17.0.2 (`javafx-controls`) |
| Build tool | Apache Maven | 3.10.0 (3.6+ supported) |
| Database | SQLite | File database, no server |
| JDBC driver | `org.xerial:sqlite-jdbc` | 3.45.3.0 |
| Logging facade | SLF4J API + NOP binding | 2.0.13 |
| Styling | JavaFX CSS | `src/main/resources/css/theme.css` |
| Testing | Self-checking console suites | Executed by `mvn test` |

No external UI framework, ORM or dependency-injection container is used.

## 5. System architecture

The application follows a strict layered architecture:

```text
┌─────────────────────────────────────────────────────────────┐
│  View (JavaFX)      WelcomeView, LoginView, AppShell, ...   │
├─────────────────────────────────────────────────────────────┤
│  Controller         LoginController, QuizAttemptController, │
│                     AnalyticsController, ...                │
├─────────────────────────────────────────────────────────────┤
│  Service            UserService, QuizService,               │
│                     QuestionService, ResultService          │
├─────────────────────────────────────────────────────────────┤
│  Repository         UserRepository, QuizRepository,         │
│                     QuestionRepository, ResultRepository    │
├─────────────────────────────────────────────────────────────┤
│  JDBC               DatabaseConnection (PreparedStatement,  │
│                     ResultSet, transactions, foreign keys)  │
├─────────────────────────────────────────────────────────────┤
│  SQLite             online_quiz.db                          │
└─────────────────────────────────────────────────────────────┘
```

Rules enforced by the code:

- Views contain layout and control references only; they do not query the database.
- Controllers translate user actions into service calls and format the outcome for
  the view. They contain no SQL.
- Services apply business rules (validation, hashing, grading, duplicate protection)
  and translate `SQLException` into a clear application failure.
- Repositories own the SQL and use `PreparedStatement` exclusively.
- `DatabaseConnection` enables `PRAGMA foreign_keys = ON` on every connection.

Cross-cutting packages:

| Package | Responsibility |
| --- | --- |
| `navigation` | Scene/stage ownership, role policy, screen lifecycle, session |
| `threads` | Countdown timer, background tasks, threading demonstration |
| `io` | Byte-stream export/import and file-I/O demonstration |
| `generics` | Generic repository, pair, utilities and demonstration |
| `collections` | Collections Framework tutorial/demo |
| `exception` | Five domain exceptions |
| `util` | Hashing, validation, exception handling, event demo, test entry points |

## 6. Modules

### 6.1 Authentication and account module

- Student self-registration with name, email, password and confirmation.
- Email normalization to lower case and a uniqueness constraint in SQLite.
- Password policy of at least eight characters plus a matching confirmation.
- PBKDF2-HMAC-SHA256 hashing with a random 16-byte salt and 65,536 iterations.
- Safe first-run bootstrap: the administrator setup screen is available only while
  no administrator exists, and the password is chosen by the user. No administrator
  password is hardcoded.
- Login, logout, active-session tracking in `UserService`, and a read-only profile
  dialog.

### 6.2 Student module

- Dashboard with personal statistics (attempts, average, best score, passed count).
- Quiz catalogue cards with category badges and per-quiz question count/duration.
- Quiz attempt screen with one question at a time, selectable options, previous/next
  navigation and answered-count progress.
- Visible countdown supplied by a daemon timer thread.
- Automatic submission at zero seconds and manual submission with confirmation.
- Result screen with score, percentage, grade, pass/fail and answer review.
- History screen listing all attempts, with export of a report through `FileChooser`
  and byte streams.

### 6.3 Quiz and question management module

- Create, edit and delete quizzes (title, category, duration).
- Add, edit and delete questions (text, four options, correct answer A–D).
- Question count kept consistent through a transaction that updates
  `quizzes.total_questions`.
- Domain validation for blank text, blank options and an invalid correct-answer label.

### 6.4 Student directory module

- Read-only list of all student accounts (administrators excluded).
- Live search by name or email.
- Detail panel with per-student attempts, average, best score, passed count and
  attempt table.
- No password hashes are exposed anywhere in the UI.

### 6.5 Results and analytics module

- All results table with student, quiz, score, percentage, grade and date.
- Student summary and quiz summary tabs.
- Pass/fail totals and pass-rate panel.
- Background loading so the JavaFX Application Thread stays responsive.

### 6.6 Timer and background-work module

- `QuizTimerThread` implements `Runnable`, runs as a daemon, publishes remaining
  seconds through a volatile field and stops on interrupt.
- Timer callbacks are marshalled to the UI thread with `Platform.runLater`.
- `BackgroundTask<T>` runs a `Callable<T>` on a daemon worker and delivers success or
  failure through a caller-supplied UI executor.
- `AttemptSubmission` is synchronized and stateful, so the Submit button and timer
  expiry cannot save two results for the same attempt.

### 6.7 Data-access module

- Four repositories; users, quizzes and questions support create, read, update and
  delete. Results are immutable attempt records, so they support insert and read.
- Transactions for question insertion plus counter update, and for sample seeding.
- Schema creation and idempotent seeding on every application start.
- Dedicated databases for tests under `target/test-databases/`.

## 7. Database design

The schema has four tables:

```text
users (1) ─────< results >───── (1) quizzes
                    │
quizzes (1) ───────< questions
```

- `users.id` is the primary key; `email` is unique; `role` is checked to
  `STUDENT`/`ADMIN`.
- `quizzes.id` is the primary key; `duration >= 1`, `total_questions >= 0`.
- `questions.quiz_id` references `quizzes(id)` with `ON DELETE CASCADE`; the correct
  answer is checked to `A`–`D`.
- `results.student_id` and `results.quiz_id` reference their parents with
  `ON DELETE CASCADE`; checks enforce `score <= total_questions`,
  `percentage` between 0 and 100, and a positive total.

Indexes cover `questions.quiz_id`, `quizzes.category`, `results.student_id` and
`results.quiz_id`. Full column-level details are in `DATABASE_DESIGN.md`.

## 8. Screens and functionality

| # | Screen | Audience | Main functionality |
| --- | --- | --- | --- |
| 1 | Welcome | Public | Entry points to login/registration; first-run admin setup link |
| 2 | Registration | Public | Create a student account with validation |
| 3 | Login | Public | Email/password authentication, friendly errors |
| 4 | Administrator setup | Public (only when no admin exists) | Secure first administrator bootstrap |
| 5 | Student dashboard | Student | Personal statistics and shortcuts |
| 6 | Quiz selection | Student | Browse quizzes and start an attempt |
| 7 | Quiz attempt | Student | Questions, options, navigation, progress, countdown |
| 8 | Result | Student | Score, percentage, grade, pass/fail, answer review |
| 9 | History | Student | All attempts and text-report export |
| 10 | Admin dashboard | Administrator | Counts of students, quizzes, questions and attempts |
| 11 | Quiz management | Administrator | Create, edit and delete quizzes |
| 12 | Question management | Administrator | Create, edit and delete questions |
| 13 | Student management | Administrator | Search students and inspect performance |
| 14 | Results and analytics | Administrator | Result tables, summaries and pass rate |

The window opens at 1180×760 with a minimum of 1024×680. All screens share the dark
navy/blue-purple theme, the same button styles, card styling and message labels. The
result and question-editor areas are scrollable so content is not clipped on smaller
displays.

## 9. Advanced Java concepts demonstrated

| Unit | Concepts in the application |
| --- | --- |
| 1. Fundamentals and inheritance | Abstract `Person`/`User` hierarchy, `Student`/`Admin` subclasses, interfaces (`Identifiable`, `Authenticatable`, `QuizOperations`, `UserOperations`), packages, access modifiers, constructors, `static`/`final` |
| 2. Polymorphism and exceptions | Overloading and overriding, dynamic dispatch, checked vs unchecked exceptions, try-catch-finally, nested try, try-with-resources, custom exception hierarchy, wrapping with cause |
| 3. Multithreading, I/O and events | `Thread`/`Runnable` countdown, daemon threads, `volatile`, synchronization, `wait`/`notifyAll`, background tasks, byte streams, JavaFX and AWT event handling |
| 4. JDBC and collections | Driver loading, connections, `Statement` for DDL, `PreparedStatement` for all data, `ResultSet`, CRUD, transactions, `ArrayList`, `LinkedList`, `Vector`, `HashSet`, `TreeSet`, `HashMap`, `Hashtable`, `Iterator` |
| 5. Generics and advanced GUI | Generic classes/interfaces/methods with bounds, layouts, controls, CSS theme, `Scene`/`Stage`, `FileChooser`, dialogs, animations |

`ADVANCED_JAVA_SYLLABUS_MAPPING.md` maps every concept to concrete files and runnable
demonstrations.

## 10. Security

| Concern | Implementation |
| --- | --- |
| Password storage | PBKDF2-HMAC-SHA256, per-password salt, 65,536 iterations; repository rejects plaintext |
| SQL injection | Every value bound through `PreparedStatement`; only static DDL uses `Statement` |
| Role separation | `ScreenArea.Audience` + `AccessPolicy`; denied navigation redirects to the correct dashboard |
| Session handling | Authenticated user kept in `NavigationManager`; logout removes the active session |
| Duplicate submission | Synchronized, stateful `AttemptSubmission` shared by manual and automatic paths |
| Data exposure | Password hashes never appear in views, tables, dialogs or exports |
| Input validation | Model, service and registration validators reject invalid data with friendly messages |
| Administrator bootstrap | Available only while no administrator exists; password chosen securely at setup |

## 11. Testing

Automated checks are self-checking console programs executed by `mvn test`:

| Suite | Checks | Focus |
| --- | --- | --- |
| `ModelTest` | 37 | Model classes, inheritance, validation, result grading and labels |
| `ExceptionTest` | 18 | Custom exceptions, propagation, checked/unchecked behaviour |
| `DatabaseTest` | 22 | Schema, constraints, CRUD, transactions, cascade deletes |
| `CollectionsTest` | 8 | List, Set, Map, Iterator implementations used in the project |
| `Phase5Test` | 48 | Bootstrap, registration, login, role policy, quiz flow, timer, grading, admin CRUD, export, generics, background tasks, logout |
| **Total** | **133** | All pass |

Additional verification performed for this report:

- `mvn clean test` executed all five suites and finished with `BUILD SUCCESS`.
- Each suite was also run directly on the command line.
- A temporary JavaFX navigation probe opened all 14 screens against a dedicated test
  database, captured snapshots, and reported no uncaught exceptions. The probe was
  removed after verification.
- The real application was launched with `mvn javafx:run`; the Welcome window,
  first-run setup link and theme were verified in an actual desktop window.
- The application database was inspected read-only: four tables, four indexes, two
  seeded quizzes with six questions, consistent question counts, and zero
  `PRAGMA foreign_key_check` violations.

Details, commands and the bug list are in `TESTING_REPORT.md`.

## 12. Conclusion

The project delivers a complete desktop quiz workflow on top of plain Java, JavaFX,
JDBC and SQLite. Students can register, attempt timed quizzes, receive immediate
scores, review answers, inspect history and export reports. Administrators can manage
quizzes and questions, search the student directory and analyse results. Security and
data integrity are handled with hashed passwords, prepared statements, foreign keys,
checks, transactions and role-based navigation.

All five Advanced Java units are demonstrated by code that the application actually
uses, not by detached samples. The automated suites, the screen-navigation probe and a
real application launch provide repeatable evidence that the implemented features
work. The remaining limitations are listed below and do not prevent the project from
meeting its objectives.

## 13. Future scope

- Add a JUnit 5 test layer alongside the existing console suites for IDE integration.
- Add server-backed multi-user access instead of a single local SQLite file.
- Add password reset, email verification and account lockout after failed attempts.
- Add question images, rich text and per-question timers.
- Add configurable attempt limits and scheduled quiz windows.
- Add CSV/PDF export and printable reports.
- Add localization and accessibility improvements (screen-reader labels, high-contrast
  theme).
- Package a self-contained runtime image with `jlink`/`jpackage`.

## 14. Known limitations

1. The application is a single-user desktop client; SQLite is not a multi-client
   server database.
2. `mvn clean package` produces a jar with the correct main class, but JavaFX is not
   shaded into the jar. The supported launch command is `mvn javafx:run` (or running
   with the Maven-resolved classpath).
3. The countdown updates once per second; sub-second precision is not displayed.
4. Students may attempt a quiz more than once; every attempt is stored separately.
   Attempt limits are not enforced.
5. Administrators can view aggregate results and per-student performance but cannot
   open a student's individual quiz-attempt screen, by design.
6. Password reset, email verification and account management by students are not
   implemented.
7. Automated UI verification covers screen construction and navigation, and the real
   application was launched for visual checks. Exhaustive interaction testing of
   every control is not automated and remains a manual acceptance activity.
