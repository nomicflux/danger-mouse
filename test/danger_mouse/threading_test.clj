(ns danger-mouse.threading-test
  (:require [danger-mouse.threading :as sut]
            [danger-mouse.catch-errors :as dm-catch-errors]
            [clojure.test :refer [deftest is testing]]))

(defn throw-on-even
  [n]
  (if (even? n)
    (throw (ex-info "Even!" {:n n}))
    n))

(deftest update-errors->>
  (is (= {:result [1 3 5 7 9]
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (->> (range 0 10)
              (dm-catch-errors/catch-errors->> (map throw-on-even))
              (sut/update-errors->> (map #(dissoc % :error)))))))

(deftest update-result->>
  (is (= {:result [2 4 6 8 10]
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (->> (range 0 10)
              (dm-catch-errors/catch-errors->> (map throw-on-even))
              (sut/update-errors->> (map #(dissoc % :error)))
              (sut/update-result->> (map inc))))))

(deftest update-errors->
  (is (= {:result [1 3 5 7 9]
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (range 0 10)
             (dm-catch-errors/catch-errors-> (map throw-on-even))
             (sut/update-errors-> (map #(dissoc % :error)))))))

(deftest update-result->
  (is (= {:result [2 4 6 8 10]
          :errors
          [{:error-msg "Even!" :input 0}
           {:error-msg "Even!" :input 2}
           {:error-msg "Even!" :input 4}
           {:error-msg "Even!" :input 6}
           {:error-msg "Even!" :input 8}]}
         (-> (range 0 10)
             (dm-catch-errors/catch-errors-> (map throw-on-even))
             (sut/update-errors-> (map #(dissoc % :error)))
             (sut/update-result-> (map inc))))))
