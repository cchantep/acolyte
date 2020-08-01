# Acolyte Scalac plugin

Scala compiler plugin

## Match component

Scala pattern matching involves use of either case class or extractor.

When extractor needs parameters to be created, it should be declared as stable identifier before `match` block. e.g. With regular expression matching:

```scala
val Letter = "[a-zA-Z]+".r

"String" match {
  case Letter() ⇒ true
}
```

Match component included in this plugin provides syntax `~(extractorFactory[, bindings])` for rich pattern matching.

Consider following extractors, instantiated with one parameter:

```scala
case class Regex(e: String) {
  lazy val re = e.r

  def unapplySeq(target: CharSequence): Option[List[String]] =
    re.unapplySeq(target)
}

case class IndexOf(ch: Char) {
  def unapply(v: String): Option[List[Int]] = {
    val is = v.foldLeft(0 → List.empty[Int]) { (st, c) ⇒
      val (i, l) = st
      (i + 1) → { if (c == ch) l :+ i else l }
    }._2

    if (is.isEmpty) None else Some(is)
  }
}
```

Then provided rich syntax can be used as following (see [complete example](./src/test/scala/acolyte/ExtractorComponentSpec.scala#L379)):

```scala
def useCase1(str: String) = str match {
  case ~(Regex("^a.*"))                        ⇒ 1 // no binding

  case ~(Regex("# ([A-Z]+).*"), a)             ⇒ 2 
  // if str == "# BCD123", then a = "BCD"

  case ~(Regex("([0-9]+);([a-z]+)"), (a, b))   ⇒ 3
  // if str == "234;xyz", then a = "234" and b = "xyz"

  case str @ ~(IndexOf('/'), a :: b :: c :: _) ⇒ 4
  // if there are exactly 3 '/' in str, 
  // matches and assign indexes to a, b and c

  case ~(Regex("^cp (.+)"), ~(Regex("([/a-z]+) ([/a-z]+)"),
    (src @ ~(IndexOf('/'), a :: b :: Nil),
      dest @ ~(IndexOf('/'), c :: _))))        ⇒ 5
  // rich syntax can be used recursively

  case _                                       ⇒ 6
}
```

It will be refactored by plugin, so that required stable identifiers will be available for matching:

```scala
val Xtr1 = Regex("^a.*")

// ...
def useCase2(str: String) = str match {
  case Xtr1() ⇒ 1 // no binding
  // ...
}
```

> Syntax like `(a, b)` (where `3` is selected) doesn't represent a tuple there, but multiple (list of) bindings.

It also works with partial function (see [more examples](./src/test/scala/acolyte/ExtractorComponentSpec.scala#L322)).

```scala
val partialFun1: String ⇒ Int = {
  case ~(Regex("^a.*"))                      ⇒ 1
  case ~(Regex("# ([A-Z]+).*"), a)           ⇒ 2 
  case ~(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ 3
  case _                                     ⇒ 4
}
// Or def partialFun1: String => Int = ...
```

It will be refactored as:

```scala
val partialFun2: String ⇒ Int = { str: String ⇒
  str match {
    case Xtr1() ⇒ 1
    // ...
  }
}
```

Anonymous partial functions are refactored too (see [more examples](./src/test/scala/acolyte/ExtractorComponentSpec.scala#L359)):

```scala
def test(s: Option[String]): Option[Int] = s map {
  case ~(Regex("^a.*"))                      ⇒ 1
  case ~(Regex("# ([A-Z]+).*"), a)           ⇒ 2 
  case ~(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ 3
  case _                                     ⇒ 4
}
```

Pattern matching in `val` statement is enriched in the same way ((see [more examples](./src/test/scala/acolyte/ExtractorComponentSpec.scala#L299)).

```scala
val ~(Regex("([A-Z]+):([0-9]+)"), (tag1, priority1)) = "EN:456"
// tag1 == "EN" && priority1 == "456"
```

Such case is refactored as following:

```scala
val XtrN = Regex("([A-Z]+):([0-9]+)")
val XtrN(tag, priority) = "FR:123"
```

## Usage

> If you have another `~` symbol, it will have to be renamed at `import pkg.{ ~ ⇒ renamed }`.

### SBT usage

Scalac plugin can be used with SBT project, using its [compiler plugins support](http://www.scala-sbt.org/0.12.3/docs/Detailed-Topics/Compiler-Plugins.html):

```ocaml
autoCompilerPlugins := true

addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % "VERSION")

scalacOptions += "-P:acolyte:debug" // Optional
```

### Maven usage

Maven scala plugin [supports compiler plugin](http://scala-tools.org/mvnsites/maven-scala-plugin/example_scalac_plugins.html), so you can do:

```xml
<project>
  <!-- ... -->
  <build>
    <!-- ... -->
    <plugings>
      <plugin>
        <groupId>org.scala-tools</groupId>
        <artifactId>maven-scala-plugin</artifactId>
        <!-- ... version -->
        <configuration>
          <!-- ... -->
          <args><!-- Optional: enable debug -->
            <arg>-P:acolyte:debug</arg>
          </args>

          <compilerPlugins>
            <compilerPlugin>
              <groupId>org.eu.acolyte</groupId>
              <artifactId>scalac-plugin_2.12</artifactId>
              <version>VERSION</version>
            </compilerPlugin>
          </compilerPlugins>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

## Plugin options

There is few option for this plugin.

- `-P:acolyte:debug`: Display debug while compiling with (e.g. refactored match code).

## Compilation errors

As match component refactors `match` AST when `~(…, …)` is used, then if there is compilation error around that location will be mentioned as `/path/to/file.scala#refactored-match-M` (with `M` informational index of refactored match).

If there is an error with given extractor factory, you will get something like:

```
[error] /path/to/file.scala#refactored-match-M:1: Compilation error.
[error] Error details.
[error] val Xtr1 = B() // generated from ln L, col C
```

Comment `// generated from ln L, col C` indicates location in original code, before it gets refactored.

If there result from given extractor factory is not a valid extract, it will raise:

```
[error] /path/to/file.scala#refactored-match-M:1: value Xtr0 is not a case class constructor, nor does it have an unapply/unapplySeq method
[error] case Xtr1((a @ _)) => Nil // generated from ln L, col C
```

When using `~(Xtractor(params))`, following error can be raised if `unapply` from `Xtractor` wait at least one argument.

```
[error] /path/to/file.scala#refactored-match-M:1: not enough patterns for class Clazz offering Xtractor: expected 1, found 0
```