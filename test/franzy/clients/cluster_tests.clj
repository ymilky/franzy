(ns franzy.clients.cluster-tests
  (:require [midje.sweet :refer :all]
            [franzy.common.models.types :as mt]
            [franzy.clients.cluster :as cluster]
            [schema.core :as s]
            [franzy.common.models.schema :as fs])
  (:import (org.apache.kafka.common Cluster)))

(def mock-node-count 5)

(def mock-topic-partitions
  [{:topic "excel-database" :partition 0}
   {:topic "excel-database" :partition 1}
   {:topic "consulting-fees-from-replacing-nodejs" :partition 1010}
   {:topic "fried-side-items" :partition 50}])

(defn create-mock-cluster []
  (cluster/mock-cluster mock-node-count mock-topic-partitions #{"michael-bolton"}))

(def mock-cluster (create-mock-cluster))


(fact
  "The nodes in a cluster may be queried."
  (let [nodes (cluster/nodes mock-cluster)]
    (count nodes) => 5
    (s/check [fs/Node] nodes) => nil))

(fact
  "The partitions for a topic may be queried"
  (cluster/partitions-for-topic mock-cluster "fried-side-items") => [{:topic            "fried-side-items",
                                                                      :partition        50,
                                                                      :leader           {:id 1, :host "127.0.0.1", :port 2181},
                                                                      :replicas         [{:id 1, :host "127.0.0.1", :port 2181}],
                                                                      :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}])

(fact
  "Available partitions in the cluster can be listed."
  (let [partitions-info (cluster/available-partitions mock-cluster "excel-database")]
    (s/check [fs/PartitionInfo] partitions-info) => nil
    partitions-info => [{:topic            "excel-database",
                         :partition        0,
                         :leader           {:id 1, :host "127.0.0.1", :port 2181},
                         :replicas         [{:id 1, :host "127.0.0.1", :port 2181}],
                         :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}
                        {:topic            "excel-database",
                         :partition        1,
                         :leader           {:id 1, :host "127.0.0.1", :port 2181},
                         :replicas         [{:id 1, :host "127.0.0.1", :port 2181}],
                         :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}]))

(fact
  "Clusters canc be bootstrapped from a list of inet addresses or inet address maps"
  (let [boot-cluster (cluster/bootstrap-cluster-hosts [{:host-name "127.0.0.1" :port 9092} {:host-name "127.0.0.1" :port 9093}])]
    (instance? Cluster boot-cluster) => true
    (count (cluster/nodes boot-cluster)) => 2))

(fact
  "Empty clusters can be created."
  (let [empty-cluster (cluster/empty-cluster)]
    (instance? Cluster empty-cluster) => true
    (count (cluster/nodes empty-cluster)) => 0))

(fact
  "Partition leaders for the cluster can be queried for by topic partition."
  (let [leader (cluster/leader-for mock-cluster "consulting-fees-from-replacing-nodejs" 1010)]
    (s/check fs/Node leader) => nil
    leader => {:id 1, :host "127.0.0.1", :port 2181}))

(fact
  "Cluster nodes can be queried by id."
  (let [node (cluster/node-by-id mock-cluster 1)]
    (s/check fs/Node node) => nil
    node => {:id 1, :host "127.0.0.1", :port 9092}))

(fact
  "The partition count for a topic in the cluster can be queried."
  (cluster/partition-count mock-cluster "excel-database") => 2)

(fact
  "The partition info for a topic partition can be queried."
  (let [partition-info (cluster/partition-info-for-topic mock-cluster "excel-database" 1)]
    (s/check fs/PartitionInfo partition-info) => nil
    partition-info => {:topic            "excel-database",
                       :partition        1,
                       :leader           {:id 1, :host "127.0.0.1", :port 2181},
                       :replicas         [{:id 1, :host "127.0.0.1", :port 2181}],
                       :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}))

(fact
  "The partition info for a node in the cluster can be queried."
  (let [partition-info-coll (cluster/partitions-for-node mock-cluster 1)]
    (nil? partition-info-coll) => false
    (coll? partition-info-coll) => true
    (empty? partition-info-coll) => false
    (count partition-info-coll) => (count mock-topic-partitions)
    (s/check [fs/PartitionInfo] partition-info-coll) => nil
    partition-info-coll => [{:topic "excel-database",
                             :partition 0,
                             :leader {:id 1, :host "127.0.0.1", :port 2181},
                             :replicas [{:id 1, :host "127.0.0.1", :port 2181}],
                             :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}
                            {:topic "excel-database",
                             :partition 1,
                             :leader {:id 1, :host "127.0.0.1", :port 2181},
                             :replicas [{:id 1, :host "127.0.0.1", :port 2181}],
                             :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}
                            {:topic "consulting-fees-from-replacing-nodejs",
                             :partition 1010,
                             :leader {:id 1, :host "127.0.0.1", :port 2181},
                             :replicas [{:id 1, :host "127.0.0.1", :port 2181}],
                             :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}
                            {:topic "fried-side-items",
                             :partition 50,
                             :leader {:id 1, :host "127.0.0.1", :port 2181},
                             :replicas [{:id 1, :host "127.0.0.1", :port 2181}],
                             :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}]))

(fact
  "The partition info for a topic can be queried."
  (let [partition-info-coll (cluster/partitions-for-topic mock-cluster "excel-database")]
    (nil? partition-info-coll) => false
    (coll? partition-info-coll) => true
    (empty? partition-info-coll) => false
    (count partition-info-coll) => 2
    (s/check [fs/PartitionInfo] partition-info-coll) => nil
    partition-info-coll => [{:topic "excel-database",
                             :partition 0,
                             :leader {:id 1, :host "127.0.0.1", :port 2181},
                             :replicas [{:id 1, :host "127.0.0.1", :port 2181}],
                             :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}
                            {:topic "excel-database",
                             :partition 1,
                             :leader {:id 1, :host "127.0.0.1", :port 2181},
                             :replicas [{:id 1, :host "127.0.0.1", :port 2181}],
                             :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}]))

(fact
  "The topics in a cluster can be queried."
  (let [topics (cluster/topics mock-cluster)]
    (nil? topics) => false
    (coll? topics) => true
    (empty? topics) => false
    (count topics) => 3
    (s/check #{s/Str} topics) => nil
    topics => #{"excel-database" "fried-side-items" "consulting-fees-from-replacing-nodejs"}))