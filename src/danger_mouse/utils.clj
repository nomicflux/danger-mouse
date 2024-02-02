;; # Utility Functions

(ns danger-mouse.utils
  (:require [danger-mouse.schema :as dm-schema]
            [schema.core :as s]))

;; ## Collection Helper

(s/defn collect-results-map :- dm-schema/GroupedResults
  "Separate out errors and result from a vector argument and return them
   in a map."
  [xs :- [s/Any]]
  (loop [[y & ys :as all] xs
         errors (transient [])
         result (transient [])]
    (cond
      (empty? all) {:errors (persistent! errors)
                    :result (persistent! result)}
      (dm-schema/is-error? y) (recur ys (conj! errors (dm-schema/get-error y)) result)
      :else (recur ys errors (conj! result y)))))

;; ## Mapping functions

(defn on-success
  "Only apply `success-fn` to a successful (i.e. unmarked) `result`."
  [success-fn
   result]
  (if (dm-schema/is-error? result)
    result
    (success-fn result)))

(defn on-error
  "Only apply `error-fn` to an error `result` (one marked with the appropriate keyword)."
  [error-fn
   result]
  (if (dm-schema/is-error? result)
    (dm-schema/as-error (error-fn (dm-schema/get-error result)))
    result))

(defn on-error-and-success
  "Apply `error-fn` if the `result` is an error, and otherwise apply `success-fn`.
   Unlike `resolve`, this leaves an error as an error."
  [error-fn
   success-fn
   result]
  (if (dm-schema/is-error? result)
    (dm-schema/as-error (error-fn (dm-schema/get-error result)))
    (success-fn result)))

(defn resolve
  "Apply `error-fn` to the error value if `result` is an error, otherwise apply `success-fn`.
   Unlike `on-error-and-success`, this will allow the user to transform an error into a success."
  [error-fn
   success-fn
   result]
  (if (dm-schema/is-error? result)
    (error-fn (dm-schema/get-error result))
    (success-fn result)))

;; ## Error handling functions

(s/defn handle-errors :- [s/Any]
  "After using `collect-results-map` to group values, this function will handle the `errors`
   portion using `handler` (which only produces side effects) and returns only the `result`."
  [handler :- (s/=> (s/named (s/eq nil) 'Unit) [s/Any])
   {:keys [errors result]} :- dm-schema/GroupedResults]
  (handler errors)
  result)

(s/defn try-catch*
  "Function version of a try-catch block. The body must be provided as a thunk to delay processing.
   Macro version in `danger-mouse.macros#try-catch`."
  [thunk :- (s/=> s/Any)]
  (try
    (thunk)
    (catch Exception e
      {::dm-schema/error e})))
