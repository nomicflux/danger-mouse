(defproject danger-mouse "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [prismatic/schema "1.4.1"]
                 [criterium "0.4.6"]]
  :plugins [[jonase/eastwood "1.4.0"]
            [lein-marginalia "0.9.1"]]
  :repl-options {:init-ns danger-mouse.catch-errors})
