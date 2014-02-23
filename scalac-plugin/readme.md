# Acolyte Scalac plugin

## Match component

Scala pattern matching: case class | extractor
Extract created with arg -> Stable identifier (ex regexp)
-> ~(extractorFactory[, bindings])

```scala
case class Regex(e: String) {
  lazy val re = e.r
  def unapplySeq(target: Any): Option[List[String]] = re.unapplySeq(target)
}
```

```scala
str match {
  case ~(Regex("^a.*"))                      ⇒ 1
  case ~(Regex("# ([A-Z]+).*"), a)           ⇒ 2
  case ~(Regex("([0-9]+);([a-z]+)"), (a, b)) ⇒ 3
  case _                                     ⇒ 4
}
```

## Usage

1.0.14-SNAPSHOT
https://raw.github.com/applicius/mvn-repo/master/snapshots/

## SBT usage

http://www.scala-sbt.org/0.12.3/docs/Detailed-Topics/Compiler-Plugins.html

```scala
autoCompilerPlugins := true

addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % "VERSION")

scalacOptions += "-P:acolyte:debug"
```

## Maven usage

http://scala-tools.org/mvnsites/maven-scala-plugin/example_scalac_plugins.html

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
              <artifactId>scalac-plugin_2.10</artifactId>
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

-P:acolyte:debug

## Compilation errors

```
[error] /path/to/file.scala#refactored-match-M:1: Compilation error.
[error] Error details.
[error] val Xtr1 = B() // generated from ln L, col C
```

```
[error] /path/to/file.scala#refactored-match-M:1: value Xtr0 is not a case class constructor, nor does it have an unapply/unapplySeq method
[error] case Xtr1((a @ _)) => Nil // generated from ln L, col C
```
