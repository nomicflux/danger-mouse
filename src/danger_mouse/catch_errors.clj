;; # Transducer Error Handling

(ns danger-mouse.catch-errors)

(def catch-errors
  "Transducer to catch errors, capture additional info, and cache them as
   DM errors in a side channel that will be returned at the end of the reduction."
  (fn [rf]
    (let [errors (volatile! [])]
      (fn
        ([] (try (rf)
                 (catch Exception e
                   nil)))
        ([result] (try {:result (rf result)
                        :errors @errors}
                       (catch Exception e
                         {:result result
                          :errors (conj @errors {:error-msg (.getMessage e)
                                                 :error e
                                                 :input result})})))
        ([result input] (try (rf result input)
                             (catch Exception e
                               (vswap! errors conj {:error-msg (.getMessage e)
                                                    :error e
                                                    :input input})
                               result)))))))

(defn catch-errors->
  "Helper function to separate out results from errors in a collection.
   Collection is first."
  [coll & args]
  (transduce (apply comp catch-errors args) conj [] coll))

(defn catch-errors->>
  "Helper function to separate out results from errors in a collection.
   Collection is last."
  [& args-and-coll]
  (let [coll (last args-and-coll)
        args (drop-last args-and-coll)]
    (apply catch-errors-> coll args)))

(defn transduce->
  "Helper function to separate out results from errors after applying
   a transducer. Collection is first."
  [coll xform initial & args]
  (transduce (apply comp catch-errors args) xform initial coll))

(defn transduce->>
  "Helper function to separate out results from errors after applying
   a transducer. Collection is last."
  [xform initial & args-and-coll]
  (let [coll (last args-and-coll)
        args (drop-last args-and-coll)]
    (apply transduce-> coll xform initial args)))
