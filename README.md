# lein-expect

Leiningen plugin for the [expect](https://github.com/dubiousdavid/expect) unit-testing library.

## Installation

Put `[com.2tothe8th/lein-expect "0.1.0"]` in the `:plugins` vector of your `:user` profile in `~/.lein/profiles.clj`.

## Usage

All expectations in your project's `:test-paths` vector will automatically be loaded. This includes the `test` directory by default.

Test expectations in all namespaces.

```shell
$ lein expect
```

Test expectations in the namespaces that match a [glob](http://en.wikipedia.org/wiki/Glob_%28programming%29) pattern.

```shell
$ lein expect my-ns.*
```

Test expectations in parallel (default is a thread per namespace). Optionally specify a thread per expectation.

```shell
$ lein expect :parallel
$ lein expect my-ns.* :parallel
$ lein expect :parallel :ex
```
