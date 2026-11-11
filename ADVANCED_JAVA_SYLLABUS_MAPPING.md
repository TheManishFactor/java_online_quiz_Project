# Advanced Java Syllabus Mapping

This document maps the five units of the Advanced Java course to the code that
actually implements them in the Online Quiz Application. Every row points to a file
that is compiled and used by the running application (or to a runnable demonstration
that accompanies it). Nothing below is a placeholder.

---

## Unit 1 — Java fundamentals, classes, inheritance and interfaces

### Concepts

- Packages, access modifiers, constructors, `this`/`super`
- Abstract classes, concrete subclasses, method overriding
- Interfaces, default methods, multiple interface implementation
- `static`, `final`, constants
- Encapsulation with private fields and public accessors
- The `Object` methods used by the UI (`toString`)

### Implementation

| Concept | Where |
| --- | --- |
| Abstract base class | `model/Person.java` |
| Abstract account class | `model/User.java` (extends `Person`) |
| Concrete roles | `model/Student.java`, `model/Admin.java` |
| Shared identity behaviour | `Person.getInitials()` reused by both roles |
| Interface with default method | `interfaces/Identifiable.java` (`isPersisted()` default) |
| Authentication contract | `interfaces/Authenticatable.java` |
| Service contracts | `interfaces/UserOperations.java`, `interfaces/QuizOperations.java` |
| Implementations of the contracts | `service/UserService.java`, `service/QuizService.java` |
| Packages and access control | `com.onlinequiz.*` packages; fields are private with getters/setters |
| Constructor chaining | `Student`/`Admin` call `User`, which calls `Person` |
| Constants | `PasswordHasher.ITERATIONS`, `NavigationManager.WINDOW_WIDTH`, `ScreenArea` audiences |
| Encapsulation example | `Quiz` keeps an internal `ArrayList<Question>` and exposes controlled accessors |

### Evidence

`ModelTest` checks role reporting, email normalization, initials, account creation,
quiz validation, question validation and result labels. `mvn test` runs it
automatically.

---

## Unit 2 — Polymorphism and exception handling

### Concepts

- Method overloading and overriding
- Upcasting and dynamic dispatch (`User` reference to `Student`/`Admin` object)
- `instanceof`-free role behaviour through `getRole()` and `isAdmin()`
- Checked vs unchecked exceptions
- `try`, multiple `catch`, nested `try`, `finally`, try-with-resources
- `throw`, `throws`, propagation, exception wrapping with a cause

### Implementation

| Concept | Where |
| --- | --- |
| Overriding | `Student.displayDashboard()`, `Admin.displayDashboard()`, `User.toString()` |
| Dynamic dispatch | `User` references returned by `UserService.loginUser()`; role-specific methods called through the base type |
| Overloading | Convenience constructors in `Admin`, `Student`, `UserService`, `UserRepository` and `QuizRepository` |
| Checked exceptions | `InvalidLoginException`, `QuizNotFoundException`, `QuizSubmissionException` |
| Unchecked exceptions | `ValidationException` (extends `IllegalArgumentException`), `InvalidQuestionException` |
| Custom hierarchy | `exception/` package |
| `throws` declarations | `UserService.loginUser`, `QuizService.getQuizById`, `Result.evaluate` |
| `try`/`catch`/`finally` | Controllers, `QuestionRepository` transactions, `QuizAttemptController` dialog handling |
| Nested try | `QuestionRepository` transaction rollback adds a suppressed rollback failure |
| try-with-resources | Every `Connection`, `PreparedStatement` and `ResultSet` in `repository/` and `database/` |
| Wrapping with cause | Service layer wraps `SQLException` in `IllegalStateException` |
| User-safe error handling | `util/ExceptionHandler.java` logs technical detail and returns a short message |

### Guided demonstrations

`src/test/java/com/onlinequiz/util/ExceptionDemo.java` runs these methods in order:

1. `demonstrateBasicTryCatch`
2. `demonstrateMultipleCatch`
3. `demonstrateNestedTry`
4. `demonstrateFinally`
5. `demonstrateThrow`
6. `demonstrateThrows`
7. `demonstrateBuiltInExceptions`
8. `demonstrateCustomExceptions`
9. `demonstratePropagation`
10. `demonstrateCheckedVsUnchecked`

