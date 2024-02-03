(ns danger-mouse.transducers-test
  (:require [danger-mouse.transducers :as sut]
            [clojure.test :refer [deftest is testing]]
            [danger-mouse.schema :as dm-schema]
            [danger-mouse.utils :as utils]))

(defn throw-on-even
  [n]
  (if (even? n)
    (throw (ex-info "Even!" {:n n}))
    n))

(deftest contain-errors-xf
  (testing "all successes"
    (is (= [0 1 2 3 4]
          (into []
                sut/contain-errors-xf
                (range 0 5)))))
  (testing "all failures"
    (is (= [(dm-schema/as-error {:error-msg "Even!", :input 0})
            (dm-schema/as-error {:error-msg "Even!", :input 1})
            (dm-schema/as-error {:error-msg "Even!", :input 2})
            (dm-schema/as-error {:error-msg "Even!", :input 3})
            (dm-schema/as-error {:error-msg "Even!", :input 4})]
           (->> (range 0 5)
                (into []
                  (sut/chain sut/contain-errors-xf
                             (map #(* % 2))
                             (map throw-on-even)))
                (map (partial utils/on-error #(dissoc % :error)))))))
  (testing "mixed"
    (is (= [(dm-schema/as-error {:error-msg "Even!", :input 0})
            1
            (dm-schema/as-error {:error-msg "Even!", :input 2})
            3
            (dm-schema/as-error {:error-msg "Even!", :input 4})]
           (->> (range 0 5)
                (into []
                  (sut/chain sut/contain-errors-xf
                             (map throw-on-even)))
                (map (partial utils/on-error #(dissoc % :error))))))))

(deftest handle-errors-xf
  (testing "all successes first"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [2 3]
             (into []
                   (comp (sut/handle-errors-xf handler) (map inc))
                   [1 2])))
      (is (= []
             (persistent! errors)))))
  (testing "all successes last"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [2 3]
             (into []
                   (comp (map (partial utils/on-success inc)) (sut/handle-errors-xf handler))
                   [1 2])))
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
                   [(dm-schema/as-error 1) 2])))
      (is (= [1]
             (persistent! errors)))))
  (testing "mixed last"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [3]
             (into []
                   (comp (map (partial utils/on-success inc)) (sut/handle-errors-xf handler))
                   [(dm-schema/as-error 1) 2])))
      (is (= [1]
             (persistent! errors)))))

  (testing "causative"
    (let [errors (transient [])
          handler (fn [e] (conj! errors e))]
      (is (= [2]
             (into []
                   (comp (map (fn [n] (if (even? n)
                                       n
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
                          [1 2])))))
  (testing "works with errors"
    (is (= [(dm-schema/as-error 1) (dm-schema/as-error 2)]
           (into [] (into []
                          (sut/carry-errors-xf (map inc))
                          [(dm-schema/as-error 1) (dm-schema/as-error 2)])))))
  (testing "works with multiple xfs"
    (is (= [2 (dm-schema/as-error 2) (dm-schema/as-error 3)]
           (into [] (into []
                          (sut/carry-errors-xf (comp (map inc) (filter even?)))
                          [1
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
                           4]))))
    (is (= [(dm-schema/as-error 2)
            (dm-schema/as-error 2)
            (dm-schema/as-error 3)
            50]
           (into [] (into []
                          (sut/carry-errors-xf (comp (map inc)
                                                     (map (fn [x] (if (even? x)
                                                                   (dm-schema/as-error x)
                                                                   x)))
                                                     (sut/carry-errors-xf (map (partial * 10)))))
                          [1
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
                           4]))))))

(deftest chain-test
  (testing "map"
    (is (= [(dm-schema/as-error 2)
            (dm-schema/as-error 2)
            (dm-schema/as-error 3)
            50]
           (into [] (into []
                          (sut/chain (map inc)
                                     (map (fn [x] (if (even? x)
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (map (partial * 10)))
                          [1
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
                           4])))))
  (testing "filter"
    (is (= [(dm-schema/as-error 4)
            50]
           (into [] (into []
                          (sut/chain (map inc)
                                     (filter #(> % 3))
                                     (map (fn [x] (if (even? x)
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (map (partial * 10)))
                          [1
                           2
                           3
                           4]))))
    (is (= [(dm-schema/as-error 2)
            (dm-schema/as-error 4)
            50]
           (into [] (into []
                          (sut/chain (map inc)
                                     (map (fn [x] (if (even? x)
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (filter #(> % 3))
                                     (map (partial * 10)))
                          [1
                           2
                           3
                           4]))))
    (is (= [(dm-schema/as-error 2)
            (dm-schema/as-error 3)
            50]
           (into [] (into []
                          (sut/chain (map inc)
                                     (filter #(> % 3))
                                     (map (fn [x] (if (even? x)
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (map (partial * 10)))
                          [1
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
                           4])))))
  (testing "partition-all"
    (is (= [20
            (dm-schema/as-error 3)
            40
            50
            (dm-schema/as-error 6)]
           (into [] (into []
                          (sut/chain (map inc)
                                     (partition-all 2)
                                     (mapcat identity)
                                     (map (fn [x] (if (zero? (mod x 3))
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (map (partial * 10)))
                          [1
                           2
                           3
                           4
                           5]))))
    (is (= [(dm-schema/as-error 2)
            (dm-schema/as-error 3)
            20
            50
            (dm-schema/as-error 6)]
           (into [] (into []
                          (sut/chain (map inc)
                                     (partition-all 2)
                                     (mapcat identity)
                                     (map (fn [x] (if (zero? (mod x 3))
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (map (partial * 10)))
                          [1
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
                           4
                           5]))))
    (is (= [(dm-schema/as-error 3)
            20
            40
            (dm-schema/as-error 6)
            50]
           (into [] (into []
                          (sut/chain (map inc)
                                     (map (fn [x] (if (zero? (mod x 3))
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (partition-all 2)
                                     (mapcat identity)
                                     (map (partial * 10)))
                          [1
                           2
                           3
                           4
                           5]))))
    (is (= [(dm-schema/as-error 2)
            (dm-schema/as-error 3)
            20
            50
            (dm-schema/as-error 6)]
           (into [] (into []
                          (sut/chain (map inc)
                                     (map (fn [x] (if (zero? (mod x 3))
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (partition-all 2)
                                     (mapcat identity)
                                     (map (partial * 10)))
                          [1
                           (dm-schema/as-error 2)
                           (dm-schema/as-error 3)
                           4
                           5]))))
    (is (= [(dm-schema/as-error 3)
            [20 40]
            (dm-schema/as-error 6)
            [50]]
           (into [] (into []
                          (sut/chain (map inc)
                                     (map (fn [x] (if (zero? (mod x 3))
                                                   (dm-schema/as-error x)
                                                   x)))
                                     (map (partial * 10))
                                     (partition-all 2))
                          [1
                           2
                           3
                           4
                           5]))))))

(deftest collect-test
  (is (= {:errors [2 2 3]
          :result [50]}
         ((sut/collect (map inc)
                       (map (fn [x] (if (even? x)
                                     (dm-schema/as-error x)
                                     x)))
                       (map (partial * 10)))
          [1
           (dm-schema/as-error 2)
           (dm-schema/as-error 3)
           4])))
  (is (= {:errors [2]
          :result [10 30]}
         (let [collector (sut/collect (map inc)
                                      (map (fn [x] (if (even? x)
                                                    (dm-schema/as-error x)
                                                    x)))
                                      (take 2)
                                      (map (partial * 10)))]
           (collector (vec (range 10)))))))
