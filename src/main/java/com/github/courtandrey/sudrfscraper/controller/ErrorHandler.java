package com.github.courtandrey.sudrfscraper.controller;

public interface ErrorHandler {
    void errorOccurred(Throwable e, Thread t);
}
