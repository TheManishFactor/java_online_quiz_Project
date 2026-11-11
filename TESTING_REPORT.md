# Testing Report

**Project:** Online Quiz Application
**Date:** 2026-11-11
**Build tool:** Apache Maven 3.10.0
**Runtime:** Amazon Corretto Java 11.0.32.1 (x64, Windows)
**JavaFX:** 17.0.2
**SQLite JDBC:** 3.45.3.0

---

## 1. Test strategy

The project has no JUnit dependency. Instead, it uses five **self-checking console
programs** that print `[PASS]`/`[FAIL]` for every check and call `System.exit(1)` when
any check fails. `pom.xml` binds them to the Maven `test` phase through
`exec-maven-plugin`, each in a forked JVM. This means `mvn test` is a real gate: a
failed check fails the build.

Test databases are isolated under `target/test-databases/` and are recreated before
each run. The application database `online_quiz.db` is never used by the automated
tests.

Verification levels used in this phase:

| Level | Method |
| --- | --- |
| Business logic and persistence | Five automated console suites |
| Screen construction and navigation | Temporary JavaFX probe against a test DB |
| Real application launch | `mvn javafx:run` in a real desktop session |
| Visual quality | Snapshot inspection of all 14 screens |
| Database integrity | Read-only SQLite inspection and `PRAGMA foreign_key_check` |
| Build | `mvn clean test`, `mvn clean package` |

---

## 2. Commands and final results

### 2.1 Automated suites

```bash
mvn clean test
```

Final result:

```text
ALL MODEL CHECKS PASSED (37 checks)
ALL EXCEPTION CHECKS PASSED (18 checks)
ALL DATABASE CHECKS PASSED (22 checks)
ALL COLLECTION CHECKS PASSED (8 checks)
ALL PHASE 5 CHECKS PASSED (48 checks)
[INFO] BUILD SUCCESS
[INFO] Total time:  20.628 s
```

**Total: 133 checks, 0 failures.**

| Suite | Checks | What it covers |
| --- | --- | --- |
| `com.onlinequiz.util.ModelTest` | 37 | Constructors, inheritance, roles, initials, email normalization, quiz/question validation, score labels, grades, pass/fail, date formatting |
| `com.onlinequiz.util.ExceptionTest` | 18 | Valid paths, all five custom exceptions, propagation, checked vs unchecked |
| `com.onlinequiz.util.DatabaseTest` | 22 | Connection, schema, seeding, idempotency, FK enforcement, user/quiz/question/result CRUD, counter updates, cascades |
| `com.onlinequiz.util.CollectionsTest` | 8 | `ArrayList`, `LinkedList`, `Vector`, `HashSet`, `TreeSet`, `HashMap`, `Hashtable`, `Iterator` |
| `com.onlinequiz.util.Phase5Test` | 48 | Bootstrap, registration, hashing, login, role policy, quiz loading, navigation, submission, timer, grading, admin CRUD, export/import, generics, background tasks, logout |

### 2.2 Package build

```bash
mvn clean package
```

Final result:

```text
ALL MODEL CHECKS PASSED (37 checks)
ALL EXCEPTION CHECKS PASSED (18 checks)
ALL DATABASE CHECKS PASSED (22 checks)
ALL COLLECTION CHECKS PASSED (8 checks)
ALL PHASE 5 CHECKS PASSED (48 checks)
[INFO] Building jar: D:\java_endsem_project\target\OnlineQuizApplication.jar
[INFO] BUILD SUCCESS
[INFO] Total time:  20.049 s
```

The jar is 230 KB. It contains the compiled application and manifest main class.
JavaFX is resolved from Maven, so the supported launch command is:

```bash
mvn javafx:run
```

### 2.3 Maven failure mapping

To prove that `mvn test` really fails on a defect, a temporary failing check was
injected into `Phase5Test`. The output contained:

```text
[FAIL] temporary failure probe
1 OF 50 PHASE 5 CHECKS FAILED
[ERROR] BUILD FAILURE
```

The probe was removed afterwards and the suites returned to 133 passing checks.

### 2.4 Running a suite directly

```bash
mvn clean test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

java -cp "target/classes;target/test-classes;$(cat target/classpath.txt)" \
     com.onlinequiz.util.Phase5Test
```

On Linux/macOS replace `;` with `:`.

---

## 3. Functional coverage

### 3.1 Student workflow

