(ns danger-mouse.utils
  (:require [danger-mouse.schema :as dm-schema]
            [schema.core :as s]))

(s/defn collect-results-map :- dm-schema/GroupedResults
  [xs :- [dm-schema/Result]]
  (loop [[{::dm-schema/keys [error success] :as y} & ys :as all] xs
         errors (transient [])
         successes (transient [])]
    (cond
      (empty? all) {::dm-schema/error (persistent! errors)
                    ::dm-schema/success (persistent! successes)}
      error (recur ys (conj! errors error) successes)
      success (recur ys errors (conj! successes success))
      :else (recur ys errors (conj! successes y)))))

(s/defn flatten :- dm-schema/Result
  [x]
  (loop [{::dm-schema/keys [error success] :as y} x
         errored false]
    (cond
      error (recur error true)
      success (recur success errored)
      :else (if errored
              (dm-schema/as-error y)
              (dm-schema/as-success y)))))
