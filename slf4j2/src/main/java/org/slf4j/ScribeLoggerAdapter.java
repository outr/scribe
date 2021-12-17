package org.slf4j;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import scala.None$;
import scala.Some;
import scribe.Level;
import scribe.slf4j.SLF4JHelper;

public class ScribeLoggerAdapter extends MarkerIgnoringBase implements Logger {
    private final String name;

    public ScribeLoggerAdapter(String name) {
        super();

        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return SLF4JHelper.includes(name, Level.Trace());
    }

    @Override
    public void trace(String msg) {
        SLF4JHelper.log(name, Level.Trace(), msg, None$.empty());
    }

    @Override
    public void trace(String format, Object arg) {
        SLF4JHelper.logTuple(name, Level.Trace(), MessageFormatter.format(format, arg));
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        SLF4JHelper.logTuple(name, Level.Trace(), MessageFormatter.format(format, arg1, arg2));
    }

    @Override
    public void trace(String format, Object... arguments) {
        SLF4JHelper.logTuple(name, Level.Trace(), MessageFormatter.arrayFormat(format, arguments));
    }

    @Override
    public void trace(String msg, Throwable t) {
        SLF4JHelper.log(name, Level.Trace(), msg, Some.apply(t));
    }

    @Override
    public boolean isDebugEnabled() {
        return SLF4JHelper.includes(name, Level.Debug());
    }

    @Override
    public void debug(String msg) {
        SLF4JHelper.log(name, Level.Debug(), msg, None$.empty());
    }

    @Override
    public void debug(String format, Object arg) {
        SLF4JHelper.logTuple(name, Level.Debug(), MessageFormatter.format(format, arg));
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        SLF4JHelper.logTuple(name, Level.Debug(), MessageFormatter.format(format, arg1, arg2));
    }

    @Override
    public void debug(String format, Object... arguments) {
        SLF4JHelper.logTuple(name, Level.Debug(), MessageFormatter.arrayFormat(format, arguments));
    }

    @Override
    public void debug(String msg, Throwable t) {
        SLF4JHelper.log(name, Level.Debug(), msg, Some.apply(t));
    }

    @Override
    public boolean isInfoEnabled() {
        return SLF4JHelper.includes(name, Level.Info());
    }

    @Override
    public void info(String msg) {
        SLF4JHelper.log(name, Level.Info(), msg, None$.empty());
    }

    @Override
    public void info(String format, Object arg) {
        SLF4JHelper.logTuple(name, Level.Info(), MessageFormatter.format(format, arg));
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        SLF4JHelper.logTuple(name, Level.Info(), MessageFormatter.format(format, arg1, arg2));
    }

    @Override
    public void info(String format, Object... arguments) {
        SLF4JHelper.logTuple(name, Level.Info(), MessageFormatter.arrayFormat(format, arguments));
    }

    @Override
    public void info(String msg, Throwable t) {
        SLF4JHelper.log(name, Level.Info(), msg, Some.apply(t));
    }

    @Override
    public boolean isWarnEnabled() {
        return SLF4JHelper.includes(name, Level.Warn());
    }

    @Override
    public void warn(String msg) {
        SLF4JHelper.log(name, Level.Warn(), msg, None$.empty());
    }

    @Override
    public void warn(String format, Object arg) {
        SLF4JHelper.logTuple(name, Level.Warn(), MessageFormatter.format(format, arg));
    }

    @Override
    public void warn(String format, Object... arguments) {
        SLF4JHelper.logTuple(name, Level.Warn(), MessageFormatter.arrayFormat(format, arguments));
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        SLF4JHelper.logTuple(name, Level.Warn(), MessageFormatter.format(format, arg1, arg2));
    }

    @Override
    public void warn(String msg, Throwable t) {
        SLF4JHelper.log(name, Level.Warn(), msg, Some.apply(t));
    }

    @Override
    public boolean isErrorEnabled() {
        return SLF4JHelper.includes(name, Level.Error());
    }

    @Override
    public void error(String msg) {
        SLF4JHelper.log(name, Level.Error(), msg, None$.empty());
    }

    @Override
    public void error(String format, Object arg) {
        SLF4JHelper.logTuple(name, Level.Error(), MessageFormatter.format(format, arg));
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        SLF4JHelper.logTuple(name, Level.Error(), MessageFormatter.format(format, arg1, arg2));
    }

    @Override
    public void error(String format, Object... arguments) {
        SLF4JHelper.logTuple(name, Level.Error(), MessageFormatter.arrayFormat(format, arguments));
    }

    @Override
    public void error(String msg, Throwable t) {
        SLF4JHelper.log(name, Level.Error(), msg, Some.apply(t));
    }
}