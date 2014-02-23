# Acolyte Scala compiler plugin

## Match component

Scala pattern matching: case class | extractor
Extract created with arg -> Stable identifier (ex regexp)
-> ~(..., ...)

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

## SBT usage

```scala
autoCompilerPlugins := true

addCompilerPlugin("org.eu.acolyte" %% "scala-compiler-plugin" % "1.0.14")

scalacOptions += "-P:acolyte:debug"
```