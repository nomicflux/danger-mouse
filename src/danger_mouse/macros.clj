(ns danger-mouse.macros
  (:require [danger-mouse.utils :as utils]))

(defmacro try-catch
  [& body]
  `(utils/try-catch* (fn [] ~@body)))
