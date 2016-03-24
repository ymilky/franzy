(ns franzy.clients.producer.partitioners
  "Partitioners used to partition topic partitions.

  Partitioners should implement the `Partitioner` interface directly to be used with Kafka configurations."
  (:require [franzy.clients.partitions :as partitions]
            [franzy.clients.cluster :as cluster])
  (:import (org.apache.kafka.clients.producer.internals DefaultPartitioner)
           (org.apache.kafka.clients.producer Partitioner)
           (org.apache.kafka.common.serialization Serializer)))

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

(defn calculate-partition
  "Calculates the hypothetical partition for a given topic of n partitions and value to partition, with an optional key.

  The calculate partition is dependent on the serializer used and the partitioning algorithm, specificed as a partitioner
  interface implementor. If no partitioner is provided, the default partitioner is assumed.

  This function allows a deterministic way of figuring out which partition your data will go to from a given
  producer input, even when disconnected from Kafka.

  Note that the partition will change for partitioners that depend on some random runtime state. If this is the case,
  ensure you pass the partitioner itself with any required state inside it so your results are reproducible.

  Additionally, some partitioners may not return the same result in the future if the number of partitions increases.
  Be mindful of the partitioning algorithm.

  Most good partitioners should return predictable results, however since the implementation is oopen, there is no guarantee."
  ([{:keys [topic key value]} partitions key-serializer value-serializer]
   (calculate-partition topic key value partitions key-serializer value-serializer nil))
  ([{:keys [topic key value]} partitions key-serializer value-serializer partitioner]
   (calculate-partition topic key value partitions key-serializer value-serializer partitioner))
  ([^String topic key value partitions ^Serializer key-serializer ^Serializer value-serializer ^Partitioner partitioner]
   (let [key-bytes (.serialize key-serializer topic key)
         value-bytes (.serialize value-serializer topic value)
         cluster (cluster/mock-cluster 1 (partitions/topic-partition-range topic partitions) #{})]
     (.partition (or partitioner (default-partitioner)) topic key key-bytes value value-bytes cluster))))