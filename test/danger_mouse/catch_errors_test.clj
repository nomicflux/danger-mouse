(ns danger-mouse.catch-errors-test
  (:require [danger-mouse.catch-errors :as sut]
            [clojure.test :refer [deftest is testing]]))

(defn throw-on-even
  [n]
  (if (even? n)
    (throw (ex-info "Even!" {:n n}))
    n))

(deftest catch-errors-test
  (is (= {:result [1 2 3 4 5 6 7 8 9 10]
          :errors []}
         (transduce (comp sut/catch-errors (map inc))
                    conj []
                    (range 0 10))))
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

(deftest catch-errors->-test
  (is (= {:result [1 3 5 7 9]
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (sut/catch-errors-> (range 0 10) (map throw-on-even))
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
             (update :errors (partial map #(dissoc % :error)))))))
