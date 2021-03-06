(ns danger-mouse.catch-errors)

(defn catch-errors []
  (fn [rf]
    (let [errors (volatile! [])]
      (fn
        ([] (rf))
        ([result] {:result (rf result)
                   :errors @errors})
        ([result input] (try (rf result input)
                             (catch Exception e
                               (vswap! errors conj {:error-msg (.getMessage e)
                                                    ;; :error e
                                                    :input input})
                               result)))))))

(defn catch-errors->
  [coll & args]
  (transduce (apply comp (catch-errors) args) conj [] coll))

(defn transduce->
  [coll xform initial & args]
  (transduce (apply comp (catch-errors) args) xform initial coll))
