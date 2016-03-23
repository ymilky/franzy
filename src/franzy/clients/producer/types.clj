(ns franzy.clients.producer.types
  (:require [schema.core :as s]
            [franzy.clients.producer.schema :as ps])
  (:import (java.util.concurrent TimeUnit)))

(defrecord ProducerOptions
  [close-timeout close-timeout-unit send-callback])

(s/defn make-producer-options :- ps/ProducerOptions
  "Creates a producer options record.

  The default close-timeout-unit if not provided is milliseconds."
  ([m]
    (map->ProducerOptions m))
  ([close-timeout send-callback]
    (make-producer-options close-timeout TimeUnit/MILLISECONDS send-callback))
  ([close-timeout close-timeout-unit send-callback]
    (->ProducerOptions close-timeout (or close-timeout-unit TimeUnit/MILLISECONDS) send-callback)))

(defrecord RecordMetadata
  [topic partition offset])

(s/defn make-record-metadata :- ps/RecordMetadata
  "Creates a record metadata record."
  ([m]
    (map->RecordMetadata m))
  ([topic partition offset]
    (->RecordMetadata topic partition offset)))

(defrecord ProducerRecord
  [topic partition key value])

(s/defn make-producer-record :- ps/ProducerRecord
  "Creates a producer record (record).

  You must provide one of the following:

  * Topic and Value - Will use partitioner in producer config to decide which partition.
  * Topic, Partition, Key, Value - Will use explicit arguments.
  * Topic, Key, Value - Will use the key and partitioner to decide which partition."
  ([m]
    (map->ProducerRecord m))
  ([topic value]
    (make-producer-record topic nil nil value))
  ([topic key value]
    (make-producer-record topic nil key value))
  ([topic partition key value]
    (->ProducerRecord topic partition key value)))

