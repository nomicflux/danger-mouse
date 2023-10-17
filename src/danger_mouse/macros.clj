;; # Macros

(ns danger-mouse.macros
  (:require [danger-mouse.utils :as utils]))

(defmacro try-catch
  "A try-catch block in function form. Uses a macro to delay resolution of the `body`."
  [& body]
  `(utils/try-catch* (fn [] ~@body)))
