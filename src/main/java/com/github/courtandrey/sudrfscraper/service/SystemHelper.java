package com.github.courtandrey.sudrfscraper.service;

import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class SystemHelper {
    public void doBeeps() {
        Toolkit.getDefaultToolkit().beep();
        ThreadHelper.sleep(0.75);
        Toolkit.getDefaultToolkit().beep();
        ThreadHelper.sleep(0.75);
        Toolkit.getDefaultToolkit().beep();
    }
}
