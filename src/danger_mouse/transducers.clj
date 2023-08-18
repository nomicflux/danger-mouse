(ns danger-mouse.transducers
  (:require [danger-mouse.utils :as utils]
            [schema.core :as s]
            [danger-mouse.schema :as dm-schema]))

(defn handle-errors-xf
  [handler]
  (fn [next]
    (fn
      ([] (next))
      ([result] (next result))
      ([result input]
       (utils/resolve
        (fn [error] (handler error) result)
        (fn [success] (next result success))
        input)))))

(defn carry-errors-xf
  [xf]
  (fn [next]
    (fn
      ([] (next))
      ([result] (next result))
      ([result input]
       (utils/resolve
        (fn [error] (next result (dm-schema/as-error error)))
        (fn [success] ((xf next) result success))
        input)))))

(defn chain
  [& xfs]
  (apply comp (map carry-errors-xf xfs)))
