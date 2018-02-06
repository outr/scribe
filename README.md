# scribe

[![Build Status](https://travis-ci.org/outr/scribe.svg?branch=master)](https://travis-ci.org/outr/scribe)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/88d47cae4fc6459dadeabae2e20af45a)](https://www.codacy.com/app/matthicks/scribe?utm_source=github.com&utm_medium=referral&utm_content=outr/scribe&utm_campaign=Badge_Coverage)
[![Stories in Ready](https://badge.waffle.io/outr/scribe.png?label=ready&title=Ready)](https://waffle.io/outr/scribe)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/scribe)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outr/scribe_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr/scribe_2.12)
[![Latest version](https://index.scala-lang.org/outr/scribe/scribe/latest.svg)](https://index.scala-lang.org/outr/scribe)

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
2. Performance cost (Blog Post: http://www.matthicks.com/2017/01/logging-performance.html)
3. Additional dependencies

A few of the main features that Scribe offers:

1. Performance is a critical consideration. We leverage Macros to handle optimization of everything possible at compile-time
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

### Performance Comparison

Since Scala Logging is the most popular Scala logging framework, we did a benchmark to compare the logging speed and
memory usage of the two logging frameworks:

![Logging Rate Over Sixy Seconds](https://1.bp.blogspot.com/-Sn0WJ91M47s/WHepNt0ok9I/AAAAAAAABrQ/4E7LOFv1RLo_XUlksp7t_Mnz_thsHhw5QCLcB/s1600/logging-speed.png)

That is a comparison of how many records were able to be logged over sixty seconds.  In addition, we can see the memory
comparison after that benchmark was run:

![Logging Memory Usage](https://2.bp.blogspot.com/-iI0njBLrYk8/WHeqTwuagiI/AAAAAAAABrc/dRktzYWBuOkY9V7Z7gWDDRuN6RE2sbEIQCLcB/s1600/logging-memory.png)

Though not a staggering distinction, Scribe is clearly both faster and has a smaller memory footprint. For more on the
results of the benchmark take a look at this blog post: http://www.matthicks.com/2017/01/logging-performance.html

## Library Dependencies

No matter how simple or straight-forward a library might be, its dependencies can quickly add a lot of bloat to your
project. This is why in Scribe we have absolutely no external dependencies for the core library.

### scribe-slf4j
* slf4j-api

### scribe-slack
* gigahorse-asynchttpclient (https://github.com/eed3si9n/gigahorse)
* upickle (https://github.com/lihaoyi/upickle-pprint)

## SBT Configuration ##

Scribe is published to Sonatype OSS and Maven Central and supports JVM and Scala.js with 2.11 and 2.12:

```
libraryDependencies += "com.outr" %% "scribe" % "2.0.0"   // Scala
libraryDependencies += "com.outr" %%% "scribe" % "2.0.0"  // Scala.js
```

## Using Scribe ##

Scribe supports a zero import and zero mix-in logging feature to make it far faster and easier to use logging in your
application:

```scala
class MyClass {
  scribe.info("Hello, World!")
  doSomething()
  
  def doSomething(): Unit = {
    scribe.info("I did something!")
  }
}
```

The output will look something like the following:

```
2017.01.02 19:05:47:342 [main] INFO MyClass:2 - Hello, World!
2017.01.02 19:05:47.342 [main] INFO MyClass.doSomething:6 - I did something!
```

You can utilize the implicit class to log on a specific instance without touching the code of that class:

```scala
import scribe._

class MyClass {
  val myString = "Nothing Special About Me"
  myString.logger.info("Logging on a String!")
}
```

Though at first glance this might not seem useful, you can configure logging on an instance and it will utilize the
class name to retain configuration:

```scala
import scribe._

class MyClass {
  val myString = "Nothing Special About Me"
  myString.logger.addHandler(LogHandler(level = Level.Debug, writer = new FileWriter(directory, FileWriter.daily())))
  myString.logger.info("Logging on a String!")
  
  "Another String".logger.info("Written to a file...")
}
```

The second logging call will share the same `Logger` instance as it is derived from the class name. This makes it very
easy to configure and log explicitly to types without a lot of extra boilerplate or hassle.

## Classic Mix-In Logging ##

Scribe also supports a more classic style of logging via mix-in of the `Logging` trait:

```scala
import scribe.Logging

class MyClass extends Logging {
  logger.info("Hello, World!")
}
```

The default logging configuration will output to the console and includes `Info` and above. Log records also include
trace information (method name and line number) to track down bugs more easily at zero cost as they are derived at
compile-time (as opposed to nearly every other logging framework that walks the stack trace which is very expensive).

## Logger by name ##

If you come from Java then this may all seem a little bit foreign to you. You can always go old-school and simply get a
logger by name and use it:

```scala
import scribe.Logger

class MyClass {
  val logger = Logger.byName("MyClass")
  logger.info("I'm old-school!")
}
```

## Configuring Scribe ##

All configuration of Scribe is managed in Scala itself to avoid messy configuration files.

### Parents ###

All loggers can be configured to define a parent logger that subscribes to all logging events fired by that logger.

### The Root Logger ###

By default all loggers have a direct parent of `Logger.Root`. It is this logger that has the default console writer.

### Logging to a File ###

The following will add a new `LogHandler` to the specified `logger` to append all `Debug` logs and above to a daily file.

```scala
logger.addHandler(LogHandler(level = Level.Debug, writer = new FileWriter(directory, FileWriter.daily())))
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
import scribe.Logging

class MyClass extends Logging {
  logger.update {
    logger.copy(parentName = None)
  }
}
```

This will update the logger being used for this class going forward. All existing handlers on the previous `Logger`
instance will be re-applied to this new instance as well.

To change the default global log level, use:

```scala
Logger.root.clearHandlers()
Logger.root.addHandler(LogHandler(level = Level.Error))
```

You can configure the output (how the log will look like) when adding a `LogHandler`.
The `Formatter` companion has three pre-defined scenarios (simple, default, and trace).
Building your own Formatter instance is easy with the `FormatterBuilder`:

```scala
Logger.root.addHandler(LogHandler(formatter = FormatterBuilder()
        .string("[")
        .threadName.string("] ")
        .positionAbbreviated.string(" - ")
        .message
        .newLine))     

```

### SLF4J Logger ###

If you add the `scribe-slf4j` dependency to your project Scribe will be picked up as an SLF4J implementation:

```
libraryDependencies += "com.outr" %% "scribe-slf4j" % "1.4.6"
```

Obviously this only applies to JVM as SLF4J isn't available in the browser. This will allow any existing application that
relies on SLF4J to log through Scribe without any additional configuration.

### Slack Logging ###

If you add the `scribe-slack` dependency to your project you can configure Scribe to log to Slack (https://slack.com/).

```
libraryDependencies += "com.outr" %% "scribe-slack" % "2.0.0"
```

The easiest way to configure this is to use the convenience method `configure`:

```scala
scribe.slack.Slack.configure(serviceHash, botName)
```

`serviceHash` is the API service hash provided by Slack.

`botName` is the name of the bot when it sends a message to Slack.

The method has several other parameters available but has defaults. For example, this will default to using the `root`
logger, but you can override the default to use any logger.

Finally, if you want complete control over the logging and formatting you can directly use the `SlackWriter`.