(ns danger-mouse.schema
  (:require [schema.core :as s]))

(s/defschema ErrorResult
  {::error s/Any})

(s/defschema SuccessResult
  {::success s/Any})

(s/defschema Result
  (s/cond-pre ErrorResult SuccessResult))

(s/defschema GroupedResults
  {::error [s/Any]
   ::success [s/Any]})

(s/defschema ResultType
  (s/enum ::error ::success))

(s/defn result-status :- ResultType
  [{::keys [error success]}]
  (cond
    error ::error
    success ::success
    :else ::success))

(s/defn get-result :- s/Any
  [{::keys [error success] :as x}]
  (cond
    error error
    success success
    :else x))

(s/defn as-error :- ErrorResult
  [x :- s/Any]
  {::error x})

(s/defn as-success :- SuccessResult
  [x :- s/Any]
  {::success x})
