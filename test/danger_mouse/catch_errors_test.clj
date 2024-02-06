(ns danger-mouse.catch-errors-test
  (:require [danger-mouse.catch-errors :as sut]
            [clojure.test :refer [deftest is testing]]))

(defn throw-on-even
  [n]
  (if (even? n)
    (throw (ex-info "Even!" {:n n}))
    n))

(deftest catch-errors-test
  (testing "no errors"
    (is (= {:result [1 2 3 4 5 6 7 8 9 10]
           :errors []}
          (transduce (comp sut/catch-errors (map inc))
                     conj []
                     (range 0 10)))))
  (testing "errors from transducer"
    (is (= {:result [1 3 5 7 9]
           :errors
           [{:error-msg "Even!" :input 0}
            {:error-msg "Even!" :input 2}
            {:error-msg "Even!" :input 4}
            {:error-msg "Even!" :input 6}
            {:error-msg "Even!" :input 8}]}
          (-> (transduce (comp sut/catch-errors (map throw-on-even))
                         conj []
                         (range 0 10))
              (update :errors (partial map #(dissoc % :error)))))))
  (testing "errors from reducing function"
    (is (= {:result [1 3 5 7 9]
           :errors
           [{:error-msg "Even!" :input 1}
            {:error-msg "Even!" :input 3}
            {:error-msg "Even!" :input 5}
            {:error-msg "Even!" :input 7}
            {:error-msg "Even!" :input 9}]}
           (-> (transduce (comp sut/catch-errors (map inc))
                          (completing
                           (fn [acc next]
                             (when (even? next)
                               (throw (ex-info "Even!" {:n next})))
                             (conj acc next)))
                         []
                         (range 0 10))
              (update :errors (partial map #(dissoc % :error))))))
    (testing "partition all with arity-1 error and final arity-2 call"
      (is (= {:result [1 2 3],
             :errors
             [{:error-msg "Quadven!", :input 5}
              {:error-msg "Quadven!", :input 8}
              {:error-msg "Base!", :input [1 2 3]}]}
            (-> (transduce (comp sut/catch-errors
                                 (map inc)
                                 (partition-all 3)
                                 (mapcat identity))
                           (fn
                             ([] nil)
                             ([acc] (throw (ex-info "Base!" {:acc acc})))
                             ([acc next]
                              (when (= 0 (mod next 4))
                                (throw (ex-info "Quadven!" {:n next})))
                              (conj acc next)))
                           []
                           (range 0 10))
                (update :errors (partial map #(dissoc % :error)))))))
    (testing "partition-all with arity-1 error without final arity-2 call"
      (is (= {:result [1 2 5 6 9 10],
             :errors
             [{:error-msg "Quadven!", :input 3}
              {:error-msg "Quadven!", :input 7}
              {:error-msg "Base!", :input [1 2 5 6 9 10]}]}
            (-> (transduce (comp sut/catch-errors
                                 (map inc)
                                 (partition-all 2)
                                 (mapcat identity))
                           (fn
                             ([] nil)
                             ([acc] (throw (ex-info "Base!" {:acc acc})))
                             ([acc next]
                              (when (= 0 (mod next 4))
                                (throw (ex-info "Quadven!" {:n next})))
                              (conj acc next)))

                           []
                           (range 0 10))
                (update :errors (partial map #(dissoc % :error)))))))))

(deftest catch-errors->-test
  (is (= {:result [1 3 5 7 9]
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (sut/catch-errors-> (range 0 10) (map throw-on-even))
             (update :errors (partial map #(dissoc % :error))))))
  (is (= {:result [1 3 5 7 9]
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (sut/catch-errors-> {:result (range 0 10)
                                  :errors []}
                                 (map throw-on-even))
             (update :errors (partial map #(dissoc % :error))))))
  (is (= {:result [1 3 5 7 9]
          :errors
          [{:error-msg "Other error" :input 876}
           {:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (sut/catch-errors-> {:result (range 0 10)
                                  :errors [{:error-msg "Other error" :input 876}]}
                                 (map throw-on-even))
             (update :errors (partial map #(dissoc % :error)))))))

(deftest catch-errors->>-test
  (is (= {:result [1 3 5 7 9]
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (sut/catch-errors->> (map throw-on-even) (range 0 10))
             (update :errors (partial map #(dissoc % :error)))))))

(deftest transduce->-test
  (is (= {:result 25
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (sut/transduce-> (range 0 10) + 0 (map throw-on-even))
             (update :errors (partial map #(dissoc % :error))))))
  (is (= {:result 25
          :errors
          [{:error-msg "Even!" :input 1}
           {:error-msg "Even!" :input 3}
           {:error-msg "Even!" :input 5}
           {:error-msg "Even!" :input 7}
           {:error-msg "Even!" :input 9}]}
         (-> (sut/transduce-> (range 0 10)
                             (completing
                              (fn [x y]
                                (when (even? y)
                                  (throw (ex-info "Even!" {:n y})))
                                (+ x y)))
                              0
                              (map inc))
             (update :errors (partial map #(dissoc % :error))))))
  (is (= {:result 25
          :errors
          [{:error-msg "Even!" :input 1}
           {:error-msg "Even!" :input 3}
           {:error-msg "Even!" :input 5}
           {:error-msg "Even!" :input 7}
           {:error-msg "Even!" :input 9}]}
         (-> (sut/transduce-> {:result (range 0 10)
                               :errors []}
                             (completing
                              (fn [x y]
                                (when (even? y)
                                  (throw (ex-info "Even!" {:n y})))
                                (+ x y)))
                              0
                              (map inc))
             (update :errors (partial map #(dissoc % :error))))))
  (is (= {:result 25
          :errors
          [{:error-msg "Other error", :input 975}
           {:error-msg "Even!" :input 1}
           {:error-msg "Even!" :input 3}
           {:error-msg "Even!" :input 5}
           {:error-msg "Even!" :input 7}
           {:error-msg "Even!" :input 9}]}
         (-> (sut/transduce-> {:result (range 0 10)
                               :errors [{:error-msg "Other error" :input 975}]}
                             (completing
                              (fn [x y]
                                (when (even? y)
                                  (throw (ex-info "Even!" {:n y})))
                                (+ x y)))
                              0
                              (map inc))
             (update :errors (partial map #(dissoc % :error)))))))

(deftest transduce->>-test
  (is (= {:result 25
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (sut/transduce->> + 0 (map throw-on-even) (range 0 10))
             (update :errors (partial map #(dissoc % :error)))))))
