(ns franzy.clients.producer.partitioners
  "Partitioners used to partition topic partitions.

  Partitioners should implement the `Partitioner` interface directly to be used with Kafka configurations."
  (:import (org.apache.kafka.clients.producer.internals DefaultPartitioner)
           (org.apache.kafka.clients.producer Partitioner)))

(defn make-partitioner [partitioner-fn close-fn]
  "Simple wrapper, usually used for testing to create a custom topic partitioner on-demand.

  For real partitioner implementations, prefer to implement via deftype, defrecord, or gen-class."
  (reify
    Partitioner
    (partition [_ topic key key-bytes value value-bytes cluster]
      (partitioner-fn topic key key-bytes value value-bytes cluster))
    (close [_] (close-fn))))

(defn default-partitioner []
  "Creates a default partitioner for partitioning topics.

  This is the default implementation used by Kafka.

  Useful for defaults, swapping partitioning implementations, or testing."
  (DefaultPartitioner.))
