;; # Transducer Error Handling

(ns danger-mouse.catch-errors)

(defn process-error
  [error input]
  {:error-msg (ex-message error)
   :error error
   :input input})

(def catch-errors
  "Transducer to catch errors, capture additional info, and cache them as
   DM errors in a side channel that will be returned at the end of the reduction."
  (fn [rf]
    (let [errors (volatile! [])]
      (fn
        ([] (try (rf)
                 (catch Exception e
                   {:result nil
                    :errors (conj @errors (process-error e nil))})))
        ([result] (try {:result (rf result)
                        :errors @errors}
                       (catch Exception e
                         {:result result
                          :errors (conj @errors (process-error e result))})))
        ([result input] (try (rf result input)
                             (catch Exception e
                               (vswap! errors conj (process-error e input))
                               result)))))))

(defn errors-coll?
  [coll]
  (and (map? coll) (= (set (keys coll)) #{:result :errors})))

(defn catch-errors->
  "Helper function to separate out results from errors in a collection.
   Collection is first."
  [coll & args]
  (let [start (if (errors-coll? coll) (:result coll) coll)
        {new-result :result new-errors :errors}
        (transduce (apply comp catch-errors args) conj [] start)]
    {:result new-result
     :errors (into (or (:errors coll) []) new-errors)}))

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
  (let [start (if (errors-coll? coll) (:result coll) coll)
        {new-result :result
         new-errors :errors}
        (transduce (apply comp catch-errors args) xform initial start)]
    {:result new-result
     :errors (into (or (:errors coll) []) new-errors)}))

(defn transduce->>
  "Helper function to separate out results from errors after applying
   a transducer. Collection is last."
  [xform initial & args-and-coll]
  (let [coll (last args-and-coll)
        args (drop-last args-and-coll)]
    (apply transduce-> coll xform initial args)))
