# User Guide

This guide explains how to install, start and use the Online Quiz Application.

---

## 1. Before you start

Requirements:

- JDK 11 or newer (`java -version`)
- Apache Maven 3.6 or newer (`mvn -version`)
- A desktop session (the application uses a JavaFX window)

Installation:

```bash
# from the project folder
mvn clean package
```

This compiles the application, runs all 133 automated checks and creates
`target/OnlineQuizApplication.jar`. To start the application, use:

```bash
mvn javafx:run
```

The database file `online_quiz.db` is created automatically in the project folder
the first time the application starts. Keep this file to keep your data.

---

## 2. First run: create the administrator

The application ships without any user account and without any hardcoded
administrator password.

1. Start the application. The **Welcome** screen opens.
2. A link such as **Set up the first administrator** is shown while no administrator
   exists.
3. Enter the administrator's name, email and a password of at least eight
   characters, then confirm the password.
4. Select the setup action. The account is created, the password is hashed with
   PBKDF2-HMAC-SHA256, and you are taken to the administrator dashboard.
5. The setup link is no longer available after an administrator exists.

If the setup link is missing on first run, another administrator already exists in the
database being used.

---

## 3. Student workflow

### 3.1 Register

1. On the Welcome screen choose **Register**.
2. Enter your name, email, password and password confirmation.
3. The email is stored in lower case, must be unique and must look like an email
   address. The password must have at least eight characters and both entries must
   match.
4. Choose **Create account**. A duplicate email, a blank field or an invalid password
   shows an inline error and keeps you on the form.
5. After successful registration, sign in with the same email and password.

There is no student role selector: student accounts always receive the student role.

### 3.2 Sign in and dashboard

1. Choose **Login** on the Welcome screen.
2. Enter your email and password. Email comparison is case-insensitive.
3. A wrong password or unknown email produces a friendly error; no account details
   are revealed.
4. After login, the **Student dashboard** shows your attempts, average percentage,
   best score and passed count, with shortcuts to the quiz catalogue and history.

### 3.3 Take a quiz

1. Open **Quizzes** / **Quiz selection**.
2. Browse the quiz cards (each shows its category, duration and question count) and
   choose **Start quiz** on one.
3. The attempt screen shows one question at a time with four options:
   - click an option, or press `1`, `2`, `3`, `4`;
   - use **Previous** / **Next** or the Left/Right arrow keys;
   - the header shows the question number, the answered count and the countdown.
4. Your selected answers are kept when you move between questions.
5. Submit in one of two ways:
   - choose **Submit attempt** and confirm the dialog;
   - or let the countdown reach zero, which submits automatically.

At zero seconds the attempt is submitted even if the confirmation dialog is open. An
attempt can be saved only once; pressing Submit again or the timer expiring after a
save cannot create a duplicate result.

If time expires before you answer everything, the attempt is still saved with the
answers selected so far; unanswered questions count as incorrect.

### 3.4 Result and answer review

After submission the **Result** screen shows:

- score (for example `8 / 10`);
- percentage and grade;
- pass/fail (50% is the pass boundary);
- correct and incorrect counts;
- time used;
- the full answer review: your answer, the correct answer and whether each was right.

Use **Retake quiz** to start a new attempt or **Back to dashboard** to leave the
screen.

### 3.5 History and export

1. Open **History**.
2. The table lists every attempt with quiz title, score, percentage, grade, date and
   pass/fail.
3. Choose **Export results** and pick a destination in the file chooser.
4. The default name is `results_<id>_<name>_<timestamp>.txt` under `exports/`.
5. The report contains your name, email, role, attempts, average and the attempt
   list. It never contains a password or password hash.

If the file cannot be written, an error message explains the problem and no partial
report is presented as successful.

---

## 4. Administrator workflow

Sign in with the administrator account created during first-run setup. The
administrator shell contains only administrator screens; student screens are blocked
by the navigation policy.

### 4.1 Dashboard

The dashboard shows the number of students, quizzes, questions and attempts, with
shortcuts to management and analytics.

### 4.2 Quiz management