### Evidence

`ExceptionTest` (18 checks) verifies valid paths, every custom exception and
propagation. `Phase5Test` verifies that login failures, missing quizzes, invalid
submissions and duplicate registrations produce the expected domain exceptions.

---

## Unit 3 — Multithreading, file I/O and event handling

### Concepts

- `Thread` subclassing and `Runnable`
- Thread lifecycle and priorities
- Daemon threads
- `volatile`, `synchronized`, `wait()`/`notifyAll()`
- Safe UI updates with `Platform.runLater`
- Byte streams and character encoding
- `IOException` handling and try-with-resources
- Delegation event model, listeners and adapter classes

### Implementation

| Concept | Where |
| --- | --- |
| `Runnable` + daemon thread | `threads/QuizTimerThread.java` |
| Timer state visibility | `volatile int remainingSeconds` |
| Timer shutdown | `stop()` interrupts the worker |
| UI-thread marshalling | `QuizAttemptController` uses `Platform.runLater` |
| Background work with `Callable` | `threads/BackgroundTask.java` |
| Background usage | Dashboards, quiz selection, history, management screens, analytics |
| Duplicate-submission guard | `controller/AttemptSubmission.java` (synchronized) |
| `Thread` subclass | `threads/ThreadDemo.java` (`demonstrateThreadSubclass`) |
| Priorities and lifecycle | `ThreadDemo.demonstrateRunnableAndPriorities`, `demonstrateLifecycle` |
| Synchronization | `ThreadDemo.demonstrateSynchronization` |
| `wait`/`notifyAll` | `ThreadDemo.demonstrateWaitNotify` |
| Byte-stream export | `io/ResultExporter.java` (`OutputStream`/`FileOutputStream`, UTF-8) |
| Byte-stream import | `io/ResultImporter.java` (`InputStream`/`FileInputStream`) |
| File I/O demonstration | `io/FileIODemo.java` |
| JavaFX events | Every controller/view; `QuizAttemptController` key shortcuts |
| AWT delegation model and adapters | `util/EventHandlingDemo.java` |

### Guided demonstrations

```bash
mvn clean test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

java -cp "target/classes;target/test-classes" com.onlinequiz.threads.ThreadDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.io.FileIODemo
java -cp "target/classes;target/test-classes" com.onlinequiz.util.EventHandlingDemo
```

`EventHandlingDemo` opens a small AWT window to show the event source, event object,
listener registration, `ActionListener`, `KeyListener`/`KeyAdapter`,
`MouseListener`/`MouseAdapter` and `WindowAdapter`. The main application itself is
JavaFX; the AWT demo exists only for syllabus demonstration.

### Evidence

`Phase5Test` checks timed expiry, `stop()` without expiry, successful and failed
background work, export content, and the absence of password hashes in exported
files.

---

## Unit 4 — JDBC, SQLite and the Collections Framework

### Concepts

- JDBC architecture: driver, connection, statement, result set
- `Statement` for DDL vs `PreparedStatement` for data
- CRUD operations
- Transactions, commit and rollback
- Referential integrity and cascading deletes
- Resource management with try-with-resources
- `List`, `Set` and `Map` interfaces and their implementations
- `Iterator`

### Implementation

| Concept | Where |
| --- | --- |
| Driver loading | `database/DatabaseConnection.java` |
| Connection management | `DatabaseConnection.getConnection(...)`, `PRAGMA foreign_keys = ON` |
| Schema creation and seeding | `database/DatabaseInitializer.java` |
| `Statement` DDL | `DatabaseInitializer.createTables`, `JDBCDemo` |
| `PreparedStatement` CRUD | All four repositories (results are insert/read only, by design) |
| `ResultSet` mapping | All four repositories |
| Transactions | `QuestionRepository` (insert question + update count), `DatabaseInitializer` (seed) |
| Rollback and suppressed errors | `QuestionRepository`, `DatabaseInitializer` |
| Cascade deletes | `questions.quiz_id`, `results.student_id`, `results.quiz_id` |
| `ArrayList` | `Quiz.questions`, repository result lists, `CollectionsDemo` |
| `LinkedList` | Question navigation demonstration in `CollectionsDemo` |
| `Vector` | Result demonstration in `CollectionsDemo` |
| `HashSet` | Active-session ids in `UserService`, demo |
| `TreeSet` | Sorted categories in `QuizService.getCategories`, demo |
| `HashMap` | Result grouping in `ResultService`, student maps in controllers |
| `Hashtable` | Quiz statistics demonstration in `CollectionsDemo` |
| `Iterator` | `ResultService` grouping and traversal, demo and test |

