package org.slf4j.impl;

import com.scribe.slf4j.ScribeLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    private static final String loggerFactoryClassStr = ScribeLoggerFactory.class.getName();

    public static String REQUESTED_API_VERSION = "1.7.15";

    private final ILoggerFactory loggerFactory;

    private StaticLoggerBinder() {
        loggerFactory = new ScribeLoggerFactory();
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return loggerFactoryClassStr;
    }
}
