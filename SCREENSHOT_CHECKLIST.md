# Screenshot Checklist

This checklist lists the screenshots required for the project submission. Capture
each one from the **real running application** or from a **real terminal**. Do not
create mock-ups, edit screenshots or present drawings as if they were the running
program.

Suggested workflow:

1. Run `mvn clean package` and then `mvn javafx:run`.
2. Use sample data only (for example "Asha Rao" / `asha@example.com`). Do not capture
   real personal data.
3. Capture with Windows **Win + Shift + S** (Snipping Tool) or another screenshot
   tool.
4. Save the files in a folder named `screenshots/` using the suggested file names.
5. Tick the checkbox only after the file has been captured and opened once to confirm
   it is readable.

Legend: **Student account** = a self-registered student. **Admin account** = the
account created through the first-run setup screen.

---

## 1. Welcome screen

- [ ] **File:** `01-welcome.png`
- **How:** Start the application with `mvn javafx:run` while signed out.
- **Must show:** Window title "Online Quiz Application", the Welcome heading, the
  Login and Register actions, and (if no administrator exists) the first-run setup
  link.
- **Tip:** Capture the full window, including the title bar.

## 2. Registration screen

- [ ] **File:** `02-registration.png`
- **How:** Welcome → Register.
- **Must show:** The registration card with name, email, password and confirmation
  fields and the create-account action.
- **Tip:** A screenshot with a partially filled form is fine; do not submit the form
  just for the screenshot.

## 3. Login screen

- [ ] **File:** `03-login.png`
- **How:** Welcome → Login.
- **Must show:** The login card with email and password fields and the sign-in action.

## 4. Student dashboard

- [ ] **File:** `04-student-dashboard.png`
- **How:** Sign in with the student account.
- **Must show:** The student sidebar/shell, welcome text for the student, and the
  personal statistic cards (attempts, average, best score, passed).
- **Tip:** Take an attempt first if you want non-zero statistics.

## 5. Quiz selection

- [ ] **File:** `05-quiz-selection.png`
- **How:** Student dashboard → Quizzes / Quiz selection.
- **Must show:** At least one quiz card with category badge, duration, question count
  and the **Start quiz** button.

## 6. Quiz attempt with timer

- [ ] **File:** `06-quiz-attempt-timer.png`
- **How:** Start a quiz and answer at least one question.
- **Must show:** The question number, the four options, the Previous/Next/Submit
  controls, the answered counter and a visible countdown (for example `09:41`).
- **Tip:** Include the whole attempt card so the timer is clearly readable.

## 7. Quiz result

- [ ] **File:** `07-quiz-result.png`
- **How:** Submit the attempt and confirm.
- **Must show:** Score, percentage, grade, pass/fail status, correct/incorrect counts
  and time used.

## 8. Answer review

- [ ] **File:** `08-answer-review.png`
- **How:** Scroll down on the result screen.
- **Must show:** The per-question review with the selected answer, the correct answer
  and the right/wrong indication.

## 9. Student history

- [ ] **File:** `09-student-history.png`
- **How:** Student dashboard → History.
- **Must show:** The attempt table with quiz, score, percentage, grade, result and
  date, plus the **Export results** action.
- **Tip:** If possible, also capture the exported `.txt` report in a text editor as a
  separate optional image; never show a password hash.

## 10. Administrator dashboard

- [ ] **File:** `10-admin-dashboard.png`
- **How:** Sign out, then sign in with the administrator account.
- **Must show:** The administrator sidebar and the summary counts for students,
  quizzes, questions and attempts.

## 11. Quiz management

- [ ] **File:** `11-quiz-management.png`
- **How:** Administrator sidebar → Quiz management.
- **Must show:** The quiz table, the create/edit form and the action buttons.

## 12. Question management

- [ ] **File:** `12-question-management.png`
- **How:** Administrator sidebar → Question management; select a quiz.
- **Must show:** The question table, the quiz selector and the editor with question
  text, options A–D and correct-answer control.
- **Tip:** Scroll the editor if needed so its action buttons are visible.

## 13. Student management

- [ ] **File:** `13-student-management.png`
- **How:** Administrator sidebar → Student management.
- **Must show:** The student table, the search field with a query, and the selected
  student's statistics/details.

## 14. Results and analytics

- [ ] **File:** `14-results-analytics.png`
- **How:** Administrator sidebar → Results & analytics.
- **Must show:** The tabs (All results, Student summary, Quiz summary, Pass/fail) and
  at least one populated table or the pass/fail summary.
- **Tip:** A second screenshot of the Pass/fail tab is optional.

## 15. SQLite tables

- [ ] **File:** `15-sqlite-tables.png`
- **How:** Open `online_quiz.db` in **DB Browser for SQLite** (or use the `sqlite3`
  command line) **after closing the application**. Show the schema and/or table data
  for `users`, `quizzes`, `questions` and `results`.
- **Must show:** The four table names and at least one table's columns/rows.
- **Safety:** Open the database in read-only mode if your tool offers it. Do not edit
  or delete rows for the screenshot. Do not show a real password value; show only the
  fact that `password_hash` contains a PBKDF2 string.

Useful read-only query:

```sql
SELECT name, type FROM sqlite_master
WHERE type IN ('table', 'index') AND name NOT LIKE 'sqlite_%'
ORDER BY type, name;
```

## 16. Successful Maven build

- [ ] **File:** `16-maven-build.png`
- **How:** In the project folder run:

```bash
mvn clean package
```

- **Must show:** The five `ALL ... CHECKS PASSED` lines, `Building jar`, and
  `BUILD SUCCESS`.
- **Tip:** Maximise the terminal and capture enough lines to make the result
  unambiguous.

## 17. Advanced Java demonstrations

- [ ] **File:** `17-advanced-java-demos.png` (or several images such as
  `17a-thread-demo.png`, `17b-collections-demo.png`, `17c-generics-demo.png`)
- **How:** Run two or more demos and capture their real terminal output:

```bash
mvn clean test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

java -cp "target/classes;target/test-classes" com.onlinequiz.collections.CollectionsDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.threads.ThreadDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.generics.GenericDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.io.FileIODemo
java -cp "target/classes;target/test-classes" com.onlinequiz.util.ExceptionDemo
java -cp "target/classes;target/test-classes;$(cat target/classpath.txt)" com.onlinequiz.util.JDBCDemo
```

- **Must show:** Real demonstration output, for example thread states and
  synchronization, collections operations, or generic type usage.
- **Tip:** `ThreadDemo` and `CollectionsDemo` together cover the most syllabus units.

---

## Final checks

- [ ] All 17 items above are captured from the real application or a real terminal.
- [ ] No screenshot contains a password, password hash, real email address or other
  personal data.
- [ ] Every screenshot is readable at 100% zoom and shows the window/terminal title.
- [ ] File names match the list, or an index file explains any differences.
- [ ] The screenshots are stored with the submission and were not edited to hide
  errors.

> The project does **not** ship prefilled screenshots. The images must be produced by
> running the application in the evaluation environment.
