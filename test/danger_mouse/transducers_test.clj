(ns danger-mouse.transducers-test
  (:require [danger-mouse.transducers :as sut]
            [clojure.test :refer [deftest is testing]]
            [danger-mouse.schema :as dm-schema]
            [danger-mouse.utils :as utils]))

(deftest handle-errors-xf
  (testing "all successes first"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [2 3]
             (into []
                   (comp (sut/handle-errors-xf handler) (map inc))
                   [(dm-schema/as-success 1) (dm-schema/as-success 2)])))
      (is (= []
             (persistent! errors)))))
  (testing "all successes last"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [2 3]
             (into []
                   (comp (map (partial utils/on-success inc)) (sut/handle-errors-xf handler))
                   [(dm-schema/as-success 1) (dm-schema/as-success 2)])))
      (is (= []
             (persistent! errors)))))

  (testing "all errors first"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= []
             (into []
                   (comp (sut/handle-errors-xf handler) (map inc))
                   [(dm-schema/as-error 1) (dm-schema/as-error 2)])))
      (is (= [1 2]
             (persistent! errors)))))
  (testing "all errors last"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= []
             (into []
                   (comp (map (partial utils/on-success inc)) (sut/handle-errors-xf handler))
                   [(dm-schema/as-error 1) (dm-schema/as-error 2)])))
      (is (= [1 2]
             (persistent! errors)))))

  (testing "mixed first"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [3]
             (into []
                   (comp (sut/handle-errors-xf handler) (map inc))
                   [(dm-schema/as-error 1) (dm-schema/as-success 2)])))
      (is (= [1]
             (persistent! errors)))))
  (testing "mixed last"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [3]
             (into []
                   (comp (map (partial utils/on-success inc)) (sut/handle-errors-xf handler))
                   [(dm-schema/as-error 1) (dm-schema/as-success 2)])))
      (is (= [1]
             (persistent! errors)))))

  (testing "causative"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [2]
             (into []
                   (comp (map (fn [n] (if (even? n)
                                       (dm-schema/as-success n)
                                       (dm-schema/as-error n))))
                         (sut/handle-errors-xf handler))
                   [1 2])))
      (is (= [1]
             (persistent! errors))))))

(deftest carry-errors-xf-test
  (testing "works with plain values"
    (is (= [2 3 4]
           (into [] (into [] (sut/carry-errors-xf (map inc)) [1 2 3])))))
  (testing "works with successes"
    (is (= [2 3]
           (into [] (into []
                          (sut/carry-errors-xf (map inc))
                          [(dm-schema/as-success 1) (dm-schema/as-success 2)])))))
  (testing "works with errors"
    (is (= [(dm-schema/as-error 1) (dm-schema/as-error 2)]
           (into [] (into []
                          (sut/carry-errors-xf (map inc))
                          [(dm-schema/as-error 1) (dm-schema/as-error 2)])))))
  (testing "works with multiple xfs"
    (is (= [2 (dm-schema/as-error 2) (dm-schema/as-error 3)]
           (into [] (into []
                          (sut/carry-errors-xf (comp (map inc) (filter even?)))
                          [(dm-schema/as-success 1)
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
                           (dm-schema/as-success 4)]))))
    (is (= [(dm-schema/as-error 2)
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
            50]
           (into [] (into []
                          (sut/carry-errors-xf (comp (map inc)
                                                     (map (fn [x] (if (even? x)
                                                                   (dm-schema/as-error x)
                                                                   (dm-schema/as-success x))))
                                                     (sut/carry-errors-xf (map (partial * 10)))))
                          [(dm-schema/as-success 1)
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
                           (dm-schema/as-success 4)]))))
    ))
