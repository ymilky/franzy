(ns franzy.clients.consumer.types
  (:require [schema.core :as s]
            [franzy.clients.consumer.schema :as cs]))

(defrecord ConsumerOptions
  [consumer-records-fn poll-timeout-ms offset-commit-callback rebalance-listener-callback])

(s/defn make-consumer-options :- cs/ConsumerOptions
  [m]
  (map->ConsumerOptions m))

(defrecord ConsumerRecord
  [topic partition offset key value])

(s/defn make-consumer-record [m] :- cs/ConsumerRecord
  (map->ConsumerRecord m))

(defrecord PartitionAssignment
  [topics user-data])

(s/defn make-partition-assignment :- cs/PartitionAssignment
  [m]
  (map->PartitionAssignment m))

(defrecord OffsetMetadata
  [offset metadata])

(s/defn make-offset-metadata :- cs/OffsetAndMetadata
  [m]
  (map->OffsetMetadata m))
