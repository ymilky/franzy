(ns franzy.clients.producer.types
  (:require [schema.core :as s]
            [franzy.clients.producer.schema :as ps]))

(defrecord ProducerOptions
  [close-timeout close-timeout-unit send-callback])

(s/defn make-producer-options :- ps/ProducerOptions
  [m]
  (map->ProducerOptions m))

(defrecord RecordMetadata
  [topic partition offset])

(s/defn make-record-metadata :- ps/RecordMetadata
  [m]
  (map->RecordMetadata m))

(defrecord ProducerRecord
  [topic partition key value])

(s/defn make-producer-record :- ps/ProducerRecord
  [m]
  (map->ProducerRecord m))
