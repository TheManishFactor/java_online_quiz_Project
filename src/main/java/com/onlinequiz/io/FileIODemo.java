package com.onlinequiz.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Console demonstration of byte-based file handling:
 * creating a file, writing, reading, closing streams and handling IOException.
 *
 * Run with:
 * java -cp "target/classes" com.onlinequiz.io.FileIODemo
 */
public final class FileIODemo {

    private FileIODemo() {
    }

    public static void main(String[] args) {
        Path directory = Paths.get("target", "file-demo");
        Path file = directory.resolve("sample-results.txt");

        try {
            Files.createDirectories(directory);
            System.out.println("1. File created: " + file.toAbsolutePath());

            String content = "ONLINE QUIZ SAMPLE REPORT" + System.lineSeparator()
                    + "Student: Demo Student" + System.lineSeparator()
                    + "Score: 8 / 10 (80.0%)" + System.lineSeparator();

            // 2. Write with byte streams. try-with-resources closes the stream.
            try (OutputStream output = new FileOutputStream(file.toFile())) {
                output.write(content.getBytes(StandardCharsets.UTF_8));
                output.flush();
                System.out.println("2. Data written: " + content.getBytes(StandardCharsets.UTF_8).length
                        + " bytes");
            }
            System.out.println("3. OutputStream closed by try-with-resources");

            // 4. Read the same bytes back.
            try (InputStream input = new FileInputStream(file.toFile())) {
                byte[] buffer = new byte[1024];
                int read = input.read(buffer);
                System.out.println("4. Data read   : " + new String(buffer, 0, read,
                        StandardCharsets.UTF_8).trim());
            }
            System.out.println("5. InputStream closed by try-with-resources");

            // 6. IOException handling for a missing file.
            Path missing = directory.resolve("missing-file.txt");
            try (InputStream ignored = new FileInputStream(missing.toFile())) {
                System.out.println("This line is never reached");
            } catch (IOException error) {
                System.out.println("6. IOException handled: " + error.getClass().getSimpleName()
                        + ": " + error.getMessage());
            }
        } catch (IOException error) {
            System.out.println("File operation failed: " + error.getMessage());
        }
    }
}
