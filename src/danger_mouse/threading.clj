(ns danger-mouse.threading)

(defmacro update-errors->>
  "Update the `errors` of a `GroupedResult` or a `WithErrors`. Takes the
   `coll` in the last position, and a `body` that threads `errors` in
   the last position."
  [& body-and-coll]
  (let [coll (last body-and-coll)
        body (drop-last body-and-coll)]
    `(update ~coll :errors #(->> % ~@body))))

(defmacro update-result->>
  "Update the `result` of a `GroupedResult` or a `WithErrors`. Takes the
   `coll` in the last position, and a `body` that threads `result` in
   the last position."
  [& body-and-coll]
  (let [coll (last body-and-coll)
        body (drop-last body-and-coll)]
    `(update ~coll :result #(->> % ~@body))))

;; TODO: This has the result in the first position, but still
;; assumes that the functions passed in to update need the argument
;; in last position. This has been my most common use case so far,
;; but at least the naming needs to be cleared up.

(defmacro update-errors->
  "Update the `errors` of a `GroupedResult` or a `WithErrors`. Takes the
   `coll` in the first position, and a `body` that threads `errors` in
   the last position."
  [coll & body]
  `(update ~coll :errors #(->> % ~@body)))

(defmacro update-result->
  "Update the `result` of a `GroupedResult` or a `WithErrors`. Takes the
   `coll` in the first position, and a `body` that threads `result` in
   the last position."
  [coll & body]
  `(update ~coll :result #(->> % ~@body)))
