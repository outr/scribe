# scribe #

[![Build Status](https://travis-ci.org/outr/scribe.svg?branch=master)](https://travis-ci.org/outr/scribe)
[![Stories in Ready](https://badge.waffle.io/outr/scribe.png?label=ready&title=Ready)](https://waffle.io/outr/scribe)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/scribe)
[![Maven Central](https://img.shields.io/maven-central/v/com.outr.scribe/scribe-core_2.11.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr.scribe/scribe-core_2.11)

Scribe is a completely different way of thinking about logging. Instead of wrapping around existing logging frameworks, Scribe is built from the ground up to provide fast and effective logging in Scala
without the need of configuration files additional dependencies. All management of logging is handled programmatically in Scala itself.

## SBT Configuration ##

Scribe is published to Sonatype OSS and Maven Central:

```libraryDependencies += "com.outr.scribe" %% "scribe-core" % "1.0.0"```

## Using Scribe ##

The easiest way to use Scribe is to simply mix-in the `Logging` trait into your class or object and then use the
`logger` instance that is available to you:

```scala
import com.outr.scribe.Logging

class MyClass extends Logging {
  logger.info("Hello, World!")
}
```

The default logging configuration will output to the console and includes `Info` and above.

## Configuring Scribe ##

All configuration of Scribe is managed in Scala itself to avoid messy configuration files.

### Parents ###

All loggers can be configured to define a parent logger that subscribes to all logging events fired by that logger.

### The Root Logger ###

By default all loggers have a direct parent of `Logger.Root`. It is this logger that has the default console writer.

### Logging to a File ###

The following will add a new `LogHandler` to the specified `logger` to append all `Debug` logs and above to a daily file.

```scala
logger.addHandler(LogHandler(level = Level.Debug, writer = new FileWriter(directory, FileWriter.Daily)))
```

### Configuring the Logger ###

A `Logger` is actually just a case class with some additional functionality added on. The `Logger` contains `name`,
`parent`, `multiplier`, and `includeTrace`.

`name: String`: The name used to reference the logger being used. This defaults to the full class name.

`parent: Option[Logger]`: The parent logger that will receive all logging events this logger receives. Defaults to `Some(Logger.Root)`.

`multiplier: Double`: The multiplier added to logs coming through this logger. This provides the ability to boost the value for log records.
For example, if you want to see all `Debug` levels for a specific class only, but your handler is filtering to only
show `Info` and above you can set `multiplier` to `2.0` and all logs that come through will boost to be included on
the next levels value. It is worth noting that they will still output with their proper level name. Defaults to `1.0`.

`includeTrace: Boolean`: Flag to determine whether trace information (method name and line number) should be included in logging information.
This is a somewhat expensive feature, but can be very useful for tracking down bugs. Defaults to false.

If I want to simply update my logger removing the `Logger.Root` parent and set a new name I can do so like this:

```scala
import com.outr.scribe.Logging

class MyClass extends Logging {
  updateLogger { l =>
    l.copy(name = "HelloWorld", parent = None)
  }
}
```

This will update the logger being used for this class going forward. All existing handlers on the previous `Logger` instance
will be re-applied to this new instance as well.