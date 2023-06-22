package com.github.courtandrey.sudrfscraper.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Component
public class Validator {
    public LocalDate validateDate(String date) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ignored) {}
        return null;
    }

    public boolean validateDirectory(String directory) {
        try {
            Path p = Path.of(directory);
            if (Files.exists(p)) return true;
            Files.createDirectories(p);
            return true;
        } catch (IOException ignored) {}
        return false;
    }

    public boolean validateName(String name) {
        return Character.isLetter(name.charAt(0)) && !name.contains(".") && !name.contains("/");
    }
}
