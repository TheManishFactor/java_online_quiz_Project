package com.onlinequiz.io;

import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Writes a student's result history with byte streams.
 *
 * OutputStream is the general byte-stream contract; FileOutputStream connects it
 * to a file. The text is converted to UTF-8 bytes, which is what the stream
 * actually writes. try-with-resources closes the stream even when writing fails.
 *
 * Only quiz performance data is exported: names, scores and dates. Password
 * hashes and other account secrets are deliberately never written.
 */
public final class ResultExporter {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Builds the default file name used by the history screen.
     */
    public static Path defaultFile(User student) {
        String name = student.getName().replaceAll("[^A-Za-z0-9]+", "_");
        return Paths.get("exports", "results_" + student.getId() + "_" + name
                + "_" + LocalDateTime.now().format(TIMESTAMP) + ".txt");
    }

    /**
     * Exports the supplied results and returns the written file.
     *
     * @param target     destination file; parent directories are created
     * @param student    owner of the results
     * @param results    attempts to export
     * @param quizTitles quiz id to title lookup
     * @throws IOException when the file cannot be created or written
     */
    public Path exportStudentResults(Path target, User student, List<Result> results,
                                     Map<Integer, String> quizTitles) throws IOException {
        if (target == null) {
            throw new IOException("Choose a destination file");
        }
        if (student == null) {
            throw new IOException("A student is required to export results");
        }

        String content = format(student, results, quizTitles);
        Path file = target.toAbsolutePath().normalize();
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (OutputStream output = new FileOutputStream(file.toFile())) {
            output.write(content.getBytes(StandardCharsets.UTF_8));
            output.flush();
        }
        return file;
    }

    /**
     * Formats the report. Package-visible so tests can assert the exported text
     * without re-reading the file.
     */
    String format(User student, List<Result> results, Map<Integer, String> quizTitles) {
        StringBuilder report = new StringBuilder();
        report.append("ONLINE QUIZ APPLICATION - RESULT REPORT").append(System.lineSeparator());
        report.append("Generated : ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .append(System.lineSeparator());
        report.append("Student   : ").append(student.getName()).append(System.lineSeparator());
        report.append("Email     : ").append(student.getEmail()).append(System.lineSeparator());
        report.append("Role      : ").append(student.getRole()).append(System.lineSeparator());

        int attempts = results == null ? 0 : results.size();
        double average = 0.0;
        if (results != null && !results.isEmpty()) {
            double total = 0.0;
            for (Result result : results) {
                total += result.getPercentage();
            }
            average = Math.round((total / results.size()) * 100.0) / 100.0;
        }
        report.append("Attempts  : ").append(attempts).append(System.lineSeparator());
        report.append("Average   : ").append(average).append('%').append(System.lineSeparator());
        report.append(System.lineSeparator());
        report.append("ATTEMPTS").append(System.lineSeparator());
        report.append("------------------------------------------------------------")
                .append(System.lineSeparator());

        if (results == null || results.isEmpty()) {
            report.append("No quiz attempts recorded.").append(System.lineSeparator());
        } else {
            for (Result result : results) {
                String title = quizTitles == null
                        ? "Quiz " + result.getQuizId()
                        : quizTitles.getOrDefault(result.getQuizId(), "Quiz " + result.getQuizId());
                report.append("#").append(result.getId())
                        .append(" | ").append(title)
                        .append(" | ").append(result.getScoreLabel())
                        .append(" | ").append(result.getPercentage()).append('%')
                        .append(" | Grade ").append(result.getGrade())
                        .append(" | ").append(result.isPassed() ? "PASS" : "FAIL")
                        .append(" | ").append(result.getAttemptDate())
                        .append(System.lineSeparator());
            }
        }
        return report.toString();
    }
}
