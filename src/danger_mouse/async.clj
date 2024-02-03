(ns danger-mouse.async
  (:require [clojure.core.async :as async]
            [danger-mouse.transducers :as dm-transducers]))

(defn safe-channel
  [buf-or-n & xforms]
  (async/chan buf-or-n (apply dm-transducers/chain
                              dm-transducers/contain-errors-xf
                              xforms)))
