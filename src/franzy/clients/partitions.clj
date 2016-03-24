(ns franzy.clients.partitions
  "Helpers and useful functions for working with topic partitions."
  (:require [franzy.common.models.types :as mt]))

(defn mock-partition-info
  "Creates mock partition info for use with a mock cluster based on a collection of topic partition maps/records."
  [topic-partitions]
  (map (fn [topic-partition]
         {:topic     (:topic topic-partition)
          :partition (:partition topic-partition)
          :leader    {:host "127.0.0.1" :id 1 :port 2181}
          :replicas  [{:host "127.0.0.1" :id 1 :port 2181}]
          :in-sync-replicas [{:host "127.0.0.1" :id 1 :port 2181}]
          }) topic-partitions))

(defn topic-partition-range
  "Creates a linear amount of topic partitions based on a given topic and partition count."
  [topic partitions]
  (map (fn [n] (mt/->TopicPartition topic n)) (range 0 partitions)))

(defn topics-from-partitions
  "Creates a set of all the topics found in a collection of topic partitions."
  [topic-partitions]
  (into #{} (map :topic) topic-partitions))

(defn topic-partition-info->topic-partition
  "Creates a topic partition from partition info."
  [topic-partition-info]
  (select-keys topic-partition-info [:topic :partition]))