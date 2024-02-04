(ns danger-mouse.async-test
  (:require [danger-mouse.async :as sut]
            [danger-mouse.utils :as dm-utils]
            [clojure.test :refer [deftest is testing]]
            [clojure.core.async :as async]
            [danger-mouse.schema :as dm-schema]))

(defn throw-on-even
  [n]
  (if (even? n)
    (throw (ex-info "Even!" {:n n}))
    n))

(deftest safe-channel-test
  (testing "no additional tranformers"
    (let [c (sut/safe-channel 1)]
      (async/onto-chan!! c (range 5))
      (is (= [0 1 2 3 4]
             (->> c (async/into []) async/<!!))))
    (let [c (sut/safe-channel 1)]
      (async/onto-chan!! c [0 1 (dm-schema/as-error 2) 3 (dm-schema/as-error 4)])
      (is (= [0 1 (dm-schema/as-error 2) 3 (dm-schema/as-error 4)]
             (->> c (async/into []) async/<!!)))))
  (testing "maps"
    (let [c (sut/safe-channel 1 (map inc))]
      (async/onto-chan!! c (range 5))
      (is (= [1 2 3 4 5]
             (->> c (async/into []) async/<!!))))
    (let [c (sut/safe-channel 1 (map throw-on-even))]
      (async/onto-chan!! c (range 5))
      (is (= [(dm-schema/as-error {:error-msg "Even!", :input 0})
              1
              (dm-schema/as-error {:error-msg "Even!", :input 2})
              3
              (dm-schema/as-error {:error-msg "Even!", :input 4})]
             (->> c (async/into []) async/<!!
                 (map (partial dm-utils/on-error #(dissoc % :error)))))))
    (let [c (sut/safe-channel 1
                              (map inc)
                              (map throw-on-even)
                              (map #(* 10 %)))]
      (async/onto-chan!! c (range 5))
      (is (= [10
              (dm-schema/as-error {:error-msg "Even!", :input 1})
              30
              (dm-schema/as-error {:error-msg "Even!", :input 3})
              50]
             (->> c (async/into []) async/<!!
                 (map (partial dm-utils/on-error #(dissoc % :error))))))))
  (testing "filters"
    (let [c (sut/safe-channel 1 (filter even?))]
      (async/onto-chan!! c (range 5))
      (is (= [0 2 4]
             (->> c (async/into []) async/<!!))))
    (let [c (sut/safe-channel 1 (filter (fn [x]
                                          (when (> x 3) (throw (ex-info "Too big" {:n x})))
                                          (even? x))))]
      (async/onto-chan!! c (range 5))
      (is (= [0 2 (dm-schema/as-error {:error-msg "Too big", :input 4})]
             (->> c (async/into []) async/<!!
                  (map (partial dm-utils/on-error #(dissoc % :error)))))))
    (let [c (sut/safe-channel 1 (map throw-on-even) (filter #(< % 3)))]
      (async/onto-chan!! c (range 5))
      (is (= [(dm-schema/as-error {:error-msg "Even!", :input 0})
              1
              (dm-schema/as-error {:error-msg "Even!", :input 2})
              (dm-schema/as-error {:error-msg "Even!", :input 4})]
             (->> c (async/into []) async/<!!
                  (map (partial dm-utils/on-error #(dissoc % :error)))))))
    (let [c (sut/safe-channel 1 (filter #(< % 3)) (map throw-on-even))]
      (async/onto-chan!! c (range 5))
      (is (= [(dm-schema/as-error {:error-msg "Even!", :input 0})
              1
              (dm-schema/as-error {:error-msg "Even!", :input 2})]
             (->> c (async/into []) async/<!!
                  (map (partial dm-utils/on-error #(dissoc % :error))))))))
  (testing "partitions"
    (let [c (sut/safe-channel 1 (partition-all 2))]
      (async/onto-chan!! c (range 5))
      (is (= [[0 1] [2 3] [4]]
             (->> c (async/into []) async/<!!))))
    (let [c (sut/safe-channel 1 (partition-all 2) (mapcat identity))]
      (async/onto-chan!! c (range 5))
      (is (= [0 1 2 3 4]
             (->> c (async/into []) async/<!!))))
    (let [c (sut/safe-channel 1 (map throw-on-even) (partition-all 2) (mapcat identity))]
      (async/onto-chan!! c (range 5))
      (is (= [(dm-schema/as-error {:error-msg "Even!", :input 0})
              (dm-schema/as-error {:error-msg "Even!", :input 2})
              1
              3
              (dm-schema/as-error {:error-msg "Even!", :input 4})]
             (->> c (async/into []) async/<!!
                  (map (partial dm-utils/on-error #(dissoc % :error)))))))
    ;; TODO: This test result is highly unfortunate - errors after a partition-all
    ;; will throw out everything in that partition after the error. Tracked in issue #1.
    (let [c (sut/safe-channel 1 (partition-all 2) (mapcat identity) (map throw-on-even))]
      (async/onto-chan!! c (range 5))
      (is (= [(dm-schema/as-error {:error-msg "Even!", :input 1})
              (dm-schema/as-error {:error-msg "Even!", :input 3})]
             (->> c (async/into []) async/<!!
                  (map (partial dm-utils/on-error #(dissoc % :error)))))))
    (let [c (sut/safe-channel 1 (map inc) (partition-all 2) (mapcat identity) (map throw-on-even))]
      (async/onto-chan!! c (range 5))
      (is (= [1
              (dm-schema/as-error {:error-msg "Even!", :input 1})
              3
              (dm-schema/as-error {:error-msg "Even!", :input 3})
              5]
             (->> c (async/into []) async/<!!
                  (map (partial dm-utils/on-error #(dissoc % :error)))))))
    (let [c (sut/safe-channel 1 (partition-all 2) (mapcat identity)
                              (map (fn [x]
                                     (when (zero? (mod x 3))
                                       (throw (ex-info "Threven!" {:n x})))
                                     x)))]
      (async/onto-chan!! c (range 5))
      (is (= [(dm-schema/as-error {:error-msg "Threven!", :input 1})
              2
              (dm-schema/as-error {:error-msg "Threven!", :input 3})
              4]
             (->> c (async/into []) async/<!!
                  (map (partial dm-utils/on-error #(dissoc % :error)))))))))
