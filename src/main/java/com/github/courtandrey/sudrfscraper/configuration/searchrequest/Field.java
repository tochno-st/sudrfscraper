package com.github.courtandrey.sudrfscraper.configuration.searchrequest;

public enum Field {
    ADMIN,
    CAS,
    CRIMINAL;

    public static Field parseField(String field) {
        for (Field fd:Field.values()) {
            if (fd.name().equals(field)) {
                return fd;
            }
        }
        throw new UnsupportedOperationException("No such field name");
    }
}
