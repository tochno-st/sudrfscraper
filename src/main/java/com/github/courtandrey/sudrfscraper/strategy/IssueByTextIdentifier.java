package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue;

public class IssueByTextIdentifier {
    public Issue checkText(String text, Connection connection) {
        if (text.contains("код с картинки") || text.contains("Время жизни сессии закончилось")) {
            return Issue.CAPTCHA;
        }

        else if (text.contains("для получения полной информации по делу или материалу, нажмите на номер") ||
                text.contains("НЕВЕРНЫЙ ФОРМАТ ЗАПРОСА!") ||
                text.matches(".*На ..\\...\\..... дел не назначено.*")) {
            return Issue.URL_ERROR;
        }

        else if (text.contains("Данных по запросу не обнаружено") ||
                text.contains("Данных по запросу не найдено") ||
                text.contains("Ничего не найдено") || text.contains("Указанный сервер СДП не найден.")) {
            return Issue.NOT_FOUND_CASE;
        }

        else if (text.contains("№ дела") || text.contains("Дата поступления") || text.contains("Номер дела")
                || text.contains("Всего по запросу найдено")) {
            return Issue.SUCCESS;
        }

        else if (text.contains("Информация временно недоступна") || text.contains("Warning: pg_connect():") || text.contains("В настоящий момент производится информационное наполнение сайта. Обратитесь к странице позже")) {
            return Issue.INACTIVE_COURT;
        }

        else if (text.contains("Данный модуль неактивен")) {
            return Issue.INACTIVE_MODULE;
        }

        else if ((text.contains("503")
                && text.contains("В настоящее время сайт временно недоступен. Обратитесь к данной странице позже")) ||
                (text.contains("Ð\u0092 Ñ\u0081Ð²Ñ\u008FÐ·Ð¸ Ñ\u0081 Ð¿Ñ\u0080Ð¾Ð²ÐµÐ´ÐµÐ½Ð¸ÐµÐ¼ Ñ\u0080Ð°Ð±Ð¾Ñ\u0082 Ð¿Ð¾ Ð¿Ñ\u0080Ð¾Ñ\u0082"))) {
            return Issue.CONNECTION_ERROR;
        }

        else if (text.contains("ERROR LEVEL 2")) {
            return Issue.NOT_SUPPORTED_REQUEST;
        }
        else if (text.contains("Этот запрос заблокирован по соображениям безопасности")) {
            return Issue.BLOCKED;
        }
        else if (text.contains("Возможно, она была удалена или перемещена.")) {
            return Issue.NOT_FOUND;
        }

        else if (connection != Connection.SELENIUM){
            return Issue.UNDEFINED_ISSUE;
        }

        else {
            return Issue.CONNECTION_ERROR;
        }
    }
}
