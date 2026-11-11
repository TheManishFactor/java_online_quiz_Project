package com.onlinequiz.util;

import com.onlinequiz.controller.AttemptSubmission;
import com.onlinequiz.controller.QuizAttemptSession;
import com.onlinequiz.database.DatabaseInitializer;
import com.onlinequiz.exception.InvalidLoginException;
import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.QuizSubmissionException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.generics.GenericPair;
import com.onlinequiz.generics.GenericRepository;
import com.onlinequiz.generics.GenericUtils;
import com.onlinequiz.io.ResultExporter;
import com.onlinequiz.io.ResultImporter;
import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.Student;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.AccessPolicy;
import com.onlinequiz.navigation.ScreenArea;
import com.onlinequiz.service.QuestionService;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.service.UserService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.threads.QuizTimerThread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Phase 5 console verification: JavaFX-free logic behind the screens, plus the
 * threading, file and generics layers.
 *
 * Run with:
 * java -cp "target/classes;target/test-classes" com.onlinequiz.util.Phase5Test
 *
 * Every test uses its own SQLite file under target/test-databases so the real
 * application database is never touched.
 */
public final class Phase5Test {

    private static final String TEST_DATABASE =
            "target/test-databases/online_quiz_phase5.db";

    private static UserService userService;
    private static QuizService quizService;
    private static QuestionService questionService;
    private static ResultService resultService;

    private static User student;
    private static Quiz sampleQuiz;

    private static int checks;
    private static int failures;

    private Phase5Test() {
    }

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println(" PHASE 5 - APPLICATION LOGIC TESTS");
        System.out.println("=====================================================");

        try {
            prepareTestDatabase();
            buildServices();
            testAdminBootstrap();
            testRegistrationValidation();
            testLoginAndSessions();
            testAccessPolicy();
            testQuizLoading();
            testAnswerSelectionAndNavigation();
            testSubmissionAndPersistence();
            testCountdownTimer();
            testResultCalculation();
            testQuizAndQuestionCrud();
            testFileHandling();
            testGenerics();
            testBackgroundTask();
            testLogout();
        } catch (Throwable error) {
            failures++;
            System.out.println("[FAIL] Unexpected test failure: "
                    + error.getClass().getSimpleName() + ": " + error.getMessage());
            error.printStackTrace();
        }

