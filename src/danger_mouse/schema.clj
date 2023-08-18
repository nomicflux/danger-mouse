(ns danger-mouse.schema
  (:require [schema.core :as s]))

(s/defschema ErrorResult
  {::error s/Any})

(s/defschema GroupedResults
  {::errors [s/Any]
   ::successes [s/Any]})

(s/defn as-error :- ErrorResult
  [x :- s/Any]
  {::error x})

(s/defn is-error? :- s/Bool
  [{::keys [error]}]
  (not (not error)))

(s/defn get-error
  [{::keys [error]}]
  error)
