[![Build Status](https://api.travis-ci.org/johnynek/fastparse-cats.svg)](https://travis-ci.org/johnynek/fastparse-cats)
[![codecov.io](http://codecov.io/github/johnynek/fastparse-cats/coverage.svg?branch=master)](http://codecov.io/github/johnynek/fastparse-cats?branch=master)
[![Latest version](https://index.scala-lang.org/johnynek/fastparse-cats/fastparse-cats-core/latest.svg?color=orange)](https://index.scala-lang.org/johnynek/fastparse-cats/fastparse-cats-core)

# fastparse-cats

Cats instances for fastparse parsers.

This gives you a lawful Monad and Alternative for fastparse Parsers. This allows you to easily
reuse generic functions from cats with fastparse.

### Quick Start

fastparse-cats supports Scala 2.11, and 2.12. It supports both the JVM
and JS platforms.

To use fastparse-cats in your own project, you can include this snippet in
your `build.sbt` file:

```scala
// use this snippet for the JVM
libraryDependencies ++= List("org.bykn" %% "fastparse-cats-core" % "0.1.0")

// use this snippet for JS, or cross-building
libraryDependencies ++= List("org.bykn" %%% "fastparse-cats-core" % "0.1.0")
```

There is also support for `fastparse-byte` by using
```scala
libraryDependencies ++= List("org.bykn" %% "fastparse-cats-byte" % "0.1.0")
```

To use the instances, make the following imports:

```scala
// get the Monad and Alternative for string Parser
import org.bykn.fastparse_cats.StringInstances._
```
or
```scala
// get the Monad and Alternative for byte Parser
import org.bykn.fastparse_cats.ByteInstances._
```

There are also utilities to at `buildParser.{ delay, suspend, pure, fromEval, fromEvalParser }` to build generic parsers lazily.
