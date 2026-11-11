package com.onlinequiz.io;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads an exported result report back into memory with byte streams.
 *
 * InputStream is the general byte-stream contract; FileInputStream connects it to
 * a file. Bytes are collected first and only then decoded as UTF-8, so multi-byte
 * characters are never split while reading.
 */
public final class ResultImporter {

    /**
     * Reads a report and returns its lines.
     *
     * @param file report created by {@link ResultExporter}
     * @return non-empty lines in file order
     * @throws IOException when the path is missing or the file cannot be read
     */
    public List<String> readLines(Path file) throws IOException {
        if (file == null) {
            throw new IOException("Choose a report file to import");
        }

        ByteArrayOutputStream collected = new ByteArrayOutputStream();
        try (InputStream input = new FileInputStream(file.toFile())) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                collected.write(buffer, 0, read);
            }
        }

        String text = new String(collected.toByteArray(), StandardCharsets.UTF_8);
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            if (!line.trim().isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }
}
