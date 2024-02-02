(ns danger-mouse.threading
  (:require [danger-mouse.schema :as dm-schema]
            [schema.core :as s]))

(defmacro update-errors->>
  [& body-and-coll]
  (let [coll (last body-and-coll)
        body (drop-last body-and-coll)]
    `(update ~coll :errors #(->> % ~@body))))

(defmacro update-result->>
  [& body-and-coll]
  (let [coll (last body-and-coll)
        body (drop-last body-and-coll)]
    `(update ~coll :result #(->> % ~@body))))

;; TODO: This has the result in the first position, but still
;; assumes that the functions passed in to update need the argument
;; in last position. This has been my most common use case so far,
;; but at least the naming needs to be cleared up.
(defmacro update-errors->
  [coll & body]
  `(update ~coll :errors #(->> % ~@body)))

(defmacro update-result->
  [coll & body]
  `(update ~coll :result #(->> % ~@body)))
