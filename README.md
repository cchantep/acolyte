# Acolyte/Anorm 10minutes tutorial

This tutorial is about how to use [Acolyte](http://cchantep.github.io/acolyte/) and [specs2](http://etorreborre.github.io/specs2/) to test project using [Anorm](http://www.playframework.com/documentation/2.2.x/ScalaAnorm).

## Requirements

In order to test this tutorial, you will need:

- [GIT client](http://git-scm.com/downloads), to get sources,
- Java 1.6+,
- [SBT](http://www.scala-sbt.org/), to build it.

## For the impatient: go to sources

If you want to look at tutorial sources -- right now --, you can clone this project:

```shell
# git clone git@github.com:cchantep/acolyte.git --branch 10m-anorm-tutorial
```

Then to run tests using Acolyte against provided Anorm use cases:

```shell
# cd 10m-anorm-tutorial
# sbt test
```

## Tutorial scenario

This documentation will describe an example of situation when Acolyte is useful.

Consider you want to manage following kind of form, to load it from database:

![Form mockup](./documentation/images/mockup.png)

There are 3 categories of information:

- section title (e.g. "Title of section #1"),
- available options, inside section,
- sub-options, linked to/depending on parent option.

It can be stored in database with a table `form_tbl` having columns like:

- `id` (primary key), unique id,
- `level` (discriminator), type/level of information (1 for section title, 2 for option, 3 for sub-option),
- `label`, title or option label.