        System.out.println();
        if (failures == 0) {
            System.out.println("ALL PHASE 5 CHECKS PASSED (" + checks + " checks)");
        } else {
            System.out.println(failures + " OF " + (checks + failures) + " PHASE 5 CHECKS FAILED");
            System.exit(1);
        }
    }

    private static void prepareTestDatabase() throws IOException, java.sql.SQLException {
        Files.createDirectories(Paths.get("target/test-databases"));
        for (String suffix : new String[]{"", "-journal", "-wal", "-shm"}) {
            Files.deleteIfExists(Paths.get(TEST_DATABASE + suffix));
        }
        DatabaseInitializer.initialize(TEST_DATABASE);
    }

    private static void buildServices() {
        userService = new UserService(TEST_DATABASE);
        quizService = new QuizService(TEST_DATABASE);
        questionService = new QuestionService(TEST_DATABASE);
        resultService = new ResultService(TEST_DATABASE);
    }

    private static void testAdminBootstrap() {
        section("1. First-run administrator bootstrap");

        check("Fresh database has no administrator", () -> require(!userService.hasAdmin(),
                "expected hasAdmin() to be false on a new database"));

        User admin = userService.createFirstAdmin("Root Admin", "root@example.com",
                "AdminPass123");

        check("Bootstrap creates an administrator", () -> {
            require(admin.getId() > 0, "admin id missing");
            require(admin.isAdmin(), "account is not an administrator");
        });
        check("Bootstrap password is hashed", () -> {
            require(admin.getPasswordHash().startsWith("pbkdf2$"),
                    "expected PBKDF2 hash, got " + admin.getPasswordHash());
            require(!admin.getPasswordHash().contains("AdminPass123"),
                    "plain password leaked into storage");
        });
        check("Bootstrap administrator can sign in", () -> {
            User signedIn = userService.loginUser("root@example.com", "AdminPass123");
            require(signedIn.isAdmin(), "login did not return the administrator");
            userService.logoutUser(signedIn.getId());
        });

        expectFailure(ValidationException.class,
                "Second bootstrap is refused once an admin exists",
                () -> userService.createFirstAdmin("Second Admin", "second@example.com",
                        "AnotherPass1"));
    }

    private static void testRegistrationValidation() {
        section("2. Registration screen validation");

        expectFailure(ValidationException.class, "Blank name is rejected",
                () -> RegistrationValidator.requireName("   "));
        expectFailure(ValidationException.class, "Invalid email is rejected",
                () -> RegistrationValidator.requireEmail("not-an-email"));
        expectFailure(ValidationException.class, "Short password is rejected",
                () -> RegistrationValidator.requirePassword("short"));
        expectFailure(ValidationException.class, "Mismatched confirmation is rejected",
                () -> RegistrationValidator.requireMatchingPasswords("Password1", "Password2"));

        check("Valid registration input is accepted", () -> {
            require("Asha Rao".equals(RegistrationValidator.requireName(" Asha Rao ")),
                    "name was not trimmed");
            require("asha@example.com".equals(
                            RegistrationValidator.requireEmail(" ASHA@Example.COM ")),
                    "email was not normalized");
        });

        check("Student registration persists the account", () -> {
            student = new Student("Asha Rao", "ASHA@Example.com", "StudentPass1");
            require(userService.registerUser(student), "registerUser returned false");
            student = userService.findUserByEmail("asha@example.com");
            require(student != null && student.getId() > 0, "student was not found");
            require(!student.isAdmin(), "self-registration produced an administrator");
            require("asha@example.com".equals(student.getEmail()),
                    "email was not normalized");
        });

        check("Registered password is stored only as a PBKDF2 hash", () -> {
            require(student.getPasswordHash().startsWith("pbkdf2$"),
                    "expected PBKDF2 hash, got " + student.getPasswordHash());
            require(!student.getPasswordHash().contains("StudentPass1"),
                    "plain password leaked into storage");
        });

        expectFailure(ValidationException.class, "Duplicate email is rejected",
                () -> userService.registerUser(
                        new Student("Copy Cat", "asha@example.com", "StudentPass1")));
    }

    private static void testLoginAndSessions() {
        section("3. Login success and failure");

        check("Correct credentials sign in", () -> {
            User signedIn = userService.loginUser("asha@example.com", "StudentPass1");
            require(!signedIn.isAdmin(), "student signed in as administrator");
            require(userService.getActiveSessionIds().contains(signedIn.getId()),
                    "session was not recorded");
            userService.logoutUser(signedIn.getId());
        });
        check("Email comparison is case-insensitive", () -> {
            User signedIn = userService.loginUser("ASHA@EXAMPLE.COM", "StudentPass1");
            require(signedIn.getId() == student.getId(), "wrong account signed in");
            userService.logoutUser(signedIn.getId());
        });

        expectFailure(InvalidLoginException.class, "Wrong password is rejected",
                () -> userService.loginUser("asha@example.com", "WrongPass1"));
        expectFailure(InvalidLoginException.class, "Unknown email is rejected",
                () -> userService.loginUser("nobody@example.com", "StudentPass1"));
    }

    private static void testAccessPolicy() {
        section("4. Role-based navigation policy");

        User anonymous = null;
        User admin = userService.findUserByEmail("root@example.com");

        check("Anonymous visitors reach only public screens", () -> {
            require(AccessPolicy.canAccess(anonymous, ScreenArea.WELCOME), "welcome blocked");
            require(AccessPolicy.canAccess(anonymous, ScreenArea.LOGIN), "login blocked");
            require(AccessPolicy.canAccess(anonymous, ScreenArea.REGISTER), "register blocked");
            require(!AccessPolicy.canAccess(anonymous, ScreenArea.STUDENT_DASHBOARD),
                    "anonymous reached student dashboard");
            require(!AccessPolicy.canAccess(anonymous, ScreenArea.ADMIN_DASHBOARD),
                    "anonymous reached admin dashboard");
        });

        check("Students are confined to student screens", () -> {
            require(AccessPolicy.canAccess(student, ScreenArea.STUDENT_DASHBOARD),
                    "student dashboard blocked");
            require(AccessPolicy.canAccess(student, ScreenArea.QUIZ_SELECTION),
                    "quiz selection blocked");
            require(AccessPolicy.canAccess(student, ScreenArea.QUIZ_ATTEMPT),
                    "quiz attempt blocked");
            require(AccessPolicy.canAccess(student, ScreenArea.RESULT), "result blocked");
            require(AccessPolicy.canAccess(student, ScreenArea.HISTORY), "history blocked");
            require(!AccessPolicy.canAccess(student, ScreenArea.ADMIN_DASHBOARD),
                    "student reached admin dashboard");
            require(!AccessPolicy.canAccess(student, ScreenArea.ANALYTICS),
                    "student reached analytics");
            require(!AccessPolicy.canAccess(student, ScreenArea.QUIZ_MANAGEMENT),
                    "student reached quiz management");
        });

        check("Administrators are confined to admin screens", () -> {
            require(AccessPolicy.canAccess(admin, ScreenArea.ADMIN_DASHBOARD),
                    "admin dashboard blocked");
            require(AccessPolicy.canAccess(admin, ScreenArea.QUIZ_MANAGEMENT),
                    "quiz management blocked");
            require(AccessPolicy.canAccess(admin, ScreenArea.QUESTION_MANAGEMENT),
                    "question management blocked");
            require(AccessPolicy.canAccess(admin, ScreenArea.STUDENT_MANAGEMENT),
                    "student management blocked");
            require(AccessPolicy.canAccess(admin, ScreenArea.ANALYTICS), "analytics blocked");
            require(!AccessPolicy.canAccess(admin, ScreenArea.STUDENT_DASHBOARD),
                    "admin reached student dashboard");
            require(!AccessPolicy.canAccess(admin, ScreenArea.QUIZ_ATTEMPT),
                    "admin reached a student quiz attempt");
        });
    }

    private static void testQuizLoading() {
        section("5. Quiz loading through the service layer");

        check("Seeded quizzes load with their questions", () -> {
            List<Quiz> quizzes = quizService.getAllQuizzes();
            require(quizzes.size() >= 2, "expected at least 2 seeded quizzes");
            sampleQuiz = quizzes.get(0);
            Quiz loaded = quizService.getQuizById(sampleQuiz.getId());
            require(!loaded.getQuestions().isEmpty(), "quiz has no questions");
            require(loaded.getTotalQuestions() == loaded.getQuestions().size(),
                    "question count is out of sync");
            require(loaded.getDuration() > 0, "quiz duration missing");
        });

        check("Categories are unique and sorted", () -> {
            List<String> categories = new ArrayList<>(quizService.getCategories());
            require(!categories.isEmpty(), "no categories returned");
            List<String> sorted = new ArrayList<>(categories);
            sorted.sort(String::compareTo);
            require(categories.equals(sorted), "categories are not sorted");
            require(new java.util.HashSet<>(categories).size() == categories.size(),
                    "categories contain duplicates");
        });

        expectFailure(QuizNotFoundException.class, "Unknown quiz id is reported",
                () -> quizService.getQuizById(999_999));
    }

    private static void testAnswerSelectionAndNavigation() {
        section("6. Answer selection and previous/next navigation");

        check("Answers are preserved while moving between questions", () -> {
            Quiz quiz = quizService.getQuizById(sampleQuiz.getId());
            QuizAttemptSession session = new QuizAttemptSession(quiz);

            require(!session.canGoPrevious(), "first question allows Previous");
            require(session.canGoNext(), "first question blocks Next");
            require(session.getAnsweredCount() == 0, "new attempt already answered");

            session.selectAnswer("b");
            require("B".equals(session.getCurrentAnswer()), "answer was not normalized");

            session.next();
            require(session.getCurrentIndex() == 1, "Next did not advance");
            session.selectAnswer("C");

            session.previous();
            require(session.getCurrentIndex() == 0, "Previous did not go back");
            require("B".equals(session.getCurrentAnswer()),
                    "first answer was lost on navigation");
            require(session.getAnsweredCount() == 2, "answered count is wrong");

            String[] copy = session.copyAnswers();
            copy[0] = "D";
            require("B".equals(session.getSelectedAnswer(0)),
                    "copyAnswers exposed internal state");
        });

        check("Navigation respects the quiz boundaries", () -> {
            Quiz quiz = quizService.getQuizById(sampleQuiz.getId());
            QuizAttemptSession session = new QuizAttemptSession(quiz);
            session.previous();
            require(session.getCurrentIndex() == 0, "Previous moved before the first question");
            for (int index = 0; index < quiz.getQuestions().size(); index++) {
                session.next();
            }
            require(session.getCurrentIndex() == quiz.getQuestions().size() - 1,
                    "Next moved past the last question");
        });

        expectFailure(ValidationException.class, "Invalid option label is rejected",
                () -> new QuizAttemptSession(quizService.getQuizById(sampleQuiz.getId()))
                        .selectAnswer("Z"));
    }

    private static void testSubmissionAndPersistence() throws QuizNotFoundException {
        section("7. Submission, duplicate protection and persistence");

        Quiz quiz = quizService.getQuizById(sampleQuiz.getId());
        String[] correct = correctAnswers(quiz);
        AttemptSubmission submission = new AttemptSubmission();

        check("A completed attempt is graded and saved", () -> {
            Result saved = submission.submit(student.getId(), quiz, correct,
                    "2026-11-11 10:00", false, resultService);
            require(saved.getId() > 0, "saved result has no id");
            require(saved.getScore() == quiz.getQuestions().size(),
                    "perfect answers did not score full marks");
            require(saved.getPercentage() == 100.0, "percentage is not 100");
            require(submission.isSubmitted(), "submission flag not set");
            require(resultService.getResultsByStudentId(student.getId()).size() == 1,
                    "result was not persisted");
        });

        expectFailure(QuizSubmissionException.class, "A second submit is blocked", () ->
                submission.submit(student.getId(), quiz, correct,
                        "2026-11-11 10:01", false, resultService));

        expectFailure(QuizSubmissionException.class,
                "A manual submission with no answers is refused",
                () -> new AttemptSubmission().submit(student.getId(), quiz,
                        new String[quiz.getQuestions().size()],
                        "2026-11-11 10:02", false, resultService));

        check("Timer expiry saves an empty attempt with score 0", () -> {
            AttemptSubmission automatic = new AttemptSubmission();
            Result saved = automatic.submit(student.getId(), quiz,
                    new String[quiz.getQuestions().size()],
                    "2026-11-11 10:05", true, resultService);
            require(saved.getScore() == 0, "blank attempt was not scored zero");
            require(saved.getPercentage() == 0.0, "blank attempt percentage is not zero");
            require(resultService.getResultsByStudentId(student.getId()).size() == 2,
                    "automatic result was not persisted");
        });
    }

    private static void testCountdownTimer() throws InterruptedException {
        section("8. Countdown timer thread and automatic expiry");

        CountDownLatch expired = new CountDownLatch(1);
        AtomicInteger ticks = new AtomicInteger();
        AtomicInteger lastTick = new AtomicInteger(-1);

        QuizTimerThread timer = new QuizTimerThread(2, new QuizTimerThread.Listener() {
            @Override
            public void onTick(int secondsRemaining) {
                ticks.incrementAndGet();
                lastTick.set(secondsRemaining);
            }

            @Override
            public void onTimeExpired() {
                expired.countDown();
            }
        });

        check("Timer counts down and raises expiry", () -> {
            timer.start();
            require(expired.await(6, TimeUnit.SECONDS), "timer never expired");
            require(lastTick.get() == 0, "last tick was not zero");
            require(ticks.get() >= 3, "expected at least three tick callbacks");
            require(!timer.isRunning(), "timer still running after expiry");
            require(timer.getTotalSeconds() == 2, "total seconds changed");
        });

        check("stop() ends the countdown without expiry", () -> {
            CountDownLatch firstTick = new CountDownLatch(1);
            AtomicBoolean expiredAfterStop = new AtomicBoolean(false);
            QuizTimerThread stoppable = new QuizTimerThread(30,
                    new QuizTimerThread.Listener() {
                        @Override
                        public void onTick(int secondsRemaining) {
                            firstTick.countDown();
                        }

                        @Override
                        public void onTimeExpired() {
                            expiredAfterStop.set(true);
                        }
                    });
            stoppable.start();
            require(firstTick.await(3, TimeUnit.SECONDS), "timer never produced a tick");
            stoppable.stop();
            require(!stoppable.isRunning(), "stop() did not clear the running flag");
            Thread.sleep(150);
            require(!expiredAfterStop.get(), "stopped timer still expired");
        });
    }

    private static void testResultCalculation() throws QuizNotFoundException {
        section("9. Result calculation and grading");

        Quiz quiz = quizService.getQuizById(sampleQuiz.getId());

        check("Perfect answers produce grade A+ and a pass", () -> {
            Result result = Result.evaluate(student.getId(), quiz,
                    correctAnswers(quiz), "2026-11-11 11:00");
            require(result.getScore() == quiz.getQuestions().size(), "wrong score");
            require(result.getPercentage() == 100.0, "wrong percentage");
            require(result.isPassed(), "perfect score did not pass");
            require("A+".equals(result.getGrade()), "perfect score is not grade A+");
        });

        check("Wrong answers are counted correctly", () -> {
            String[] answers = new String[quiz.getQuestions().size()];
            Arrays.fill(answers, "A");
            int expected = 0;
            for (Question question : quiz.getQuestions()) {
                if (question.isCorrect("A")) {
                    expected++;
                }
            }
            Result result = Result.evaluate(student.getId(), quiz, answers,
                    "2026-11-11 11:05");
            require(result.getScore() == expected, "wrong-answer count is incorrect");
            require(result.getTotalQuestions() == quiz.getQuestions().size(),
                    "total question count is incorrect");
        });

        check("Pass boundary is 50 percent", () -> {
            Result result = new Result(student.getId(), quiz.getId(), 1, 2,
                    "2026-11-11 11:10");
            require(result.getPercentage() == 50.0, "percentage should be 50.0");
            require(result.isPassed(), "50 percent should pass");
            result.setScore(0);
            require(!result.isPassed(), "0 percent should not pass");
        });
    }

    private static void testQuizAndQuestionCrud() {
        section("10. Administrator quiz and question management");

        check("Quiz create, edit and delete round trip", () -> {
            Quiz created = new Quiz("Phase 5 Test Quiz", "Testing", 5);
            require(quizService.addQuiz(created), "addQuiz returned false");
            require(created.getId() > 0, "created quiz has no id");

            Quiz loaded = quizService.getQuizById(created.getId());
            require("Phase 5 Test Quiz".equals(loaded.getTitle()), "title not stored");

            loaded.setTitle("Phase 5 Test Quiz (edited)");
            loaded.setCategory("Automated");
            loaded.setDuration(7);
            require(quizService.updateQuiz(loaded), "updateQuiz returned false");

            Quiz updated = quizService.getQuizById(created.getId());
            require("Phase 5 Test Quiz (edited)".equals(updated.getTitle()),
                    "title not updated");
            require(updated.getDuration() == 7, "duration not updated");

            require(quizService.deleteQuiz(created.getId()), "deleteQuiz returned false");
            final int deletedId = created.getId();
            expectFailure(QuizNotFoundException.class, "Deleted quiz cannot be reloaded",
                    () -> quizService.getQuizById(deletedId));
        });

        check("Question add, edit and delete round trip", () -> {
            Quiz quiz = new Quiz("Phase 5 Question Quiz", "Testing", 4);
            quizService.addQuiz(quiz);

            Question question = new Question(quiz.getId(), "2 + 2 = ?",
                    "1", "2", "3", "4", "D");
            questionService.addQuestion(question);
            List<Question> stored = questionService.getQuestionsByQuizId(quiz.getId());
            require(stored.size() == 1, "question was not stored");
            require("D".equals(stored.get(0).getCorrectAnswer()), "answer not stored");

            Question editable = stored.get(0);
            editable.setQuestionText("2 + 3 = ?");
            require(questionService.updateQuestion(editable), "updateQuestion failed");
            require("2 + 3 = ?".equals(
                            questionService.getQuestionById(editable.getId()).getQuestionText()),
                    "question text not updated");

            require(questionService.deleteQuestion(editable.getId()),
                    "deleteQuestion failed");
            require(questionService.getQuestionsByQuizId(quiz.getId()).isEmpty(),
                    "question still present after delete");

            quizService.deleteQuiz(quiz.getId());
        });

        expectFailure(QuizNotFoundException.class,
                "Questions cannot be added to a missing quiz",
                () -> questionService.addQuestion(new Question(999_999, "Ghost question",
                        "A", "B", "C", "D", "A")));
    }

    private static void testFileHandling() throws IOException {
        section("11. Byte-stream result export and import");

        List<Result> results = resultService.getResultsByStudentId(student.getId());
        Map<Integer, String> titles = new HashMap<>();
        for (Quiz quiz : quizService.getAllQuizzes()) {
            titles.put(quiz.getId(), quiz.getTitle());
        }

        Path target = Paths.get("target/test-exports/phase5-results.txt");
        Files.deleteIfExists(target);

        check("Results export to a text file", () -> {
            Path written = new ResultExporter().exportStudentResults(
                    target, student, results, titles);
            require(Files.exists(written), "export file was not created");
            require(Files.size(written) > 0, "export file is empty");
        });

        check("Exported file contains performance data but no password hash", () -> {
            List<String> lines = new ResultImporter().readLines(target);
            require(!lines.isEmpty(), "imported file has no lines");
            String content = String.join("\n", lines);
            require(content.contains("RESULT REPORT"), "report header missing");
            require(content.contains(student.getName()), "student name missing");
            require(content.toLowerCase().contains("attempts"), "attempts section missing");
            require(!content.contains(student.getPasswordHash()),
                    "password hash leaked into the export");
            require(!content.toLowerCase().contains("pbkdf2"),
                    "hash algorithm marker leaked into the export");
        });

        expectFailure(IOException.class, "Missing file raises IOException",
                () -> new ResultImporter().readLines(
                        Paths.get("target/test-exports/does-not-exist.txt")));
    }

    private static void testGenerics() {
        section("12. Generic classes, pairs and utility methods");

        check("GenericRepository stores and retrieves by type", () -> {
            GenericRepository<User> repository = new GenericRepository<>();
            repository.add(student);
            require(repository.size() == 1, "add did not increase size");
            require(repository.getById(student.getId()) == student,
                    "getById returned a different object");
            require(repository.contains(student.getId()), "contains returned false");
            require(!repository.remove(999_999), "removed a missing id");
            require(repository.remove(student.getId()), "remove failed");
            require(repository.size() == 0, "repository not empty after removal");
        });

        check("GenericPair carries a student id and name", () -> {
            GenericPair<Integer, String> pair =
                    GenericPair.of(student.getId(), student.getName());
            require(pair.getFirst().equals(student.getId()), "first value changed");
            require(pair.getSecond().equals(student.getName()), "second value changed");
            GenericPair<String, Integer> swapped = pair.swap();
            require(swapped.getFirst().equals(student.getName()), "swap first failed");
            require(swapped.getSecond().equals(student.getId()), "swap second failed");
        });

        check("Generic methods display, find, count and compare", () -> {
            List<User> users = userService.getAllUsers();
            GenericUtils.display("Students", users);
            User found = GenericUtils.find(users,
                    user -> "asha@example.com".equals(user.getEmail()));
            require(found != null && found.getId() == student.getId(),
                    "find returned the wrong user");
            int studentsOnly = GenericUtils.count(users, user -> !user.isAdmin());
            require(studentsOnly >= 1, "count found no students");
            require(GenericUtils.max(Arrays.asList(3, 9, 4)).equals(9),
                    "max returned the wrong value");
        });
    }

    private static void testBackgroundTask() throws InterruptedException {
        section("13. Background task callbacks");

        CountDownLatch success = new CountDownLatch(1);
        AtomicReference<Integer> value = new AtomicReference<>();
        new BackgroundTask<>(() -> 42, Runnable::run)
                .onSuccess(value::set)
                .onFinished(success::countDown)
                .start();
        check("Successful background work reports its value", () -> {
            require(success.await(3, TimeUnit.SECONDS), "success callback never ran");
            require(Integer.valueOf(42).equals(value.get()), "wrong value delivered");
        });

        CountDownLatch failure = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        new BackgroundTask<>(() -> {
            throw new IllegalStateException("boom");
        }, Runnable::run)
                .onFailure(error::set)
                .onFinished(failure::countDown)
                .start();
        check("Failed background work reports the exception", () -> {
            require(failure.await(3, TimeUnit.SECONDS), "failure callback never ran");
            require(error.get() instanceof IllegalStateException,
                    "wrong exception delivered");
        });
    }

    private static void testLogout() {
        section("14. Session logout");

        check("Logout removes the active session", () -> {
            User signedIn = userService.loginUser("asha@example.com", "StudentPass1");
            require(userService.getActiveSessionIds().contains(signedIn.getId()),
                    "login did not create a session");
            require(userService.logoutUser(signedIn.getId()), "logout returned false");
            require(!userService.getActiveSessionIds().contains(signedIn.getId()),
                    "session survived logout");
            require(!userService.logoutUser(signedIn.getId()),
                    "second logout should report no session");
        });
    }

    // -------------------------------------------------------------- helpers

    private static String[] correctAnswers(Quiz quiz) {
        List<Question> questions = quiz.getQuestions();
        String[] answers = new String[questions.size()];
        for (int index = 0; index < questions.size(); index++) {
            answers[index] = questions.get(index).getCorrectAnswer();
        }
        return answers;
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("-- " + title + " --");
    }

    private static void check(String description, Action action) {
        try {
            action.run();
            pass(description);
        } catch (Throwable error) {
            fail(description, error.getClass().getSimpleName() + ": " + error.getMessage());
        }
    }

    private static <T extends Throwable> void expectFailure(Class<T> expected,
                                                            String description,
                                                            Action action) {
        try {
            action.run();
            fail(description, "expected " + expected.getSimpleName()
                    + " but nothing was thrown");
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                pass(description + " -> " + error.getClass().getSimpleName());
            } else {
                fail(description, "expected " + expected.getSimpleName() + " but got "
                        + error.getClass().getSimpleName());
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void pass(String description) {
        checks++;
        System.out.println("[PASS] " + description);
    }

    private static void fail(String description, String reason) {
        checks++;
        failures++;
        System.out.println("[FAIL] " + description + " (" + reason + ")");
    }

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }
}
