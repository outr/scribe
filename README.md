# scribe

[![CI](https://github.com/outr/scribe/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/outr/scribe/actions/workflows/ci.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/f333f6b110974985b2d1dbea39665e9e)](https://www.codacy.com/gh/outr/scribe/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=outr/scribe&amp;utm_campaign=Badge_Grade)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/scribe)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outr/scribe_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr/scribe_2.13)
[![Latest version](https://index.scala-lang.org/outr/scribe/scribe/latest.svg)](https://index.scala-lang.org/outr/scribe)
[![Javadocs](https://javadoc.io/badge/com.outr/scribe_2.13.svg)](https://javadoc.io/doc/com.outr/scribe_2.13)

Scribe is a completely different way of thinking about logging. Instead of wrapping around existing logging frameworks
and bearing their performance and design flaws, Scribe is built from the ground up to provide fast and effective logging
in Scala, Scala.js, and Scala Native without the need of configuration files or additional dependencies. All management
of logging can be handled programmatically (of course, classic logging configuration can be utilized as well if desired)
in Scala itself, giving the developer the freedom to use whatever configuration framework, if any, they should choose to
use.

## Availability

Scribe is available on the JVM, Scala.js, and ScalaNative with cross-compiling for Scala 2.12, 2.13, and 3

## Quick Start

For people that want to skip the explanations and see it action, this is the place to start!

### Dependency Configuration

```scala
libraryDependencies += "com.outr" %% "scribe" % "3.7.0"
```

For Cross-Platform projects (JVM, JS, and/or Native):

```scala
libraryDependencies += "com.outr" %%% "scribe" % "3.7.0"
```

Or, if you want interoperability with SLF4J (to allow better interoperability with existing libraries using other loggers):

```scala
libraryDependencies += "com.outr" %% "scribe-slf4j" % "3.7.0"
```

### Usage

```scala
scribe.info("Yes, it's that simple!")
```

## Why Another Logging Framework?

Yes, we know there are too many Java logging frameworks to count, and a large number of decent logging frameworks in
Scala, so why did we write yet another logging framework?  Nearly every Scala logging framework is mostly just a wrapper
around Java logging frameworks (usually SLF4J, Log4J, or Logback). This comes with a few problems:

1. No support for Scala.js
2. No support for Scala Native
3. Performance cost (Blog Post: http://www.matthicks.com/2018/02/scribe-2-fastest-jvm-logger-in-world.html)
4. Additional dependencies
5. Substantial cost logging method and line numbers
6. Lack of programmatic configuration support

A few of the main features that Scribe offers ([for a complete list](https://github.com/outr/scribe/wiki/Features)):

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

## Documentation
* [Features](https://github.com/outr/scribe/wiki/Features)
* [Performance / Benchmarks](https://github.com/outr/scribe/wiki/benchmarks)
* [Library Dependencies](https://github.com/outr/scribe/wiki/library-dependencies)
* [Getting Started](https://github.com/outr/scribe/wiki/getting-started)

## Advanced Topics
* [Asynchronous Tracing](https://github.com/outr/scribe/wiki/asynchronous-tracing)
* [Cats Effect Module](https://github.com/outr/scribe/wiki/Cats-Effect-Support)

## Optional Modules
* [SLF4J integration](https://github.com/outr/scribe/wiki/slf4j)
* [Slack integration](https://github.com/outr/scribe/wiki/slack)
* [Logstash integration](https://github.com/outr/scribe/wiki/logstash)

## Community
The best way to receive immediate feedback for any questions is via our [Gitter channel](https://gitter.im/outr/scribe)

## Acknowledgements
YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/)
and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
innovative and intelligent tools for profiling Java and .NET applications.