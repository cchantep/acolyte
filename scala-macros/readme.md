# Acolyte Scala macros

Scala macros for Acolyte.

## rmatch

Macro `rmatch`, for rich or refactored match, allow to use extractor initialized with arguments.

```scala
import acolyte.macros.{ Xt, rmatch }

val res = rmatch {
  Xt(v) match {
    case Xt(ExtractorWithParam("b"), _) => 2
    case Xt(ExtractorWithParam("c"), (d, _)) => 3
    case notRefactored => 4
  }
}

// Refactored as ...
val res = {
  val Xtr1 = ExtractorWithParam("a")
  val Xtr2 = ExtractorWithParam("b")
  v match {
    case Xtr0 => 1
    case Xtr1(_) => 2
    case Xtr2(d, _) => 3
    case notRefactored => 4
  }
}
```

*Implementation notes:*

`Xt` case class is used as marked, so that `rmatch` can identify pattern matching to be refactored. `Xt` is removed by `rmatch`, and should not be used (is useless) outside it.

Each `case Xt(extractorFactory, bindings)` is refactored with a `val` initialized with `extractorFactory`, to have a stable identifier for created extractor, and an updated `case` unapplying with this extractor.

`bindings` in `Xt` case should be either `()` (no binding), a single identifier (e.g. `_` or `a`), or multiple identifiers specified using tuple syntax (e.g. `(a, _, c)`).

If there are multiple identifiers in bindings of a `Xt` case, extractor created with given factory should implement `unapplySeq` (rather than `unapply`).

### Provided extractor factories

*Regular expression:*

```scala
import acolyte.macros.Regex

Regex(".*") // equivalent to ".*".r

import acolyte.macros.{ Xt, rmatch }

val res: Int = rmatch {
  Xt("abc") match {
    case Xt(Regex("^a"), ()) => 1 // match without binding
    case Xt(Regex("(.+)b([a-z]+)$"), (x, y)) => 2 // x == "a", y == "b"
  }
}
```