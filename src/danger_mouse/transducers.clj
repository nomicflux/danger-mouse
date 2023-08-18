(ns danger-mouse.transducers
  (:require [danger-mouse.utils :as utils]
            [schema.core :as s]))

(defn handle-errors-xf
  [handler]
  (fn [next]
    (fn
      ([] (next))
      ([result] (next result))
      ([result input]
       (utils/resolve-result
        (fn [error] (handler error) result)
        (fn [success] (next result success))
        input)))))
