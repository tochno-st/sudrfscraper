package com.github.courtandrey.sudrfscraper.service.logger;

import com.github.courtandrey.sudrfscraper.service.ConstantsGetter;

public enum Message {
    FILL_IN_CAPTCHA,
    RESULT,
    STRATEGY_NOT_CHOSEN,
    DUMP,
    DUMP_RU,
    SEARCH_REQUEST_NOT_SET,
    EXECUTION_TIME,
    WRONG_DUMP,
    NO_TEXT_FOUND,
    VNKOD_NOT_FOUND,
    EXECUTION_EXCEPTION_OCCURRED,
    EXCEPTION_OCCURRED_WHILE_PARSING,
    DOCUMENT_NOT_PARSED,
    VNKOD_MISSING,
    MANY_VNKODS,
    GO_TO_ANOTHER_PAGE,
    BEGINNING_OF_EXECUTION,
    BEGINNING_OF_EXECUTION_RU,
    CASES_WITH_TEXTS,
    NO_DECISION_TEXT,
    CASES_PER_REGION,
    UNKNOWN_DUMP,
    UNKNOWN_ARTICLE,
    INVALID_OUTPUT,
    CONNECTION_INFO_NOT_SET,
    IOEXCEPTION_OCCURRED,
    DRIVER_NOT_FOUND,
    SQL_EXCEPTION_OCCURRED,
    EXCEPTION_OCCURRED,
    WRONG_DATE_FORMAT,
    WRONG_ARTICLE_FORMAT,
    SUSPICIOUS_NUMBER_OF_CASES,
    SQL_CONNECTION_ERROR,
    EXECUTION_STATUS,
    EXECUTION_STATUS_END,
    WRONG_REGIONS_FORMAT,
    MALFORMED_CONFIG,
    COLLECTING_TEXTS,
    COLLECTED_TEXTS,
    COURT_CONFIG_TO_STRING,
    WRONG_LEVEL_FORMAT,
    SOME_SQL_EXCEPTION,
    WRONG_STRATEGY_FORMAT,
    MOSGORSUD_PARSING_EXCEPTION,
    DOWNLOAD_FAILED,
    CONVERSION_FAILED,
    DOWNLOAD_FAILED_UNKNOWN_EXTENSION, ENCODING_NOT_PARSED, SECOND_LAP, SECOND_LAP_RU, SCRAPING_DONE, SCRAPING_DONE_RU,
    EXECUTION_STATUS_BEGINNING,
    EXECUTION_STATUS_MID,
    COULD_NOT_PARSE_ROW, CHUNK;

    @Override
    public String toString() {
        return ConstantsGetter.getMessage(this);
    }

}