### Guided demonstrations

```bash
mvn clean test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

java -cp "target/classes;target/test-classes;$(cat target/classpath.txt)" com.onlinequiz.util.JDBCDemo
java -cp "target/classes;target/test-classes" com.onlinequiz.collections.CollectionsDemo
```

`JDBCDemo` uses `target/jdbc_demo.db` and its own demo table. It never touches the
application's `online_quiz.db`.

### Evidence

- `DatabaseTest` (22 checks): schema, idempotent seeding, foreign-key enforcement,
  CRUD, question-count updates, result lookups and cascades.
- `CollectionsTest` (8 checks): every collection type and `Iterator` removal.
- Read-only inspection of the application database: four tables, four indexes,
  consistent `total_questions`, zero foreign-key violations.

---

## Unit 5 — Generics and advanced GUI programming

### Concepts

- Generic classes, interfaces and methods
- One and multiple type parameters
- Bounded type parameters (`<T extends Identifiable>`)
- Wildcards and generic utility methods
- JavaFX application lifecycle, `Stage`, `Scene`
- Layout containers and controls
- CSS styling
- Dialogs, `FileChooser`, animations
- JavaFX properties and observable collections

### Implementation

| Concept | Where |
| --- | --- |
| Generic interface | `generics/GenericRepository.java` (`<T extends Identifiable>`) |
| Generic class with two parameters | `generics/GenericPair.java` (`<F, S>` and `swap()`) |
| Generic methods | `generics/GenericUtils.java` (`display`, `find`, `count`, bounded `max`) |
| Practical generic use | `QuizSelectionController` caches quizzes in `GenericRepository<Quiz>` |
| Practical generic utility | Admin dashboard counts students with `GenericUtils.count` |
| JavaFX lifecycle | `Main extends Application`, `start(Stage)` |
| Stage and scene | `navigation/NavigationManager` (title, size, minimum size, theme) |
| Layout containers | `BorderPane`, `VBox`, `HBox`, `StackPane`, `GridPane`, `FlowPane`, `ScrollPane`, `TabPane` |
| Controls | `TableView`, `ComboBox`, `RadioButton`, `ToggleGroup`, `ProgressBar`, `TextField`, `PasswordField`, `TextArea`, `Button` |
| Observable data | `FXCollections.observableArrayList`, `FilteredList` in student search |
| CSS theme | `resources/css/theme.css` applied by `NavigationManager` |
| Dialogs | `UiFactory.confirm`, `UiFactory.info`, `UiFactory.errorAlert` |
| File chooser | `HistoryController` export flow |
| Animations | Fade-in screen transition and hover lift in `AppShell`/`QuizCard` |
| Generic demo | `generics/GenericDemo.java` |

### Guided demonstration

```bash
mvn clean test-compile
java -cp "target/classes;target/test-classes" com.onlinequiz.generics.GenericDemo
```

### Evidence

`Phase5Test` checks the generic repository, the generic pair and all generic utility
methods. The real application was launched and all 14 screens were constructed and
navigated in a dedicated UI probe without uncaught exceptions.

---

## Summary table

| Unit | Main packages | Automated evidence |
| --- | --- | --- |
| 1. Fundamentals and inheritance | `model`, `interfaces` | `ModelTest` (37 checks) |
| 2. Polymorphism and exceptions | `model`, `exception`, `service`, `util` | `ExceptionTest` (18), `Phase5Test` |
| 3. Threads, I/O and events | `threads`, `io`, `controller`, `util` | `Phase5Test` (timer, background, export) |
| 4. JDBC and collections | `database`, `repository`, `collections`, `service` | `DatabaseTest` (22), `CollectionsTest` (8) |
| 5. Generics and GUI | `generics`, `view`, `navigation`, `resources` | `Phase5Test`, UI navigation probe |

Total automated checks executed by `mvn test`: **133**.
