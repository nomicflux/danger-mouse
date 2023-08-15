(defproject danger-mouse "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [prismatic/schema "1.4.1"]]
  :plugins [[jonase/eastwood "1.4.0"]]
  :repl-options {:init-ns danger-mouse.catch-errors})
