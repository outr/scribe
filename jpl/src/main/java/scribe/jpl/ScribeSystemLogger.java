package scribe.jpl;

import scala.Function0;
import scribe.LogRecord;
import scribe.Logger$;
import scribe.message.LoggableMessage;
import scribe.message.LoggableMessage$;
import scribe.throwable.TraceLoggableMessage;
import scala.Option;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;
import scala.jdk.javaapi.CollectionConverters;
import scribe.util.Time$;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

import scala.jdk.javaapi.FunctionConverters;

public final class ScribeSystemLogger implements System.Logger {
    private final String name;
    private final scribe.Logger delegate;

    public ScribeSystemLogger(String name) {
        this.name = Objects.requireNonNull(name, "name");
        this.delegate = Logger$.MODULE$.apply(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLoggable(System.Logger.Level level) {
        return delegate.includes(translate(level));
    }

    @Override
    public void log(System.Logger.Level level,
                    ResourceBundle bundle,
                    String msg,
                    Throwable thrown) {
        final scribe.Level sLevel = translate(level);
        if (!delegate.includes(sLevel)) return;

        final String message = localize(bundle, msg);
        final java.util.List<LoggableMessage> parts = new ArrayList<>(2);
        parts.add(LoggableMessage$.MODULE$.string2LoggableMessage(FunctionConverters.asScalaFromSupplier(() -> message)));
        if (thrown != null) {
            parts.add(new TraceLoggableMessage(thrown));
        }
        emit(sLevel, parts);
    }

    @Override
    public void log(System.Logger.Level level,
                    ResourceBundle bundle,
                    String format,
                    Object... params) {
        final scribe.Level sLevel = translate(level);
        if (!delegate.includes(sLevel)) return;

        final String pattern = localize(bundle, format);
        final String message = (pattern == null)
                ? null
                : MessageFormat.format(pattern, params == null ? new Object[0] : params);

        final java.util.List<LoggableMessage> parts = new ArrayList<>(1);
        parts.add(LoggableMessage$.MODULE$.string2LoggableMessage(FunctionConverters.asScalaFromSupplier(() -> message)));
        emit(sLevel, parts);
    }

    /* --------------------- helpers --------------------- */

    private void emit(scribe.Level level, java.util.List<LoggableMessage> jMessages) {
        final List<LoggableMessage> messages = CollectionConverters.asScala(jMessages).toList();

        // Build a LogRecord with minimal metadata (fileName empty, class/method optional)
        final Option<String> so = Option.empty();
        final Map<String, Function0<Object>> m = Map$.MODULE$.empty();
        final LogRecord record = new LogRecord(
                level,                     // level
                level.value(),             // levelValue
                messages,                  // messages (Scala immutable.List)
                "",                        // fileName
                name,                      // className (use logger name)
                so,                        // methodName
                Option.empty(),            // line
                Option.empty(),            // column
                Thread.currentThread(),
                m,
                Time$.MODULE$.apply()
        );

        Function0<LogRecord> f0 = FunctionConverters.asScalaFromSupplier(() -> record);
        delegate.log(f0);
    }

    private static scribe.Level translate(System.Logger.Level level) {
        switch (level) {
            case ALL:
            case TRACE:
                return scribe.Level.Trace();
            case DEBUG:
                return scribe.Level.Debug();
            case INFO:
                return scribe.Level.Info();
            case WARNING:
                return scribe.Level.Warn();
            case ERROR:
                return scribe.Level.Error();
            case OFF:
                return scribe.Level.Fatal(); // mirrors original mapping
            default:
                return scribe.Level.Info();
        }
    }

    private static String localize(ResourceBundle bundle, String msg) {
        if (bundle == null || msg == null) return msg;
        return unsafeGet(bundle, msg);
    }

    private static String unsafeGet(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (ClassCastException e) {
            try {
                Object o = bundle.getObject(key);
                return (o == null) ? null : o.toString();
            } catch (Exception ignored) {
                return key;
            }
        } catch (Exception ignored) {
            return key;
        }
    }
}