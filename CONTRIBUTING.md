# Contributing to Acolyte

Thanks for your interest in contributing to Acolyte!

Acolyte is an open-source project providing mock and testing drivers for JDBC and MongoDB, with integrations for JVM and Scala applications.

Contributions are welcome, whether they are bug fixes, improvements, documentation, tests, or new features.

## Getting started

Clone the repository and enter the project directory:

```bash
git clone https://github.com/cchantep/acolyte.git
cd acolyte
```

Acolyte is built with [sbt](https://www.scala-sbt.org/).

The repository is organized into several modules, including:

- `jdbc-driver` — core JDBC driver
- `jdbc-java8` — Java 8 support
- `jdbc-scala` — Scala API and DSL
- `play-jdbc` — Play JDBC integration
- `reactive-mongo-core` / `reactive-mongo` — ReactiveMongo support
- `play-reactive-mongo` — Play ReactiveMongo integration
- `jdbc-mcp` — JDBC MCP support
- `studio` — Acolyte Studio

## Building and testing

Run the complete test suite with:

```bash
sbt test
```

For a specific module, use its sbt project scope, for example:

```bash
sbt jdbcDriver/test
```

You can also run a specific test:

```bash
sbt 'jdbcDriver/testOnly fully.qualified.TestName'
```

When changing a module, running its tests during development is usually sufficient. Before submitting a pull request, run the complete test suite when practical.

## Code style

Acolyte uses Scalafmt for Scala formatting and Scalafix for some source-level checks.

Before submitting a change, format the affected sources using the project's configured formatting tasks.

Please follow the existing style of the surrounding code. In particular:

- Prefer simple and explicit designs.
- Avoid unnecessary abstractions.
- Keep public APIs small and focused.
- Preserve compatibility with existing APIs unless a breaking change is intentional.
- Add tests for behaviour that is introduced or changed.

For Java and Scala code, use the conventions already established in the corresponding module rather than introducing a different style.

## Tests

Tests are an important part of Acolyte contributions.

### Bug fixes

A bug fix should normally include a regression test demonstrating the problem.

The test should fail before the fix and pass after it whenever practical.

### New features

New functionality should be covered by tests, including relevant edge cases.

For changes to JDBC behaviour, tests should preferably exercise the behaviour through the public JDBC API rather than relying unnecessarily on implementation details.

### Compatibility

Acolyte is used as a library. Changes should therefore consider:

- source compatibility;
- binary compatibility;
- behavioural compatibility;
- supported Java and Scala versions.

Avoid removing or changing existing public APIs unless there is a clear reason to do so.

## Pull requests

For non-trivial changes, it is usually recommended to **discuss the proposed change before opening a pull request**.

Opening an issue or starting a discussion beforehand can help clarify the expected behaviour and implementation approach, and avoid spending time on a change that may not fit the project's direction.

This is particularly useful for:

- new features;
- changes to public APIs;
- changes affecting several modules;
- changes involving architectural or design decisions.

For small bug fixes, documentation updates, or straightforward improvements, a pull request can generally be opened directly when the intent and scope are clear.

Before opening a pull request:

1. Make sure the change is focused on a single issue or improvement.
2. Add or update tests as appropriate.
3. Make sure the affected module compiles and its tests pass.
4. Format the changed source files.
5. Update documentation when the public behaviour or API changes.
6. Keep the commit history reasonably focused.

A pull request description should explain:

- **what** changed;
- **why** it changed;
- how the change was tested;
- any compatibility or migration considerations.

For bug fixes, including a small reproducible example or description of the failing case is particularly useful.

## Issues

Before opening a new issue, please check whether the problem has already been reported.

For a bug report, include enough information to reproduce the problem, such as:

- Acolyte version or commit;
- Java, Scala and/or framework versions;
- affected module;
- relevant configuration;
- a minimal reproducer when possible;
- expected behaviour;
- actual behaviour;
- relevant error messages or stack traces.

For feature requests, explain the use case and why the proposed behaviour would be useful. Concrete examples are particularly helpful.

## Documentation

Documentation improvements are welcome.

When changing a public API or user-visible behaviour, update the relevant documentation and examples where appropriate.

Keep examples small and runnable where possible.

## Commit messages

There is no strict commit-message format. Prefer concise messages describing the change rather than the implementation details.

For example:

```text
Fix generated keys handling for prepared statements
```

or:

```text
Add support for nullable JDBC parameters
```

## License

By contributing to Acolyte, you agree that your contributions will be distributed under the same license as the project.

Acolyte is licensed under the LGPL-2.1. See [`LICENSE.txt`](LICENSE.txt) for the complete license text.

## Questions

If you are unsure about an implementation or contribution, feel free to open an issue and describe the problem or proposed approach.

Small, focused pull requests are generally easier to review and merge.