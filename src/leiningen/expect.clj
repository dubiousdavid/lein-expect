(ns leiningen.expect
  (:require [cuerdas.core :as str]
            [clojure.java.io :as io]
            [rk.example :refer [ex]]
            [leiningen.core.project :refer [merge-profiles]]
            [leiningen.core.eval :refer [eval-in-project]]
            [expect :refer [prun-ns# run-ns# prun-all run-all]]))

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
    (file->ns "/path" (io/file "/path/to/some_file.clj")))

(defn load-files* [path-prefix files]
  (let [file->ns' (partial file->ns path-prefix)]
    (for [file files]
      (file->ns' file))))

(ex "load-files*"
    (load-files* "/path/to/test"
                 [(io/file "/path/to/test/my_file.clj")
                  (io/file "/path/to/test/dir/my_file.clj")]))

(defn load-files [path-prefix files]
  (for [ns-sym (load-files* path-prefix files)]
    `(require '~ns-sym)))

(defn find-test-files [test-paths]
  (for [test-path test-paths]
    [test-path (find-clj-files test-path)]))

(defn gen-require-forms [test-paths]
  (mapcat (fn [[test-path test-files]] (load-files test-path test-files))
          (find-test-files test-paths)))

(defn expect
  ""
  [project & args]
  (let [test-paths (:test-paths project)
        test-profile {:source-paths test-paths}
        project (merge-profiles project [test-profile])
        [ns-pattern kws] (parse-args args)
        test-files (find-test-files project)
        require-forms (gen-require-forms test-paths)
        run-form `(run-all)
        init-form `(do (require '~'expect) ~@require-forms)]
    (println project)
    (println run-form)
    (println init-form)

    ;; Run tests
    (if ns-pattern
      (cond (parallel-ns? kws)
            (prun-ns# ns-pattern :level :ns)
            (parallel-ex? kws)
            (prun-ns# ns-pattern :level :ex)
            :else (run-ns# ns-pattern))
      (cond (parallel-ns? kws)
            (prun-all :level :ns)
            (parallel-ex? kws)
            (prun-all :level :ex)
            :else (do (println "got here") (eval-in-project project run-form init-form))))))
