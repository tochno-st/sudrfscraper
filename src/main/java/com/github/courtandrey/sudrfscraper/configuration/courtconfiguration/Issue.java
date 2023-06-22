package com.github.courtandrey.sudrfscraper.configuration.courtconfiguration;

public enum Issue {
    CAPTCHA_NOT_CONFIGURABLE("Cannot configure captcha",3),
    CONNECTION_ERROR("Server error",4),
    NOT_FOUND_CASE("Cases not found",1),
    URL_ERROR("Court is ignoring request. Court is probably using different interface",4),
    CAPTCHA("CAPTCHA occurred",0),
    SUCCESS("No issues",0),
    CONFIGURATION_ERROR("Important configuration element missing (vnkod)",-1),
    ERROR("Error occurred",-2),
    INACTIVE_COURT("Inactive court",2),
    INACTIVE_MODULE("One of following: inactive court or different interface",2),
    NOT_SUPPORTED_REQUEST("Court does not support request", 3),
    UNDEFINED_ISSUE("Issue cannot be defined", 5),
    NOT_FOUND("Server returns 404",4),
    BLOCKED("Court is blocking request",-1),
    LOOPED_CAPTCHA("Captcha is not accepted", 5);

    final String description;
    final int issueLevel;

    public static boolean isBadIssue(Issue issue) {
        return issue == ERROR || issue == CONFIGURATION_ERROR || issue == UNDEFINED_ISSUE;
    }

    public static boolean isGoodIssue(Issue issue) {
        return issue == SUCCESS || issue == NOT_FOUND_CASE;
    }
    public static Issue compareAndSetIssue(Issue thisIssue, Issue thatIssue) {
        if (thatIssue == null && thisIssue == null) return null;
        if (thatIssue == null) return thisIssue;
        if (thisIssue == null) return thatIssue;
        if (thisIssue.issueLevel > thatIssue.issueLevel) {
            return thatIssue;
        }
        return thisIssue;
    }

    public static boolean isPreventable(Issue issue) {
        return issue == INACTIVE_COURT || issue == INACTIVE_MODULE || issue == CONNECTION_ERROR;
    }

    Issue(String description,int issueLevel) {
        this.description = description;
        this.issueLevel = issueLevel;
    }


    @Override
    public String toString() {
        return description;
    }
}
