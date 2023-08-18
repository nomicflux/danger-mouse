(ns danger-mouse.utils
  (:require [danger-mouse.schema :as dm-schema]
            [schema.core :as s]))

(s/defn collect-results-map :- dm-schema/GroupedResults
  [xs :- [dm-schema/Result]]
  (loop [[{::dm-schema/keys [error success] :as y} & ys :as all] xs
         errors (transient [])
         successes (transient [])]
    (cond
      (empty? all) {::dm-schema/errors (persistent! errors)
                    ::dm-schema/successes (persistent! successes)}
      error (recur ys (conj! errors error) successes)
      success (recur ys errors (conj! successes success))
      :else (recur ys errors (conj! successes y)))))

(s/defn normalize :- dm-schema/Result
  [x]
  (loop [{::dm-schema/keys [error success] :as y} x
         errored false]
    (cond
      error (recur error true)
      success (recur success errored)
      :else (if errored
              (dm-schema/as-error y)
              (dm-schema/as-success y)))))

(s/defn on-success :- dm-schema/Result
  [success-fn
   {::dm-schema/keys [success] :as result} :- dm-schema/Result]
  (if success
    {::dm-schema/success (success-fn success)}
    result))

(def ^:const fmap on-success)
(def ^:const map-r on-success)

(s/defn on-error :- dm-schema/Result
  [error-fn
   {::dm-schema/keys [error] :as result} :- dm-schema/Result]
  (if error
    {::dm-schema/error (error-fn error)}
    result))

(def ^:const map-l on-error)

(s/defn on-error-and-success :- dm-schema/Result
  [error-fn
   success-fn
   {::dm-schema/keys [success error]} :- dm-schema/Result]
  (if success
    {::dm-schema/success (success-fn success)}
    {::dm-schema/error (error-fn error)}))

(def ^:const bimap on-error-and-success)

(s/defn resolve-result :- s/Any
  [error-fn
   success-fn
   {::dm-schema/keys [success error] :as result} :- s/Any]
  (cond
    error   (error-fn error)
    success (success-fn success)
    :else   (success-fn result)))

(s/defn and-then :- dm-schema/Result
  [new-result-fn
   {::dm-schema/keys [success] :as result} :- dm-schema/Result]
  (if success
    (new-result-fn success)
    result))

(def ^:const flat-map and-then)

(s/defn handle-errors :- [s/Any]
  [handler :- (s/=> (s/named (s/eq nil) 'Unit) [s/Any])
   {::dm-schema/keys [errors successes]} :- dm-schema/GroupedResults]
  (handler errors)
  successes)

(s/defn try-catch* :- dm-schema/Result
  [thunk :- (s/=> s/Any)]
  (try
    {::dm-schema/success (thunk)}
    (catch Exception e
      {::dm-schema/error e})))
