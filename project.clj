(defproject lein-expect "0.1.0"
  :description ""
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-maven-s3-wagon "0.2.3"]]
  :dependencies [[expect "0.1.0"]
                 [cuerdas "0.3.0"]
                 [com.roomkey/example "0.3.0+repl"]]
  :eval-in-leiningen true
  :repositories {"rk-public" {:url "http://rk-maven-public.s3-website-us-east-1.amazonaws.com/releases/"}
                 "releases" {:url "s3://rk-maven/releases/"}})
