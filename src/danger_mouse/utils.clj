(ns danger-mouse.utils
  (:require [danger-mouse.schema :as dm-schema]
            [schema.core :as schema]))

(schema/defn collect-results-map :- dm-schema/GroupedResults
  [xs :- [dm-schema/Result]]
  (->> xs
       (group-by dm-schema/result-status)
       (map (fn [[k vs]] [k (map dm-schema/get-result vs)]))
       (into {})))
