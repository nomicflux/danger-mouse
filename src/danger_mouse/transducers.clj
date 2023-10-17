(ns danger-mouse.transducers
  (:require [danger-mouse.utils :as utils]
            [schema.core :as s]
            [danger-mouse.schema :as dm-schema]))

(defn handle-errors-xf
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

(defn chain
  [& xfs]
  (apply comp (map carry-errors-xf xfs)))

(s/defn collect :- dm-schema/GroupedResults
  [& xfs]
  (fn [coll]
    (let [errors (transient [])
          handler (handle-errors-xf (fn [e]
                                      (conj! errors e)))
          successes (into []
                          (apply comp (interleave (cons handler xfs) (repeat handler)))
                          coll)]
      {:errors (persistent! errors)
       :successes successes})))
