(ns franzy.clients.partitions-tests
  (:require [midje.sweet :refer :all]
            [franzy.clients.partitions :as partitions]
            [franzy.common.models.schema :as fs]
            [schema.core :as s]))

(fact
  "A collection of topic partitions can be generated given a topic and number of partitions"
  (let [topic-partitions (partitions/topic-partition-range "socks-with-sandals-say-no" 5)]
    (nil? topic-partitions) => false
    (coll? topic-partitions) => true
    (empty? topic-partitions) => false
    (count topic-partitions) => 5
    (s/check [fs/TopicPartition] topic-partitions) => nil))

(fact
  "A set of topics can be extracted from a collection of topic partitions."
  (let [topics (partitions/topics-from-partitions [{:topic "sjw" :partition 99} {:topic "sjw" :partition 0} {:topic "too-much-time" :partition 5}])]
    (nil? topics) => false
    (coll? topics) => true
    (empty? topics) => false
    (set? topics) => true
    (count topics) => 2
    topics => (contains "sjw" "too-much-time")))

(fact
  "A topic partition can be converted from topic partition info."
  (let [topic-partition (partitions/topic-partition-info->topic-partition {:topic "excel-database",
                                                             :partition 15,
                                                             :leader {:id 1, :host "127.0.0.1", :port 2181},
                                                             :replicas [{:id 1, :host "127.0.0.1", :port 2181}],
                                                             :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]})]
    (nil? topic-partition) => false
    (coll? topic-partition) => true
    (empty? topic-partition) => false
    (map? topic-partition) => true
    (s/check fs/TopicPartition topic-partition) => nil
    topic-partition => {:topic "excel-database" :partition 15}))

(fact
  "Partition info can be created from a topic partition."
  (let [mock-partition-info (partitions/mock-partition-info [{:topic "excel-database" :partition 15}])]
    (nil? mock-partition-info) => false
    (coll? mock-partition-info) => true
    (empty? mock-partition-info) => false
    (s/check [fs/PartitionInfo] mock-partition-info) => nil
    (first mock-partition-info) => {:topic "excel-database",
                            :partition 15,
                            :leader {:id 1, :host "127.0.0.1", :port 2181},
                            :replicas [{:id 1, :host "127.0.0.1", :port 2181}],
                            :in-sync-replicas [{:id 1, :host "127.0.0.1", :port 2181}]}))
