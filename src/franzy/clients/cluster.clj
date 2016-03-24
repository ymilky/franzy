(ns franzy.clients.cluster
  "Helpers and useful functions for working with Cluster metadata."
  (:require [franzy.clients.codec :as codec]
            [franzy.clients.partitions :as partitions])
  (:import (org.apache.kafka.common Cluster)
           (java.net InetSocketAddress)))

(defn bootstrap-cluster-hosts
  "Bootstraps a cluster from a collection of maps of host names and ports for InetAddresses.

  ex: `(bootstrap-cluster-hosts [{:host-name \"localhost\" :port 9092}])`"
  [host-map-coll]
  (->> (map (fn [{:keys [^String host-name port]}] (InetSocketAddress. host-name (int port))) host-map-coll)
       (Cluster/bootstrap)))

(defn bootstrap-cluster
  "Bootstraps a cluster from a collection of InetAddresses."
  [addresses]
  (Cluster/bootstrap addresses))

(defn empty-cluster []
  "Creates an empty cluster with no nodes and no topic-partitions."
  (Cluster/empty))

(defn make-cluster
  "Creates a cluster from a given collection of nodes and partition info and an optional set of unauthorized topics.

  If no parameters are provided, an empty cluster is created."
  (^Cluster [] (Cluster/empty))
  (^Cluster [nodes partition-info]
    (make-cluster nodes partition-info nil))
  (^Cluster [nodes partition-info unauthorized-topics]
   (let [node-coll (map codec/map->node nodes)
         partition-coll (map codec/map->partition-info partition-info)
         unauthorized-topic-set (into #{} unauthorized-topics)]
     (Cluster. node-coll partition-coll unauthorized-topic-set))))

(defn mock-nodes
  "Creates a mock number of nodes based on the provided node count."
  [node-count]
  (map (fn [n] {:id n :host "127.0.0.1" :port 9092}) (range 1 (inc node-count))))

(defn mock-cluster
  "Creates a mock cluster for testing, dev, and as dummy data for Kafka functions requiring clusters such as partitioners."
  [node-count topic-partitions unauthorized-topics]
  (let [nodes (mock-nodes node-count)
        partitions (partitions/mock-partition-info topic-partitions)]
    (make-cluster nodes partitions unauthorized-topics)))

(defn available-partitions
  "Retrieve a collection of available partitions for a topic in a cluster."
  [^Cluster cluster ^String topic]
  (->> (.availablePartitionsForTopic cluster topic)
       (codec/decode)))

(defn leader-for
  "Retrives the partition leader for a given topic partition."
  ([^Cluster cluster {:keys [topic partition]}]
   (leader-for cluster topic partition))
  ([^Cluster cluster topic partition]
   (->
     (.leaderFor cluster (codec/map->topic-partition topic partition))
     (codec/decode))))

(defn node-by-id
  "Retrieves a node by its node id."
  [^Cluster cluster node-id]
  (some-> (.nodeById cluster (int node-id))
          (codec/decode)))

(defn nodes
  "Retrieves a collection of nodes in the cluster."
  [^Cluster cluster]
  (->> (.nodes cluster)
       (codec/decode)))

(defn partition-info-for-topic
  "Retrieves the partition info for a given topic partition."
  ([^Cluster cluster {:keys [topic partition]}]
    (partition-info-for-topic cluster topic partition))
  ([^Cluster cluster topic partition]
   (->> (codec/map->topic-partition topic partition)
        (.partition cluster)
        (codec/decode))))

(defn partition-count
  "Retrives the partition count for a given topic."
  [^Cluster cluster ^String topic]
  (.partitionCountForTopic cluster topic))

(defn partitions-for-node
  "Retrieves a collection of partitions with a leader matching the given node id."
  [^Cluster cluster node-id]
  (->> (.partitionsForNode cluster (int node-id))
       (codec/decode)))

(defn partitions-for-topic
  "Retrieves a collection of partitions for a given topic."
  [^Cluster cluster ^String topic]
  (->> (.partitionsForTopic cluster topic)
       (codec/decode)))

(defn topics
  "Retrieves a collection of topics in this cluster."
  [^Cluster cluster]
  (->>
    (.topics cluster)
    (codec/decode)))