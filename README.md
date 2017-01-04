# scribe

[![Build Status](https://travis-ci.org/outr/scribe.svg?branch=master)](https://travis-ci.org/outr/scribe)
[![Stories in Ready](https://badge.waffle.io/outr/scribe.png?label=ready&title=Ready)](https://waffle.io/outr/scribe)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/scribe)
[![Maven Central](https://img.shields.io/maven-central/v/com.outr/scribe_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr/scribe_2.12)
[![Latest version](https://index.scala-lang.org/com.outr/scribe/scribe/latest.svg)](https://index.scala-lang.org/com.outr/scribe/scribe)

Scribe is a completely different way of thinking about logging. Instead of wrapping around existing logging frameworks
and bearing their performance and design flaws, Scribe is built from the ground up to provide fast and effective logging
in Scala and Scala.js without the need of configuration files additional dependencies. All management of logging is
handled programmatically in Scala itself giving the developer the freedom to use whatever configuration framework, if
any, they should choose to use.

## Why Another Logging Framework? ##

Yes, we know there are too many Java logging frameworks to count, and a large number of decent logging frameworks in
Scala, so why did we write yet another logging framework?  As we see it, nearly every Scala logging framework is mostly
just a wrapper around Java logging frameworks (usually SLF4J, Log4J, or Logback). This comes with a few problems:

1. No support for Scala.js
2. Performance cost
3. Additional dependencies

A few of the main features that Scribe offers:

1. Performance is critical consideration. We leverage Macros to handle optimization of everything possible at compile-time
to avoid logging slowing down your production application.
2. Programmatic configuration. No need to be bound to configuration files to configure your logging. This means you can
rely on any configuration framework or you can configure real-time changes to your logging in your production environment.
This particularly comes in handy if you need to enable debug logging on something going wrong in production. No need to
restart your server, simply provide a mechanism to modify the logging configuration in real-time.
3. Clean logging. Macros allow us to introduce logging into a class via an import instead of a mix-in or unnecessary
setup code.
4. Zero cost class, method, and line number logging built-in. Never worry about your logger working up the stack to figure
out the position of the logging statement at runtime. With Macros we determine that information at compile-time to avoid
any runtime cost.

## SBT Configuration ##

Scribe is published to Sonatype OSS and Maven Central and supports JVM and Scala.js with 2.11 and 2.12:

```
libraryDependencies += "com.outr" %% "scribe" % "1.3.0"   // Scala
libraryDependencies += "com.outr" %%% "scribe" % "1.3.0"  // Scala.js
```

## Using Scribe ##

As of 1.3 Scribe has become much easier to use. Previously, the preferred way to use Scribe was as a mix-in to your
class the way many logging frameworks do.  However, Scribe now supports a package-level Macro that will give you the
same result via an import without needing to mix-in anything:

```scala
import com.outr.scribe._

class MyClass {
  logger.info("Hello, World!")
}
```

The output will look something like the following:

```
2017.01.02 19:05:47:342 [main] INFO MyClass:4 - Hello, World!
```

In addition, you can utilize the implicit class to log on a specific instance without touching the code of that class:

```scala
import com.outr.scribe._

class MyClass {
  val myString = "Nothing Special About Me"
  myString.logger.info("Logging on a String!")
}
```

Though at first glance this might not seem useful, you can configure logging on an instance and it will utilize the
class name to retain configuration:

```scala
import com.outr.scribe._

class MyClass {
  val myString = "Nothing Special About Me"
  myString.logger.addHandler(LogHandler(level = Level.Debug, writer = new FileWriter(directory, FileWriter.Daily())))
  myString.logger.info("Logging on a String!")
  
  "Another String".logger.info("Written to a file...")
}
```

The second logging call will share the same `Logger` instance as it is derived from the class name. This makes it very
easy to configure and log explicitly to types without a lot of extra boilerplate or hassle.

## The Old Way ##

Though probably less useful, you can still utilize the `Logging` mix-in to add logging support to your class like most
other frameworks do:

```scala
import com.outr.scribe.Logging

class MyClass extends Logging {
  logger.info("Hello, World!")
}
```

The default logging configuration will output to the console and includes `Info` and above. Log records also include
trace information (method name and line number) to track down bugs more easily at zero cost as they are derived at
compile-time (as opposed to nearly every other logging framework).

## Configuring Scribe ##

All configuration of Scribe is managed in Scala itself to avoid messy configuration files.

### Parents ###

All loggers can be configured to define a parent logger that subscribes to all logging events fired by that logger.

### The Root Logger ###

By default all loggers have a direct parent of `Logger.Root`. It is this logger that has the default console writer.

### Logging to a File ###

The following will add a new `LogHandler` to the specified `logger` to append all `Debug` logs and above to a daily file.

```scala
logger.addHandler(LogHandler(level = Level.Debug, writer = new FileWriter(directory, FileWriter.Daily())))
```

You may want to use `Logger.Root` instead of `logger` to set this handler globally.

### Configuring the Logger ###

A `Logger` is actually just a case class with some additional functionality added on. The `Logger` contains `parentName`
and `multiplier`.

`parentName: Option[String]`: The parent logger that will receive all logging events this logger receives. Defaults to
`Some(Logger.rootName)`.

`multiplier: Double`: The multiplier added to logs coming through this logger. This provides the ability to boost the\
value for log records. For example, if you want to see all `Debug` levels for a specific class only, but your handler is
filtering to only show `Info` and above you can set `multiplier` to `2.0` and all logs that come through will boost to
be included on the next levels value. It is worth noting that they will still output with their proper level name.
Defaults to `1.0`.

If I want to simply update my logger removing the `Logger.Root` parent reference I can do so like this:

```scala
import com.outr.scribe.Logging

class MyClass extends Logging {
  logger.update {
    logger.copy(parentName = None)
  }
}
```

This will update the logger being used for this class going forward. All existing handlers on the previous `Logger`
instance will be re-applied to this new instance as well.

### SLF4J Logger ###

If you add the `scribe-slf4j` dependency to your project Scribe will be picked up as an SLF4J implementation:

```
libraryDependencies += "com.outr" %% "scribe-slf4j" % "1.3.0"   // Scala
```

Obviously this only applies to JVM as SLF4J isn't available in the browser.