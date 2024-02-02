(defproject org.clojars.nomicflux/danger-mouse "0.3.0-SNAPSHOT"
  :description "Transducer friendly error-handling in Clojure"
  :url "https://github.com/nomicflux/danger-mouse"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [prismatic/schema "1.4.1"]
                 [criterium "0.4.6"]]
  :plugins [[jonase/eastwood "1.4.0"]
            [lein-marginalia "0.9.1"]]
  :deploy-repositories [["releases" :clojars]]
  :aliases {"run-all"
            ["do"
             "clean,"
             "deps,"
             "eastwood,"
             "test,"
             "marg" "-f index.html,"
             "uberjar"]}
  :repl-options {:init-ns danger-mouse.catch-errors})
