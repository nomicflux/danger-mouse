;; # Error Handling Transducers

(ns danger-mouse.transducers
  (:require [danger-mouse.utils :as utils]
            [schema.core :as s]
            [danger-mouse.schema :as dm-schema]))

;; ## Transducers
;; Caution, these functions are all experimental and do not always act as intended
;; depending on the transducers applied.

(defn handle-errors-xf
  "Handle errors as part of the transduction process via `handler`, removing them
   from later stages."
  [handler]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (utils/resolve
        (fn [error] (handler error) result)
        (fn [success] (rf result success))
        input)))))

(defn handle-and-continue-xf
  "Tranducer transformer that takes an existing transducer `xf`, and applies it to
   unmarked values while using `handler` to deal with and remove errored values."
  [handler xf]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (utils/resolve
        (fn [error] (handler error) result)
        (fn [success] ((xf rf) result success))
        input)))))

(defn carry-errors-xf
  "Propogates errors as errors, and otherwise applies the marked transducer `xf`."
  [xf]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (utils/resolve
        (fn [error] (rf result (dm-schema/as-error error)))
        (fn [success] ((xf rf) result success))
        input)))))

;; ## Transducer Helper Functions

(defn chain
  "Takes a splat of transducers `xfs` and wraps them to carry errors forward and
   otherwise act on values as normal. As no grouping is necessary, this can be used
   on arbitrary collections, including streams and infinite lists."
  [& xfs]
  (apply comp (map carry-errors-xf xfs)))

(s/defn collect :- dm-schema/GroupedResults
  "Takes a splat of transducers `xfs`. Any errors encountered will be thrown
   into a side channel `errors` and returned as part of a `GroupedResults`.
   Blocks until transduction is complete, so not appropriate for streaming."
  [& xfs]
  (fn [coll]
    (let [errors (transient [])
          handler (handle-errors-xf (fn [e]
                                      (conj! errors e)))
          result (into []
                       (apply comp (interleave (cons handler xfs) (repeat handler)))
                       coll)]
      {:errors (persistent! errors)
       :result result})))
