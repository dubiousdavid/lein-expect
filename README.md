# lein-expect

Lein plugin for the [expect](https://github.com/dubiousdavid/expect) unit-testing library.

## Usage

Put `[com.2tothe8th/lein-expect "0.1.0"]` in the `:plugins` vector of your `:user` profile in `~/.lein/profiles.clj`.

Test expectations in all namespaces.

```shell
$ lein expect
```

Test expectations in the namespaces that match a glob pattern.

```shell
$ lein expect my-ns.*
```

Test expectations in parallel (default is a thread per namespace). Optionally specify a thread per expectation.

```shell
$ lein expect :parallel
$ lein expect my-ns.* :parallel
$ lein expect :parallel :ex
```
