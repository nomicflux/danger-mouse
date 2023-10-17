;; # Schemas & Functions for them

(ns danger-mouse.schema
  (:require [schema.core :as s]))

;; ## Schemas

(s/defschema ErrorResult
  {::error s/Any})

(s/defschema GroupedResults
  {:errors [s/Any]
   :successes [s/Any]})

;; ## Schema Utility Functions

(s/defn as-error :- ErrorResult
  "Format any value as an error.
   Preferable to using the keyword manually, but the tools in utils are preferred
   over explicitly creating errors."
  [x :- s/Any]
  {::error x})

(s/defn is-error? :- s/Bool
  "Check whether a value is an error.
   Preferable to checking the keyword manually."
  [{::keys [error]}]
  (not (not error)))

(s/defn get-error
  "Retrieve error value, which can be an Any (not necessarily an exception).
   Preferable to destructuring manually."
  [{::keys [error]}]
  error)
