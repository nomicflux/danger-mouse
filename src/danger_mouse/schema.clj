(ns danger-mouse.schema
  (:require [schema.core :as schema]))

(schema/defschema ErrorResult
  {::error schema/Any})

(schema/defschema SuccessResult
  {::success schema/Any})

(schema/defschema Result
  (schema/cond-pre ErrorResult SuccessResult))

(schema/defschema GroupedResults
  {::error [schema/Any]
   ::success [schema/Any]})

(schema/defschema ResultType
  (schema/enum ::error ::success))

(schema/defn result-status :- ResultType
  [{::keys [error success]}]
  (cond
    error ::error
    success ::success
    :else ::success))

(schema/defn get-result :- schema/Any
  [{::keys [error success] :as x}]
  (cond
    error error
    success success
    :else x))

(schema/defn as-error :- ErrorResult
  [x :- schema/Any]
  {::error x})

(schema/defn as-success :- SuccessResult
  [x :- schema/Any]
  {::success x})
