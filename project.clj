(defproject toggles "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url "http://find the actual mit url"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [cheshire "5.7.0"]
                 [com.novemberain/monger "3.1.0"]]
  :main toggles.main
  :profiles {:dev {:dependencies [[clj-http "2.3.0"]
                                  [org.slf4j/slf4j-nop "1.7.12"]  ;to disable mongo driver logging during test runs. maybe there's a more elengant way around this, like configuring slf4j? TODO look it up
                                  [com.github.fakemongo/fongo "2.0.6"]]}})
