---
layout: page
title: Integration
subtitle: Acolyte JDBC can be used with various JVM test and persistence frameworks.
---

## Specs2

Acolyte can be used with [specs2](http://etorreborre.github.io/specs2/) to write executable specification for function accessing persistence.

Considering a sample persistence function:

```scala
object Zoo {
  trait Animal { def name: String }
  case class Bird(name: String, fly: Boolean = true) extends Animal
  case class Dog(name: String, color: String) extends Animal

  def atLocation(con: java.sql.Connection)(id: Int): Option[Animal] = {
    // Yes would be better with something like Anorm or Slick
    var stmt: java.sql.PreparedStatement = null
    var rs: java.sql.ResultSet = null

    try {
      stmt = con.prepareStatement("SELECT * FROM zoo WHERE location = ?")
      stmt.setInt(1, id)

      rs = stmt.executeQuery()
      rs.next()

      rs.getString("type") match {
        case "bird" => Some(Bird(rs.getString("name"), rs.getBoolean("fly")))
        case "dog"  => Some(Dog(rs.getString("name"), rs.getString("color")))
        case _      => None
      }
    } catch {
      case _: Throwable => sys.error("Fails to locate animate")
    } finally {
      try { rs.close() } catch { case _: Throwable => }
      try { stmt.close() } catch { case _: Throwable => }
    }
  }
}
```

Then following specification can be written, checking that query result is properly selected and mapped:

```scala
import acolyte.jdbc.Implicits._
import acolyte.jdbc.AcolyteDSL.{ // DSL
  connection, handleQuery, withQueryResult
} 
import acolyte.jdbc.RowLists.rowList5
import Zoo._

object ZooSpec extends org.specs2.mutable.Specification {
  val zooSchema = rowList5(
    classOf[String] -> "type",
    classOf[Int] -> "location",
    classOf[String] -> "name",
    classOf[Boolean] -> "fly",
    classOf[String] -> "color")

  "Dog" should {
    "be found at location 1, and be red" in withQueryResult(
      zooSchema :+ ("dog", 1, "Scooby", null.asInstanceOf[Boolean], "red")) {
        conn =>
          atLocation(conn)(1) aka "animal" must beSome(Dog("Scooby", "red"))
    }
  }

  "Ostrich" should {
    "be found at location 2" in {
      val conn = connection(handleQuery { _ =>
        zooSchema :+ ("bird", 2, "Ostrich", false, null.asInstanceOf[String])
      })

      atLocation(conn)(2) aka "animal" must beSome(Bird("Ostrich", false))
    }
  }
}
```

## JUnit

Acolyte can be used with JUnit to write test case for Java method accessing persistence:

```java
import java.util.List;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

import static org.junit.Assert.*;

import acolyte.jdbc.AbstractCompositeHandler.QueryHandler;
import acolyte.jdbc.StatementHandler.Parameter;
import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowList5;

import static acolyte.jdbc.RowList.Column;
import static acolyte.jdbc.RowLists.rowList5;

@org.junit.runner.RunWith(org.junit.runners.JUnit4.class)
public class ZooTest {
    private RowList5<String,Integer,String,Boolean,String> zooSchema =
        rowList5(Column(String.class, "type"),
                 Column(Integer.class, "location"),
                 Column(String.class, "name"),
                 Column(Boolean.class, "fly"),
                 Column(String.class, "color"));

    @org.junit.Test
    public void dogAtLocation() {
        final String handlerId = "dogTest";
        acolyte.jdbc.Driver.
            register(handlerId, acolyte.jdbc.CompositeHandler.empty().
                     withQueryDetection("^SELECT").
                     withQueryHandler(new QueryHandler() {
                             public QueryResult apply(String sql, List<Parameter> parameters) throws SQLException {
                                 return zooSchema.
                                     append("dog", 1, "Scooby", null, "red").
                                     asResult();

                             }
                         }));

        final String jdbcUrl = 
            "jdbc:acolyte:anything-you-want?handler=" + handlerId;

        try {
            final Zoo zoo = new Zoo(DriverManager.getConnection(jdbcUrl));
            
            org.junit.Assert.assertEquals("Dog should be found at location 1", 
                                          zoo.atLocation(1), 
                                          new Dog("Scooby", "red"));

        } catch (Exception e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    @org.junit.Test
    public void birdAtLocation() {
        final Connection conn = acolyte.jdbc.Driver.
            connection(acolyte.jdbc.CompositeHandler.empty().
                     withQueryDetection("^SELECT").
                     withQueryHandler(new QueryHandler() {
                             public QueryResult apply(String sql, List<Parameter> parameters) throws SQLException {
                                 return zooSchema.
                                     append("bird", 2, "Ostrich", false, null)).
                                     asResult();

                             }
                         }));

        org.junit.Assert.assertEquals("Ostrich should be found at location 2", 
                                      new Zoo(conn).atLocation(2), 
                                      new Bird("Ostrich", false));

    }
}
```

Sample zoo method could be as following:

```java
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class Zoo {
    private final Connection connection;

    public Zoo(Connection c) { this.connection = c; }

    public Animal atLocation(int id) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.connection.
                prepareStatement("SELECT * FROM zoo WHERE location = ?");

            stmt.setInt(1, id);

            rs = stmt.executeQuery();
            rs.next();

            final String type = rs.getString("type");
            
            if (type == "bird") {
                return new Bird(rs.getString("name"), rs.getBoolean("fly"));
            } else if (type == "dog") {
                return new Dog(rs.getString("name"), rs.getString("color"));
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fails to locate animate");
        } finally {
            try { rs.close(); } catch (Exception e) {}
            try { stmt.close(); } catch (Exception e) {}
        }
    }
}

interface Animal {
    public String getName();
}
class Bird implements Animal {
    public final String name;
    public final boolean fly;

    public Bird(String n, boolean f) {
        this.name = n;
        this.fly = f;
    }

    public String getName() { return this.name; }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Bird)) {
            return false;
        } 

        final Bird other = (Bird) o;

        return (((this.name == null && other.name == null) ||
                 (this.name != null && this.name.equals(other.name))) &&
                this.fly == other.fly);

    }
}
class Dog implements Animal {
    public final String name;
    public final String color;

    public Dog(String n, String c) {
        this.name = n;
        this.color = c;
    }

    public String getName() { return this.name; }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Dog)) {
            return false;
        } 

        final Dog other = (Dog) o;

        return (((this.name == null && other.name == null) ||
                 (this.name != null && this.name.equals(other.name))) &&
                ((this.color == null && other.color == null) ||
                 (this.color != null && this.color.equals(other.color))));

    }
}
```

## Anorm

Acolyte is useful to write test about persistence in projects using [Anorm](http://www.playframework.com/documentation/latest/ScalaAnorm).

*Read the [10 minutes tutorial about Acolyte with Anorm](https://github.com/cchantep/acolyte/tree/10m-anorm-tutorial#acolyteanorm-10-minutes-tutorial)*.

## Play Framework

Acolyte can be easily used with [Play](http://www.playframework.com/) test helpers.

First step is to create an application builder:

```scala
import play.api.inject.guice.GuiceApplicationBuilder

import acolyte.jdbc.StatementHandler

def fakeAppBuilder(
  h: Option[StatementHandler] = None
): GuiceApplicationBuilder => GuiceApplicationBuilder = { initial =>
  val builder = initial.load(
    new play.api.i18n.I18nModule(),
    new play.api.mvc.CookiesModule(),
    new play.api.inject.BuiltinModule()
  )

  h match {
    case Some(handler) => {
      val id = System.identityHashCode(this).toString
      acolyte.jdbc.Driver.register(id, handler)

      builder.configure(
        "db.default.driver" -> "acolyte.jdbc.Driver",
        "db.default.url" -> s"jdbc:acolyte:test?handler=${id}"
      )
    }

    case _ => builder
  }
}
```

Then Play/DB test can be performed as following:

```scala
import acolyte.jdbc.{ AcolyteDSL, QueryResult }, AcolyteDSL.handleStatement

lazy val handler = Some(handleStatement.
  withQueryDetection("^SELECT").withQueryHandler { e =>
    QueryResult.Nil // Any Acolyte result
  })

play.api.test.Helpers.running(fakeAppBuilder(handler)) { app =>
  val db = app.injector.instanceOf[play.api.db.Database]

  db.withConnection { con =>
    // Connection |con| will use provided |handler|
    // So any DB related test can be done there
  }
}
```

This can be simplified using the Acolyte Play JDBC module:

```scala
import acolyte.jdbc.{ AcolyteDSL, QueryResult }
//import acolyte.jdbc.Implicits._
import acolyte.jdbc.play.{ PlayJdbcContext, PlayJdbcDSL }

val playHandler = AcolyteDSL.handleStatement.
  withQueryDetection("^SELECT").withQueryHandler { e =>
    QueryResult.Nil // Any Acolyte result
  }

def playCtx: PlayJdbcContext = PlayJdbcDSL.withPlayDB(playHandler)

playCtx { db: play.api.db.Database =>
  ??? // the code to be tested with a Play DB
}
```

This module can be configured in your Play project as bellow.

```ocaml
libraryDependencies ++= Seq(
  "org.eu.acolyte" %% "play-jdbc" % "{{site.latest_release}}" % "test"
)
```

*See online [API documentation](https://oss.sonatype.org/service/local/repositories/releases/archive/org/eu/acolyte/play-jdbc_2.12/{{site.latest_release}}/play-jdbc_2.12-{{site.latest_release}}-javadoc.jar/!/index.html)*