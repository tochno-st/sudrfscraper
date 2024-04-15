package com.github.courtandrey.sudrfscraper.configuration.searchrequest;

public enum Field {
    ADMIN,
    CAS,
    CRIMINAL,
    MATERIAL_PROCEEDING, CIVIL;

    public Instance[] getInstanceList() {
        if (this != MATERIAL_PROCEEDING) return new Instance[]{Instance.FIRST, Instance.APPELLATION, Instance.CASSATION};
        return new Instance[]{Instance.FIRST, Instance.CASSATION};
    }

    public static Field parseField(String field) {
        for (Field fd:Field.values()) {
            if (fd.name().equals(field)) {
                return fd;
            }
        }
        throw new UnsupportedOperationException("No such field name");
    }
}
