(ns danger-mouse.utils
  (:require [danger-mouse.schema :as dm-schema]
            [schema.core :as s]))

(s/defn collect-results-map :- dm-schema/GroupedResults
  [xs :- [s/Any]]
  (loop [[y & ys :as all] xs
         errors (transient [])
         successes (transient [])]
    (cond
      (empty? all) {::dm-schema/errors (persistent! errors)
                    ::dm-schema/successes (persistent! successes)}
      (dm-schema/is-error? y) (recur ys (conj! errors (::dm-schema/error y)) successes)
      :else (recur ys errors (conj! successes y)))))

(defn on-success
  [success-fn
   result]
  (if (dm-schema/is-error? result)
    result
    (success-fn result)))

(defn on-error
  [error-fn
   result]
  (if (dm-schema/is-error? result)
    (dm-schema/as-error (error-fn (dm-schema/get-error result)))
    result))

(defn on-error-and-success
  [error-fn
   success-fn
   result]
  (if (dm-schema/is-error? result)
    (dm-schema/as-error (error-fn (dm-schema/get-error result)))
    (success-fn result)))

(defn resolve
  [error-fn
   success-fn
   result]
  (if (dm-schema/is-error? result)
    (error-fn (dm-schema/get-error result))
    (success-fn result)))

(s/defn handle-errors :- [s/Any]
  [handler :- (s/=> (s/named (s/eq nil) 'Unit) [s/Any])
   {::dm-schema/keys [errors successes]} :- dm-schema/GroupedResults]
  (handler errors)
  successes)

(s/defn try-catch*
  [thunk :- (s/=> s/Any)]
  (try
    (thunk)
    (catch Exception e
      {::dm-schema/error e})))
