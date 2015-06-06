(ns leiningen.expect
  (:require [cuerdas.core :as str]
            [clojure.java.io :as io]
            [example.core :refer [ex]]
            [leiningen.core.eval :refer [eval-in-project]]
            [leiningen.core.main :as main]
            expect.core))

(defn is-keyword? [s]
  (str/starts-with? s ":"))

(ex "is-keyword?"
    (is-keyword? ":parallel")
    (is-keyword? "test.*"))

(defn is-clojure? [file]
  (-> file .getName (str/ends-with? ".clj")))

(ex "is-clojure?"
    (is-clojure? (io/file "file.clj"))
    (is-clojure? (io/file "/tmp")))

(defn parse-args [[fst :as args]]
  (let [ns-pattern (when (and fst (not (is-keyword? fst)))
                     (symbol fst))
        kws (->> args
                 (filter is-keyword?)
                 (map #(str/slice % 1))
                 (mapv keyword))]
    [ns-pattern kws]))

(ex "parse-args"
    (parse-args (list ":parallel" ":ex"))
    (parse-args (list "test.*" ":parallel" ":ex"))
    (parse-args (list "test.*" ":parallel"))
    (parse-args (list)))

(defn find-clj-files [path]
  (for [file (file-seq (io/file path))
        :when (is-clojure? file)]
    file))

(defn parallel-ns? [[par par-level]]
  (and (= par :parallel)
       (or (= par-level :ns) (nil? par-level))))

(ex "parallel-ns?"
    (parallel-ns? [:parallel])
    (parallel-ns? [:parallel :ns])
    (parallel-ns? [:parallel :ex]))

(defn parallel-ex? [[par par-level]]
  (and (= par :parallel) (= par-level :ex)))

(ex "parallel-ex?"
    (parallel-ex? [:parallel :ex])
    (parallel-ex? [:parallel])
    (parallel-ex? [:parallel :ns]))

(defn path->ns [path]
  (-> path
      (str/strip-suffix ".clj")
      (str/replace "/" ".")
      (str/strip-prefix ".")
      (str/replace "_" "-")
      symbol))

(ex "path->ns"
    (path->ns "/path/to/some_file.clj"))

(defn file->ns [path-prefix file]
  (-> file
      .toString
      (str/strip-prefix path-prefix)
      path->ns))

(ex "file->ns"
    (file->ns "/path" (io/file "/path/to/some_file.")))

(defn load-files [path-prefix files]
  (let [file->ns' (partial file->ns path-prefix)]
    (for [file files
          :let [ns-sym (file->ns' file)]]
      `(require '~ns-sym))))

(ex "load-files"
    (load-files "/path/to/test"
                [(io/file "/path/to/test/my_file.clj")
                 (io/file "/path/to/test/dir/my_file.clj")]))

(defn find-test-files [project]
  (for [test-path (:test-paths project)]
    [test-path (find-clj-files test-path)]))

(defn mk-require-forms [project]
  (->> (find-test-files project)
       (mapcat (fn [[test-path test-files]]
                 (load-files test-path test-files)))))

(defn mk-init-form [project]
  (let [require-forms (mk-require-forms project)]
    `(do (require '~'expect.core) ~@require-forms)))

(defn mk-exit-fn [project]
  (if (or (:eval-in-leiningen project)
          (= (:eval-in project) :leiningen))
    `main/exit
    `(fn []
       (shutdown-agents)
       (System/exit 0))))

(defn mk-run-form [project args]
  (let [[ns-pattern kws] (parse-args args)
        form (if ns-pattern
               (cond (parallel-ns? kws)
                     `(~'expect.core/prun-ns# ~ns-pattern :level :ns)
                     (parallel-ex? kws)
                     `(~'expect.core/prun-ns# ~ns-pattern :level :ex)
                     :else `(~'expect.core/run-ns# ~ns-pattern))
               (cond (parallel-ns? kws)
                     `(expect.core/prun-all :level :ns)
                     (parallel-ex? kws)
                     `(expect.core/prun-all :level :ex)
                     :else `(expect.core/run-all)))
        exit-fn (mk-exit-fn project)]
    `(do ~form (~exit-fn))))

(ex "mk-run-form"
    (mk-run-form {} (list "test.*" ":parallel" ":ex"))
    (mk-run-form {} (list "test.*" ":parallel"))
    (mk-run-form {} (list "test.*"))
    (mk-run-form {} (list ":parallel" ":ns"))
    (mk-run-form {} (list ":parallel" ":ex"))
    (mk-run-form {} (list)))

(defn expect
  "Main entry point."
  [project & args]
  (let [run-form (mk-run-form project args)
        init-form (mk-init-form project)]
    (eval-in-project project run-form init-form)))