| Requirement | How verified | Result |
| --- | --- | --- |
| Registration | `Phase5Test` section 2: valid input, persistence, duplicate rejection | Pass |
| Duplicate email prevention | Service check + `UNIQUE` constraint; automated check | Pass |
| Password hashing | Stored value starts with `pbkdf2$`; automated check | Pass |
| Login / logout | Correct credentials, wrong password, unknown email, case-insensitive email, logout removes session | Pass |
| Dashboard | Screen constructed in UI probe; statistics loaded via `BackgroundTask` | Pass (construction + data path) |
| Quiz listing | Seeded quizzes load with questions; categories sorted | Pass |
| Start quiz / answer selection | `QuizAttemptSession` answer preservation and bounds | Pass |
| Previous / next | Boundary checks for first/last question | Pass |
| Countdown timer | Timer counts down and raises expiry; `stop()` ends it | Pass |
| Automatic submission | Expiry grades and saves an empty attempt with score 0 | Pass |
| Manual submission | Completed attempt is graded and saved | Pass |
| Duplicate submission prevention | Second submit raises `QuizSubmissionException` | Pass |
| Score calculation | Perfect score, wrong answers, 50% pass boundary | Pass |
| Result saving | Result persisted and reloaded through service | Pass |
| Answer review | Result screen rendered in UI probe with answers | Pass (construction) |
| Attempt history | History screen rendered; rows built from results | Pass (construction + data path) |
| Result export | Text file written through byte streams; content checked; no password hash | Pass |

### 3.2 Administrator workflow

| Requirement | How verified | Result |
| --- | --- | --- |
| Administrator authentication | Bootstrap admin signs in; role navigation policy | Pass |
| Dashboard | Screen constructed; counts loaded through services | Pass (construction + data path) |
| Quiz create/edit/delete | Full round trip through `QuizService`; deleted quiz cannot be reloaded | Pass |
| Question add/edit/delete | Full round trip; missing quiz rejected | Pass |
| View students | Student directory loads users excluding admins | Pass (data path + screen) |
| Search students | `FilteredList` predicate on name/email; code review + screen render | Pass |
| View quiz results | Analytics tables built from `ResultService.getAllResults()` | Pass (data path + screen) |
| Analytics | All results, student summary, quiz summary, pass/fail tabs constructed | Pass (construction) |

### 3.3 Security

| Check | Result | Evidence |
| --- | --- | --- |
| Passwords are not stored in plain text | Pass | `Phase5Test` hash check; repository plaintext guard |
| All data SQL uses `PreparedStatement` | Pass | Repository review: only static DDL uses `Statement` |
| Role-based access enforced | Pass | Three `AccessPolicy` checks (anonymous, student, admin) |
| Session cleared on logout | Pass | `Phase5Test` logout check |
| Duplicate submission blocked | Pass | `AttemptSubmission` check |
| Password hashes never shown or exported | Pass | Export content check; view/controller inspection |
| No hardcoded administrator password | Pass | First-run setup only; no credentials in source |

### 3.4 Database

| Check | Result |
| --- | --- |
| Schema initialization and idempotency | Pass (`DatabaseTest`) |
| Primary keys and auto-increment ids | Pass |
| Unique email and check constraints | Pass |
| Foreign-key enforcement | Pass (`PRAGMA foreign_keys = ON` verified) |
| Question counter consistency | Pass |
| CRUD for users, quizzes and questions | Pass |
| Result insert and retrieval by id/student/quiz | Pass |
| Cascading deletes | Pass |
| Persistence across separate connections | Pass |
| `PRAGMA foreign_key_check` on application DB | 0 violations |
| Application DB content preserved | 2 quizzes, 6 questions, 0 users, 0 results; not modified |

---

## 4. UI verification

### 4.1 Screen navigation probe

A temporary JavaFX probe (removed after use) prepared a dedicated database at
`target/test-databases/online_quiz_ui.db`, created an administrator, a student and a
result, then constructed and displayed all 14 screens through `NavigationManager`.
It installed an uncaught-exception handler and captured a snapshot of each screen.

Result:

```text
14 screens rendered
UI SMOKE PASSED
no uncaught exceptions
```

Screens covered: Welcome, Administrator setup, Login, Registration, Student
dashboard, Quiz selection, Quiz attempt, Result, History, Admin dashboard, Quiz
management, Question management, Student management, Results and analytics.

Snapshot inspection found and led to fixes for:

- the Question Management editor being clipped at the bottom;
- authentication cards stretching to full window height;
- long quiz options not wrapping;
- the result/review screen clipping on shorter windows;
- duplicate "Results" and "Analytics" entries in the admin sidebar.

### 4.2 Real application launch

```bash
mvn javafx:run
```

