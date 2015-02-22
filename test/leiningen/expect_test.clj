(ns leiningen.expect-test
  (:use midje.sweet leiningen.expect)
  (:require [clojure.java.io :as io]))

(facts "is-keyword?"
  (fact (is-keyword? ":parallel") => true)
  (fact (is-keyword? "test.*") => false))
(facts "is-clojure?"
  (fact (is-clojure? (io/file "file.clj")) => true)
  (fact (is-clojure? (io/file "/tmp")) => false))
(facts "parse-args"
  (fact (parse-args (list ":parallel" ":ex")) => [nil [:parallel :ex]])
  (fact (parse-args (list "test.*" ":parallel" ":ex")) =>
    ['test.* [:parallel :ex]])
  (fact (parse-args (list "test.*" ":parallel")) => ['test.* [:parallel]])
  (fact (parse-args (list)) => [nil []]))
(facts "parallel-ns?"
  (fact (parallel-ns? [:parallel]) => true)
  (fact (parallel-ns? [:parallel :ns]) => true)
  (fact (parallel-ns? [:parallel :ex]) => false))
(facts "parallel-ex?"
  (fact (parallel-ex? [:parallel :ex]) => true)
  (fact (parallel-ex? [:parallel]) => false)
  (fact (parallel-ex? [:parallel :ns]) => false))
(facts "path->ns"
  (fact (path->ns "/path/to/some_file.clj") => 'path.to.some-file))
(facts "file->ns"
  (fact (file->ns "/path" (io/file "/path/to/some_file.")) => 'to.some-file.))
(facts "load-files"
  (fact (load-files "/path/to/test" [(io/file "/path/to/test/my_file.clj") (io/file "/path/to/test/dir/my_file.clj")]) =>
    (list
     (list 'clojure.core/require (list 'quote 'my-file))
     (list 'clojure.core/require (list 'quote 'dir.my-file)))))
(facts "mk-run-form"
  (fact (mk-run-form {} (list "test.*" ":parallel" ":ex")) =>
    (list
     'do
     (list 'expect/prun-ns# 'test.* :level :ex)
     (list
      (list
       'clojure.core/fn
       []
       (list 'clojure.core/shutdown-agents)
       (list 'java.lang.System/exit 0)))))
  (fact (mk-run-form {} (list "test.*" ":parallel")) =>
    (list
     'do
     (list 'expect/prun-ns# 'test.* :level :ns)
     (list
      (list
       'clojure.core/fn
       []
       (list 'clojure.core/shutdown-agents)
       (list 'java.lang.System/exit 0)))))
  (fact (mk-run-form {} (list "test.*")) =>
    (list
     'do
     (list 'expect/run-ns# 'test.*)
     (list
      (list
       'clojure.core/fn
       []
       (list 'clojure.core/shutdown-agents)
       (list 'java.lang.System/exit 0)))))
  (fact (mk-run-form {} (list ":parallel" ":ns")) =>
    (list
     'do
     (list 'expect/prun-all :level :ns)
     (list
      (list
       'clojure.core/fn
       []
       (list 'clojure.core/shutdown-agents)
       (list 'java.lang.System/exit 0)))))
  (fact (mk-run-form {} (list ":parallel" ":ex")) =>
    (list
     'do
     (list 'expect/prun-all :level :ex)
     (list
      (list
       'clojure.core/fn
       []
       (list 'clojure.core/shutdown-agents)
       (list 'java.lang.System/exit 0)))))
  (fact (mk-run-form {} (list)) =>
    (list
     'do
     (list 'expect/run-all)
     (list
      (list
       'clojure.core/fn
       []
       (list 'clojure.core/shutdown-agents)
       (list 'java.lang.System/exit 0))))))
