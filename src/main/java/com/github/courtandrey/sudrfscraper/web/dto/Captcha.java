package com.github.courtandrey.sudrfscraper.web.dto;

import lombok.Getter;

@Getter
public class Captcha {
    private String captcha;

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