The application was launched in a real desktop session on 2026-11-11. The window
titled **Online Quiz Application** opened at the Welcome screen with the dark theme,
the student entry points and the first-run administrator setup link. The process was
verified and then stopped. This confirms the documented launch command works with the
project's Maven configuration.

**Scope note:** the automated UI work verifies screen construction, navigation,
styling and data binding. Exhaustive click-through automation of every form, modal
dialog and file chooser is not part of the automated suite; those remain manual
acceptance checks (section 7).

---

## 5. Defects found and fixed in Phase 6

| # | Defect | Impact | Fix |
| --- | --- | --- | --- |
| 1 | `mvn clean test` executed zero checks (no JUnit tests) | The build could pass without testing anything | Added five forked `exec-maven-plugin` executions; verified failure mapping |
| 2 | Timer expiry did not auto-submit while the manual submit confirmation dialog was open | A student could lose an attempt at 0:00 | `dialogOpen` now guards only manual submission; automatic submission always proceeds |
| 3 | `dialogOpen` stayed `true` if the confirmation dialog threw | Subsequent manual submits were silently ignored | Dialog call wrapped in `try/finally` |
| 4 | Analytics load failure silently replaced statistics with zeros | Administrator saw misleading data | Inline error message; failure is logged |
| 5 | Unused `NavigationManager` parameter in `AnalyticsController` | Confusing constructor and dead dependency | Removed |
| 6 | Quiz create/update did not handle database runtime errors | Failure escaped to the JavaFX thread with no user feedback | Catch `RuntimeException`, log via `ExceptionHandler`, show a message |
| 7 | Question add/update had the same gap | Same | Same treatment |
| 8 | Welcome screen swallowed an administrator-check database error | Setup link could appear incorrectly with no trace | Failure is logged through `ExceptionHandler` |
| 9 | Question Management editor clipped at the bottom | "Save changes"/"Delete"/"Clear" partially unreachable | Editor form wrapped in a `ScrollPane` |
| 10 | Authentication cards stretched to full window height | Poor visual balance and excess empty space | `setMaxHeight(Region.USE_PREF_SIZE)` on four cards |
| 11 | Result/review screen could clip on shorter windows | Answer review cut off | Result content wrapped in a scroll pane |
| 12 | Duplicate admin sidebar entries ("Results" and "Analytics") | Two menu items opened the same screen | Merged into "Results & analytics" |
| 13 | Long quiz option text was truncated | Students could not read an option | Option labels wrap text |

No empty `catch` blocks were introduced. Unexpected failures are logged through
`ExceptionHandler` and reported to the user with a short message.

---

## 6. UI polish checklist

| Item | Status |
| --- | --- |
| Consistent dark navy/blue-purple theme across screens | Verified in snapshots |
| Consistent button styles (primary, ghost, danger) | Verified in snapshots and `UiFactory` |
| Readable text and sufficient contrast | Verified in snapshots |
| No overlapping or clipped controls at default size | Verified; editor clipping fixed |
| Minimum window size 1024×680; default 1180×760 | Enforced in `NavigationManager` |
| Meaningful success and error messages | Present on forms, management screens and analytics |
| No placeholder screens or non-functional buttons | All 14 screens wired to controllers/services |
| Scrollable content where lists/forms can grow | Result, history, question editor, management tables |

---

## 7. Manual acceptance checks (for the evaluator)

These interactions are intentionally manual; they require a person to operate the
desktop window:

1. Run `mvn javafx:run`; create the first administrator with a password of at least
   eight characters; confirm the setup link disappears after restart.
2. Register a new student and try a duplicate email; confirm the friendly error.
3. Sign in as the student; open a quiz; answer using mouse and keyboard (`1`–`4`,
   Left/Right); confirm the progress indicator and countdown.
4. Submit manually; confirm the score, grade, pass/fail and answer review.
5. Let another attempt expire; confirm it is saved once with the answers selected so
   far.
6. Open History and export; choose a location; confirm the report contains no
   password hash.
7. Sign out; sign in as the administrator; create/edit/delete a quiz and question;
   search a student; inspect all analytics tabs.
8. Attempt to navigate directly to a student screen while signed in as administrator
   (and the reverse); confirm the redirect.

---

## 8. Remaining limitations

1. No automated GUI interaction suite; the UI probe verifies construction and
   navigation, not every click path.
2. The package is not a self-contained JavaFX runtime; launch with `mvn javafx:run`.
3. SQLite is a single-file database intended for local desktop use, not concurrent
   multi-client access.
4. Multiple attempts per quiz are allowed and stored separately.
5. No password reset, email verification or account lockout.
6. The timer has one-second display resolution.
