---
layout: page
title: Acolyte
subtitle: Acolyte is a JDBC driver designed for cases like mockup, testing, or any case you would like to be able to handle JDBC query by hand (or maybe that’s only Chmeee’s son on the Ringworld).
---

<div class="posts-list">
  {% for post in paginator.posts %}
  <article class="post-preview">
    <h2 class="post-title">{{ post.title }}</h2>

    {% if post.subtitle %}
    <h3 class="post-subtitle">
      {{ post.subtitle }}
    </h3>
    {% endif %}
  </article>
  {% endfor %}
</div>

## Motivation

Persistence layer not only apply changes and retrieve raw data. It usually gathers those data from several sources (e.g. various queries), but also converts data types (e.g. integer to boolean) and maps it to structured information.

Automated testing about that is not trivial.
Using test DB requires tools (scripts) to set up environment repeatedly, for each time tests are executed.

Considering integration testing that's fine. It's different for unit testing.

Unit tests must be isolated from each others so each unit can be validated independently.     

A unit test can alter database as executed. Thus tests coming after would have to cope with this altered environment, without asserting which one is executed first (no order assumption).

As tests can be executed in parallel while considering code accessing same data spaces. Without extra attention to isolation/transaction management, this can lead to tests conflicting between them.

With Acolyte, connection behaviour can be built, defining which statement is supported with which (query or update) result.

Each prepared connection can supports only queries and updates your code is interested in, and there is no need to simulate a whole data store structure/schema.

As soon as Acolyte connections don't rely on data store, statement executions are isolated without extra effort.

As a JDBC driver is provided you can simply update test configuration, so that Acolyte connections are resolved by persistence code without change through standard mechanisms (JDBC URL, JNDI, ...).

It also makes simple testing of DB edge cases (e.g. unrecoverable/unexpected error). It's easy to throw an exception from Acolyte connection, so that it can be validated persistence code is properly handling such case.

You can also use Acolyte to fully benefit from data access abstraction, not only not having to wait persistence (DB) being setup to code accesses, but also not having to wait persistence to code tests for access code.

You can get a quick interactive tour of Acolyte, online at [tour.acolyte.eu.org](http://tour.acolyte.eu.org).

## Usage

Acolyte is usable with any code relying on JDBC. It makes it available for any JVM language:

* vanilla [Java](/java/),
* [Scala](/scala/) (with DSL),
* [Clojure](http://clojure.com), [Fredge](https://github.com/Frege/frege), ...

You can get connection defined by Acolyte using the well-known `java.sql.DriverManager.getConnection(jdbcUrl)` (see [connection management](/java/#Connection)).

```java
final String jdbcUrl = "jdbc:acolyte:anything-you-want?handler=my-unique-id";

StatementHandler handler = new CompositeHandler().
  withQueryDetection("^SELECT "). // regex test from beginning
  withQueryDetection("EXEC that_proc"). // second detection regex
  withUpdateHandler(new CompositeHandler.UpdateHandler() {
    // Handle execution of update statement (not query)
    public UpdateResult apply(String sql, List<Parameter> parameters) {
      // ...
    }
  }).withQueryHandler(new CompositeHandler.QueryHandler () {
    public QueryResult apply(String sql, List<Parameter> parameters) {
      // ...
    }
  });

// Register prepared handler with expected ID 'my-unique-id'
acolyte.jdbc.Driver.register("my-unique-id", handler);

// then when existing code do ...
Connection con = DriverManager.getConnection(jdbcUrl);

// ... Connection |con| is managed through Acolyte |handler|
```

You can use Acolyte with various JVM test and persistence frameworks (see [Integration guide](/integration/)).

With [Studio](/studio/), you can use data extracted from existing database with Acolyte handler.

_Projects using Acolyte:_

- [Play Framework](http://www.playframework.com/) Anorm ([`AnormSpec`](https://github.com/playframework/playframework/blob/master/framework/src/anorm/src/test/jdbc-scala/anorm/AnormSpec.scala)). 
- [Youtube Vitess](https://github.com/youtube/vitess).

To share questions, answers & ideas, you can go to the [mailing list](https://groups.google.com/forum/#!forum/acolyte-support).

## Requirements

* Java 1.6+

## Limitations

- Limited data type conversions.
- Binary data type are not currently supported.
- Pseudo-support for transaction.
- Currency types.

## Subprojects

- [Acolyte for ReactiveMongo](/reactive-mongo/): Library to unit test persistence based on [ReactiveMongo](http://reactivemongo.org/).
- [Scalac plugin](/scalac-plugin/): A scalac plugin to ease the pattern matching for testing.
- [Acolyte Studio](/studio/): Application with CLI and GUI which is useful when you already have a database and want tests to use data extracted from there.
