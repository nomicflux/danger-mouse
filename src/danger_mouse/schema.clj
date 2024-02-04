;; # Schemas & Functions for them

(ns danger-mouse.schema
  (:require [schema.core :as s]))

;; ## Schemas

(s/defschema ErrorResult
  {::error s/Any})

(s/defschema GroupedResults
  {:errors [s/Any]
   :result [s/Any]})

(s/defschema ProcessedError
  {:error-msg s/Str
   :error Throwable
   :input s/Any})

(defn WithErrors
  "Used to show that a result of `schema` can now be accompanied with `ProcessedError`s."
  [schema]
  {:errors [ProcessedError]
   :result schema})

;; ## Schema Utility Functions

;; Format any value as an error.
;; Preferable to using the keyword manually, but the tools in utils are preferred
;; over explicitly creating errors.
(s/defn as-error :- ErrorResult
  [x :- s/Any]
  {::error x})

;; Check whether a value is an error.
;; Preferable to checking the keyword manually.
(s/defn is-error? :- s/Bool
  [{::keys [error]}]
  (not (not error)))

;; Retrieve error value, which can be an Any (not necessarily an exception).
;; Preferable to destructuring manually.
(s/defn get-error
  [{::keys [error]}]
  error)
