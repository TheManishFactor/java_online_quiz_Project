# Database Design

The application uses SQLite through JDBC. The whole database is one file
(`online_quiz.db` in the working directory), created automatically on first start.
This document describes the schema, constraints, relationships, indexes,
transactions and the database files used for tests.

---

## 1. Entity–relationship overview

```text
┌──────────────┐            ┌──────────────┐            ┌──────────────┐
│    users     │            │   results    │            │   quizzes    │
├──────────────┤            ├──────────────┤            ├──────────────┤
│ id (PK)      │ 1        * │ id (PK)      │ *        1 │ id (PK)      │
│ name         │───────────<│ student_id FK│>───────────│ title        │
│ email UNIQUE │            │ quiz_id FK   │            │ category     │
│ password_hash│            │ score        │            │ duration     │
│ role CHECK   │            │ total_q      │            │ total_q      │
└──────────────┘            │ percentage   │            └──────┬───────┘
                            │ attempt_date │                   │ 1
                            └──────────────┘                   │
                                                               │ *
                                                        ┌──────┴───────┐
                                                        │  questions   │
                                                        ├──────────────┤
                                                        │ id (PK)      │
                                                        │ quiz_id FK   │
                                                        │ question_text│
                                                        │ option_a..d  │
                                                        │ correct CHECK│
                                                        └──────────────┘
```

- One user can have many results.
- One quiz can have many questions and many results.
- A question belongs to exactly one quiz.
- Deleting a user or a quiz cascades to the dependent results; deleting a quiz also
  cascades to its questions.

## 2. Tables

### 2.1 `users`

| Column | SQLite type | Constraints | Meaning |
| --- | --- | --- | --- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Account identifier |
| `name` | TEXT | NOT NULL | Display name |
| `email` | TEXT | NOT NULL, UNIQUE | Login identifier, stored lower case |
| `password_hash` | TEXT | NOT NULL | PBKDF2 string `pbkdf2$iterations$salt$hash` |
| `role` | TEXT | NOT NULL, CHECK IN ('STUDENT','ADMIN') | Account role |

The unique index on `email` is the database-level duplicate-email guard. The service
also checks for an existing email before insert so the user receives a friendly
message instead of a raw constraint error.

### 2.2 `quizzes`

| Column | SQLite type | Constraints | Meaning |
| --- | --- | --- | --- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Quiz identifier |
| `title` | TEXT | NOT NULL | Quiz title |
| `category` | TEXT | NOT NULL | Quiz category label |
| `duration` | INTEGER | NOT NULL, CHECK (duration >= 1) | Attempt duration in minutes |
| `total_questions` | INTEGER | NOT NULL DEFAULT 0, CHECK (total_questions >= 0) | Cached question count |

`total_questions` is maintained by the repository inside the same transaction that
adds or deletes a question. It is a deliberate denormalisation that keeps quiz cards
and listings fast without a `COUNT(*)` per row.

### 2.3 `questions`

| Column | SQLite type | Constraints | Meaning |
| --- | --- | --- | --- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Question identifier |
| `quiz_id` | INTEGER | NOT NULL, FK → `quizzes(id)` ON DELETE CASCADE | Parent quiz |
| `question_text` | TEXT | NOT NULL | Question prompt |
| `option_a` | TEXT | NOT NULL | Option A |
| `option_b` | TEXT | NOT NULL | Option B |
| `option_c` | TEXT | NOT NULL | Option C |
| `option_d` | TEXT | NOT NULL | Option D |
| `correct_answer` | TEXT | NOT NULL, CHECK IN ('A','B','C','D') | Correct option key |

### 2.4 `results`

| Column | SQLite type | Constraints | Meaning |
| --- | --- | --- | --- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Attempt identifier |
| `student_id` | INTEGER | NOT NULL, FK → `users(id)` ON DELETE CASCADE | Attempt owner |
| `quiz_id` | INTEGER | NOT NULL, FK → `quizzes(id)` ON DELETE CASCADE | Quiz attempted |
| `score` | INTEGER | NOT NULL, CHECK (score >= 0), CHECK (score <= total_questions) | Correct answers |
| `total_questions` | INTEGER | NOT NULL, CHECK (total_questions > 0) | Question count at attempt time |
| `percentage` | REAL | NOT NULL, CHECK (between 0 and 100) | Score percentage |
| `attempt_date` | TEXT | NOT NULL | Display timestamp (`yyyy-MM-dd HH:mm`) |

Results store the question total as it was at attempt time. Editing a quiz later does
not rewrite historical percentages.

There is intentionally no database uniqueness constraint that forbids multiple
attempts of the same quiz: a student may retake a quiz and each attempt is a separate
record. Prevention of a duplicate save for the *same* attempt is handled before the
insert by the synchronized `AttemptSubmission` object in the controller layer.

## 3. DDL

The schema is created by `database/DatabaseInitializer.java` with
`CREATE TABLE IF NOT EXISTS`, so every application start is safe:

