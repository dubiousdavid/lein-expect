(defproject com.2tothe8th/lein-expect "0.1.0"
  :description "Lein plugin for expect unit-testing library."
  :url "https://github.com/dubiousdavid/lein-expect"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.2tothe8th/expect "0.1.0"]
                 [cuerdas "0.3.0"]
                 [com.2tothe8th/example "0.3.0"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}}
  :eval-in-leiningen true)
