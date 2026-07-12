package com.lisory.backend.shared.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public final class StructuredLogger {

    private final Logger logger;

    private StructuredLogger(Logger logger) {
        this.logger = logger;
    }

    public static StructuredLogger forClass(Class<?> clazz) {
        return new StructuredLogger(LoggerFactory.getLogger(clazz));
    }

    public void info(String event, Map<String, Object> fields) {
        if (logger.isInfoEnabled()) {
            logger.info(formatMessage(event, fields));
        }
    }

    public void warn(String event, Map<String, Object> fields) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatMessage(event, fields));
        }
    }

    public void error(String event, Map<String, Object> fields, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error(formatMessage(event, fields), t);
        }
    }

    public void error(String event, Map<String, Object> fields) {
        if (logger.isErrorEnabled()) {
            logger.error(formatMessage(event, fields));
        }
    }

    private String formatMessage(String event, Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return "[" + event + "]";
        }
        String fieldString = fields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(" "));
        return "[" + event + "] " + fieldString;
    }
}
