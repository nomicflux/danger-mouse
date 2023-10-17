;; # Transducer Error Handling

(ns danger-mouse.catch-errors)

(defn catch-errors
  "Transducer to catch errors, capture additional info, and cache them as
   DM errors in a side channel that will be returned at the end of the reduction."
  []
  (fn [rf]
    (let [errors (volatile! [])]
      (fn
        ([] (rf))
        ([result] {:result (rf result)
                   :errors @errors})
        ([result input] (try (rf result input)
                             (catch Exception e
                               (vswap! errors conj {:error-msg (.getMessage e)
                                                    :error e
                                                    :input input})
                               result)))))))

(defn catch-errors->
  "Helper function to separate out results from errors in a collection."
  [coll & args]
  (transduce (apply comp (catch-errors) args) conj [] coll))

(defn transduce->
  "Helper function to separate out results from errors after applying
   a transducer."
  [coll xform initial & args]
  (transduce (apply comp (catch-errors) args) xform initial coll))