1. Open **Quiz management**.
2. Fill the form with title, category and duration in minutes (`duration >= 1`).
3. **Create** adds the quiz; select a row and **Update** saves edits; **Delete**
   removes the quiz after confirmation.
4. Deleting a quiz also deletes its questions and its results, as defined by the
   foreign keys. Use this deliberately.

### 4.3 Question management

1. Open **Question management** and select a quiz.
2. Enter the question text, four options and the correct option (A–D).
3. **Add** creates the question; selecting a question loads it into the form for
   **Update** or **Delete**.
4. The quiz's question count is updated in the same transaction as the change.
5. Blank text, blank options or an invalid correct-answer label are rejected with a
   message.

The editor panel scrolls so the action buttons remain reachable on smaller windows.

### 4.4 Student management

1. Open **Student management**.
2. The table lists student accounts only (administrators are excluded).
3. Type in the search box to filter by name or email.
4. Select a student to see attempt count, average, best score, passed count and the
   student's attempts.
5. The directory is read-only; it never displays password hashes.

### 4.5 Results and analytics

Open **Results & analytics** from the sidebar. The screen provides:

- **All results** — student, quiz, score, percentage, grade and date;
- **Student summary** — aggregates per student;
- **Quiz summary** — aggregates per quiz;
- **Pass / fail** — totals and pass rate.

Data loads in the background so the window stays responsive. If loading fails, an
inline message appears instead of silently showing zeroes.

---

## 5. Account and session

- The current user is held by the navigation manager for the whole session.
- **Logout** removes the active session and returns to the Welcome screen.
- **My profile** shows the signed-in user's name, email and role in a read-only
  dialog.
- A student can never navigate to an administrator screen, and an administrator
  cannot navigate to a student quiz attempt. A denied request is redirected to the
  correct dashboard.

---

## 6. Files and data

| File | Purpose |
| --- | --- |
| `online_quiz.db` | Your application data. Back it up; do not delete it unless you want a reset. |
| `exports/` | Default folder for student result reports. |
| `target/` | Build output and test databases. Safe to delete with `mvn clean`. |

To back up, close the application and copy `online_quiz.db`. To reset the application
completely, close it and move the file aside; a new empty database with sample quizzes
is created on the next start.

---

## 7. Demos for the course

The project also contains runnable demonstrations used in the report:

```bash
mvn clean test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

java -cp "target/classes;target/test-classes" com.onlinequiz.collections.CollectionsDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.threads.ThreadDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.io.FileIODemo
java -cp "target/classes;target/test-classes" com.onlinequiz.generics.GenericDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.util.EventHandlingDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.util.ExceptionDemo
java -cp "target/classes;target/test-classes;$(cat target/classpath.txt)" com.onlinequiz.util.JDBCDemo
```

On Linux/macOS use `:` instead of `;` in the classpath.

---

## 8. Troubleshooting

| Problem | What to do |
| --- | --- |
| `mvn javafx:run` fails because JavaFX is missing | Run `mvn clean package` first so Maven resolves JavaFX; confirm Maven uses JDK 11+. |
| The window does not open | Check that a desktop session is available; run from a terminal to see error output. |
| "Database could not be initialized" | Check that the project folder is writable and `online_quiz.db` is not locked by another process. |
| The setup link is missing | An administrator already exists. Sign in as that administrator. |
| "Email is already registered" | Use another email or sign in with the existing account. |
| Export fails | Choose a writable folder; close any program that has the target file open. |
| A quiz shows fewer questions than expected | The administrator may have deleted questions; check Question management. |
| The application shows an unexpected error | The full technical cause is written to the console; the UI intentionally shows only a short message. |
| Tests fail | Run `mvn clean test` and read the `[FAIL]` line; each suite prints the failing check. |

---

## 9. Quick reference

| Action | Where |
| --- | --- |
| Register | Welcome → Register |
| Sign in | Welcome → Login |
| First-run administrator | Welcome → Set up the first administrator |
| Take a quiz | Student dashboard → Quizzes → Start |
| Review a result | Automatic after submission |
| Export history | Student dashboard → History → Export results |
| Manage quizzes/questions/students | Administrator sidebar |
| Analyze results | Administrator sidebar → Results & analytics |
| Sign out | Sidebar → Logout |
