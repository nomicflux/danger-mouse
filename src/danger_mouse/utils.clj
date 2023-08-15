(ns danger-mouse.utils
  (:require [danger-mouse.schema :as dm-schema]
            [schema.core :as schema]))

(schema/defn collect-results-map :- dm-schema/GroupedResults
  [xs :- [dm-schema/Result]]
  (->> xs
       (group-by dm-schema/result-status)
       (map (fn [[k vs]] [k (map dm-schema/get-result vs)]))
       (into {})))

(schema/defn flatten :- dm-schema/Result
  [x]
  (loop [{::dm-schema/keys [error success] :as y} x
         errored false]
    (cond
      error (recur error true)
      success (recur success errored)
      :else (if errored
              (dm-schema/as-error y)
              (dm-schema/as-success y)))))
