(ns danger-mouse.utils-test
  (:require [danger-mouse.utils :as sut]
            [clojure.test :refer [deftest is]]
            [danger-mouse.schema :as dm-schema]))

(deftest collect-results-map-test
  (is (= {::dm-schema/error []
          ::dm-schema/success []}
         (sut/collect-results-map [])))
  (is (= {::dm-schema/success [1 2 3]
          ::dm-schema/error []}
         (sut/collect-results-map [1 2 3])))
  (is (= {::dm-schema/success [1 2 3]
          ::dm-schema/error []}
         (sut/collect-results-map [(dm-schema/as-success 1)
                                   (dm-schema/as-success 2)
                                   (dm-schema/as-success 3)])))
  (is (= {::dm-schema/error [1 2 3]
          ::dm-schema/success []}
         (sut/collect-results-map [(dm-schema/as-error 1)
                                   (dm-schema/as-error 2)
                                   (dm-schema/as-error 3)])))
  (is (= {::dm-schema/error [1]
          ::dm-schema/success [2 3]}
         (sut/collect-results-map [(dm-schema/as-error 1)
                                   (dm-schema/as-success 2)
                                   3]))))

(deftest flatten-test
  (is (= (dm-schema/as-success 1)
         (sut/flatten 1)))
  (is (= (dm-schema/as-error 1)
         (sut/flatten (dm-schema/as-error 1))))
  (is (= (dm-schema/as-error 1)
         (sut/flatten (-> 1 dm-schema/as-error dm-schema/as-error))))
  (is (= (dm-schema/as-error 1)
         (sut/flatten (-> 1
                          dm-schema/as-error
                          dm-schema/as-error
                          dm-schema/as-error))))
  (is (= (dm-schema/as-success 1)
         (sut/flatten (dm-schema/as-success 1))))
  (is (= (dm-schema/as-success 1)
         (sut/flatten (-> 1 dm-schema/as-success dm-schema/as-success))))
  (is (= (dm-schema/as-success 1)
         (sut/flatten (-> 1
                          dm-schema/as-success
                          dm-schema/as-success
                          dm-schema/as-success))))
  (is (= (dm-schema/as-error 1)
         (sut/flatten (-> 1
                          dm-schema/as-success
                          dm-schema/as-error
                          dm-schema/as-success))))
  (is (= (dm-schema/as-error 1)
         (sut/flatten (-> 1
                          dm-schema/as-success
                          dm-schema/as-success
                          dm-schema/as-error))))
  (is (= (dm-schema/as-error 1)
         (sut/flatten (-> 1
                          dm-schema/as-error
                          dm-schema/as-success
                          dm-schema/as-error)))))