```sql
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('STUDENT', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS quizzes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    duration INTEGER NOT NULL CHECK (duration >= 1),
    total_questions INTEGER NOT NULL DEFAULT 0
        CHECK (total_questions >= 0)
);

CREATE TABLE IF NOT EXISTS questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id INTEGER NOT NULL,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer TEXT NOT NULL
        CHECK (correct_answer IN ('A', 'B', 'C', 'D')),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    quiz_id INTEGER NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 0),
    total_questions INTEGER NOT NULL CHECK (total_questions > 0),
    percentage REAL NOT NULL
        CHECK (percentage >= 0 AND percentage <= 100),
    attempt_date TEXT NOT NULL,
    CHECK (score <= total_questions),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);
```

## 4. Indexes

| Index | Column | Reason |
| --- | --- | --- |
| `idx_questions_quiz_id` | `questions(quiz_id)` | Load all questions of a quiz |
| `idx_quizzes_category` | `quizzes(category)` | Supports category-based queries; the current catalogue derives categories in memory |
| `idx_results_student_id` | `results(student_id)` | Student history and dashboard statistics |
| `idx_results_quiz_id` | `results(quiz_id)` | Quiz summary and analytics |

The unique constraint on `users.email` creates an implicit unique index used by login
and duplicate-email checks.

## 5. Connections and integrity

`database/DatabaseConnection.java`:

- loads `org.sqlite.JDBC` explicitly and reports a clear `SQLException` if the driver
  is missing;
- resolves relative paths against the working directory and normalises separators;
- executes `PRAGMA foreign_keys = ON` on **every** connection, because SQLite
  disables foreign-key enforcement by default;
- sets `PRAGMA busy_timeout = 5000` so short lock contention waits instead of failing
  immediately;
- closes the connection if configuration fails.

All database objects are closed with try-with-resources.

## 6. Transactions

| Transaction | Statements | Behaviour on failure |
| --- | --- | --- |
| Seed sample data | Insert quiz, insert questions, update `total_questions` | `rollback()`, original error rethrown, rollback error added as suppressed |
| Add question | Insert question, update `quizzes.total_questions` | `rollback()` |
| Delete question | Delete question, update `quizzes.total_questions` | `rollback()` |

Result insertion is a single statement and therefore atomic by itself.

## 7. Seeding

Sample data is inserted only when `quizzes` is empty:

- **Java Basics** — Programming, 10 minutes, 3 questions
- **World Capitals** — General Knowledge, 8 minutes, 3 questions

The `total_questions` column is derived from the inserted rows, so the seed data is
self-consistent. Restarting the application does not duplicate the sample quizzes.
No sample user or administrator account is seeded. The first administrator is created
only through the first-run setup screen, and only while `users` contains no
administrator.

## 8. Database files and isolation

| File | Used by | Lifecycle |
| --- | --- | --- |
| `online_quiz.db` | The real desktop application | **User data. Never deleted by tests or build.** |
| `target/test-databases/online_quiz_test.db` | `DatabaseTest` | Deleted and recreated before each run |
| `target/test-databases/online_quiz_phase5.db` | `Phase5Test` | Deleted and recreated before each run |
| `target/test-databases/online_quiz_ui.db` | Temporary Phase 6 UI probe | Test-only file created under `target/` |
| `target/jdbc_demo.db` | `JDBCDemo` | Dedicated demo table, recreated by the demo |

`mvn clean` removes `target/`, so it deletes test databases and build output only. It
does not touch `online_quiz.db` in the project root.

Exports default to `exports/results_<id>_<name>_<timestamp>.txt` and contain no
password hashes.

## 9. Verification performed

The application database was inspected read-only (URL
`jdbc:sqlite:file:D:/java_endsem_project/online_quiz.db?mode=ro`) without writing to
it:

| Check | Result |
| --- | --- |
| Tables present | `users`, `quizzes`, `questions`, `results` |
| Indexes present | 4 (plus the unique email index) |
| Users | 0 (no administrator has been created in this copy) |
| Quizzes | 2 |
| Questions | 6 |
| Results | 0 |
| `quizzes.total_questions` vs actual question count | Consistent |
| Foreign keys | `questions.quiz_id`, `results.student_id`, `results.quiz_id` all `ON DELETE CASCADE` |
| `PRAGMA foreign_key_check` | 0 violations |

`DatabaseTest` additionally verifies idempotent initialization, duplicate-email
rejection, CRUD, counter updates, cascade deletes and persistence across separate
connections.

## 10. Backup and recovery

- Close the application before copying `online_quiz.db`.
- The database is a single file; copying it is a complete backup.
- There is no automatic backup or migration tool. If the schema changes in a future
  version, the file should be backed up before running the new build.
- Deleting the file resets the application to a fresh state. This is not done by the
  build or the automated tests and should only be performed deliberately.
