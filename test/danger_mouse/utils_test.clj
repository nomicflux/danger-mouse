(ns danger-mouse.utils-test
  (:require [danger-mouse.utils :as sut]
            [clojure.test :refer [deftest is testing]]
            [danger-mouse.schema :as dm-schema]))

(deftest collect-results-map-test
  (is (= {::dm-schema/errors []
          ::dm-schema/successes []}
         (sut/collect-results-map [])))
  (is (= {::dm-schema/successes [1 2 3]
          ::dm-schema/errors []}
         (sut/collect-results-map [1 2 3])))
  (is (= {::dm-schema/successes [1 2 3]
          ::dm-schema/errors []}
         (sut/collect-results-map [1
                                   2
                                   3])))
  (is (= {::dm-schema/errors [1 2 3]
          ::dm-schema/successes []}
         (sut/collect-results-map [(dm-schema/as-error 1)
                                   (dm-schema/as-error 2)
                                   (dm-schema/as-error 3)])))
  (is (= {::dm-schema/errors [1]
          ::dm-schema/successes [2 3]}
         (sut/collect-results-map [(dm-schema/as-error 1)
                                   2
                                   3]))))

(deftest try-catch-test
  (is (= 1
         (sut/try-catch* (fn [] 1))))
  (is (= {::dm-schema/error ["oops" {:data :something}]}
         (-> (sut/try-catch* (fn [] (throw (ex-info "oops" {:data :something}))))
             (update ::dm-schema/error (juxt ex-message ex-data))))))

(deftest mapping-tests
  (testing "mapping success"
    (is (= 2
           (sut/on-success inc 1)))
    (is (= (dm-schema/as-error 1)
           (sut/on-success inc (dm-schema/as-error 1)))))

  (testing "mapping error"
    (is (= 1
           (sut/on-error inc 1)))
    (is (= (dm-schema/as-error 2)
           (sut/on-error inc (dm-schema/as-error 1)))))

  (testing "mapping both"
    (is (= 2
           (sut/on-error-and-success dec inc 1)))
    (is (= (dm-schema/as-error 0)
           (sut/on-error-and-success dec inc {::dm-schema/error 1})))))

(deftest handle-errors-test
  (let [handled-errors (atom [])]
    (is (= [1 2 3]
          (sut/handle-errors
           (fn [errors] (reset! handled-errors errors))
           {::dm-schema/errors [:a :b :c]
            ::dm-schema/successes [1 2 3]})))
    (is (= [:a :b :c]
           @handled-errors))))
