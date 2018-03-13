# scribe

[![Build Status](https://travis-ci.org/outr/scribe.svg?branch=master)](https://travis-ci.org/outr/scribe)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/88d47cae4fc6459dadeabae2e20af45a)](https://www.codacy.com/app/matthicks/scribe?utm_source=github.com&utm_medium=referral&utm_content=outr/scribe&utm_campaign=Badge_Coverage)
[![Stories in Ready](https://badge.waffle.io/outr/scribe.png?label=ready&title=Ready)](https://waffle.io/outr/scribe)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/scribe)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outr/scribe_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr/scribe_2.12)
[![Latest version](https://index.scala-lang.org/outr/scribe/scribe/latest.svg)](https://index.scala-lang.org/outr/scribe)

Scribe is a completely different way of thinking about logging. Instead of wrapping around existing logging frameworks
and bearing their performance and design flaws, Scribe is built from the ground up to provide fast and effective logging
in Scala, Scala.js, and Scala Native without the need of configuration files or additional dependencies. All management
of logging can be handled programmatically (of course, classic logging configuration can be utilized as well if desired)
in Scala itself, giving the developer the freedom to use whatever configuration framework, if any, they should choose to
use.

## Why Another Logging Framework?

Yes, we know there are too many Java logging frameworks to count, and a large number of decent logging frameworks in
Scala, so why did we write yet another logging framework?  Nearly every Scala logging framework is mostly just a wrapper
around Java logging frameworks (usually SLF4J, Log4J, or Logback). This comes with a few problems:

1. No support for Scala.js
2. Performance cost (Blog Post: http://www.matthicks.com/2018/02/scribe-2-fastest-jvm-logger-in-world.html)
3. Additional dependencies
4. Substantial cost logging method and line numbers

A few of the main features that Scribe offers:

1. Performance is a critical consideration. We leverage Macros to handle optimization of everything possible at
compile-time to avoid logging slowing down your production application. As far as we are aware, Scribe is the fastest
logging framework on the JVM.
2. Programmatic configuration. No need to be bound to configuration files to configure your logging. This means you can
rely on any configuration framework or you can configure real-time changes to your logging in your production environment.
This particularly comes in handy if you need to enable debug logging on something going wrong in production. No need to
restart your server, simply provide a mechanism to modify the logging configuration in real-time.
3. Clean logging. Macros allow us to introduce logging into a class via an import instead of a mix-in or unnecessary
setup code.
4. Zero cost class, method, and line number logging built-in. Never worry about your logger working up the stack to figure
out the position of the logging statement at runtime. With Macros we determine that information at compile-time to avoid
any runtime cost.
5. Asynchronous logging support. Scribe's logger is very fast, but if real-time performance is critical, the
asynchronous logging support completely removes logging impact from your application's thread impact.

### Performance Comparison

Scribe is now the fastest logging framework in existence. Here is a chart comparing Log4J 2, Scala Logging, and Scribe:

![All Frameworks Graph](https://raw.githubusercontent.com/outr/scribe/master/work/images/2018.01.31.benchmark-all.png)

That's right. Scribe averages nearly 1.2 million logged records per second single-threaded. Log4J on the other hand
maxes out at roughly 652k per second.

In addition, to see the graph over time:

![All Frameworks Chart](https://raw.githubusercontent.com/outr/scribe/master/work/images/2018.01.31.benchmark-all-lines.png)

For more information [check out the blog post](http://www.matthicks.com/2018/02/scribe-2-fastest-jvm-logger-in-world.html)

## Library Dependencies

No matter how simple or straight-forward a library might be, its dependencies can quickly add a lot of bloat to your
project. This is why in Scribe we have absolutely no external dependencies for the core library for JVM and Native.
However, to properly support dates and times we include a couple of Scala.js libraries for time and locale support.

### scribe-slf4j
* slf4j-api

### scribe-slack
* gigahorse-asynchttpclient (https://github.com/eed3si9n/gigahorse)
* upickle (https://github.com/lihaoyi/upickle-pprint)

## SBT Configuration ##

Scribe is published to Sonatype OSS and Maven Central and supports JVM and Scala.js with 2.11 and 2.12 and Scala Native
with 2.11:

```
libraryDependencies += "com.outr" %% "scribe" % "2.2.1"   // Scala
libraryDependencies += "com.outr" %%% "scribe" % "2.2.1"  // Scala.js / Scala Native / Cross-project
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
  myString.updateLogger(_.withHandler(writer = FileWriter.daily(), minimumLevel = Level.Debug))
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

By default all loggers have a direct parent of `Logger.root`. It is this logger that has the default console writer.

### Logging to a File ###

The following will add a new `LogHandler` to the specified `logger` to append all `Debug` logs and above to a daily file.

```scala
Logger.update(loggerName) { l =>      // Gives you the current logger by name to allow immutable modification
  l.withHandler(minimumLevel = Level.Debug, writer = FileWriter.daily())
}
```

All future references to `loggerName` will include the new handler.

### Configuring the Logger ###

A `Logger` is actually just a case class with some additional functionality added on. The `Logger` contains `parentName`,
`modifiers`, `handlers`, and `overrideClassName`.

`parentName: Option[String]`: The parent logger that will receive all logging events this logger receives. Defaults to
`Some(Logger.rootName)`. During configuration this can be disconnected with `logger.orphan()` or set to have a different
parent name with `logger.withParent(parentName)`.

`modifiers: List[LogModifier]`: A `LogModifier` has a priority (to determine the order in which they will be invoked)
and take a `LogRecord` and return an `Option[LogRecord]`. This gives the modifier the ability change the record (for
example, to boost the value or modify the message), or even to filter out records altogether by returning `None`. There
are many pre-defined `LogModifier` helper classes, but it's trivial to define your own as needed.

`handlers: List[LogHandler]`: A `LogHandler` receives `LogRecord`s after they have been processed sequentially by
`modifiers` and if the result of the `modifiers` is non empty, will be passed along to each `LogHandler`. The primary
use-case of `LogHandler` is to output the records (to the console, a file, etc.), but it is a flexible and simple
interface that can be extended to fulfill any need.

`overrideClassName: Option[String]`: The className is derived automatically when a `LogRecord` is created, but if this
value is set, the name can be explicitly defined for records created by this logger.

If you want to simply update my logger removing the `Logger.root` parent reference you can do so like this:

```scala
import scribe.Logging

class MyClass extends Logging {
  logger.update(_.orphan())
}
```

This will update the logger being used for this class going forward.

To change the default global log level, use:

```scala
Logger.update(Logger.rootName)(_.clearHandlers().addHandler(minimumLevel = Level.Error))
```

You can configure the output (how the log will look like) when adding a `LogHandler`. The `Formatter` companion
currently has two pre-defined scenarios (simple and default). Building your own Formatter instance is easy and efficient
with the formatter interpolator:

```scala
import scribe.format._

val myFormatter: Formatter = formatter"[$threadName] $positionAbbreviated - $message$newLine"
Logger.update(Logger.rootName) { l =>
  l.clearHandlers().withHandler(formatter = myFormatter)
}
```

This builds an efficient formatter at compile-time with the blocks you specify. This is both clean and readable.
Finally, this fully extensible as additional custom blocks can be defined by simply implementing the `FormatBlock`
interface.

### SLF4J Logger ###

If you add the `scribe-slf4j` dependency to your project Scribe will be picked up as an SLF4J implementation:

```
libraryDependencies += "com.outr" %% "scribe-slf4j" % "2.2.1"
```

Obviously this only applies to JVM as SLF4J isn't available in the browser or compiled for Scala Native. This will allow
any existing application that relies on SLF4J to log through Scribe without any additional configuration.

### Slack Logging ###

If you add the `scribe-slack` dependency to your project you can configure Scribe to log to Slack (https://slack.com/).

Again, this will only work on the JVM variant.

```
libraryDependencies += "com.outr" %% "scribe-slack" % "2.2.1"
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