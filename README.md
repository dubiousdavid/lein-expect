# lein-expect

Lein plugin for the expect unit-testing library.

## Usage

Put `[lein-expect "0.1.0"]` into the `:plugins` vector of your `:user` profile.

    $ lein expect
    $ lein expect my-ns.*
    $ lein expect :parallel
    $ lein expect :parallel :ex
    $ lein expect my-ns.* :parallel
