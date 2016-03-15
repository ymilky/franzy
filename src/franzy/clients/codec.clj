(ns franzy.clients.codec
  "Encodes and decodes Java and Clojure types used with org.apache.kafka.common

  See http://kafka.apache.org/090/javadoc/org/apache/kafka/common/package-summary.html"
  (:require [franzy.clients.consumer.types :as ct])
  (:import (java.util Map Set Collection List)
           (org.apache.kafka.common MetricName PartitionInfo Node TopicPartition)
           (org.apache.kafka.clients.producer ProducerRecord RecordMetadata)
           (org.apache.kafka.clients.consumer OffsetAndMetadata ConsumerRecord ConsumerRecords$ConcatenatedIterable OffsetResetStrategy ConsumerRecords)
           (org.apache.kafka.common.metrics KafkaMetric)))

(declare decode-xf)

(defprotocol FranzCodec
  "Protocol used to encode and decode values between Franzy and the Kafka Java client.

  Extend this protocol to implement conversions between types and modify existing conversions as needed."
  (encode [v]
    "Encodes Clojure values into Kafka Java client values.")
  (decode [v]
    "Decodes Kafka Java client values to Clojure."))

(defn map->topic-partition
  "Convert a map of topic and partition to a Kafka TopicPartition.

  Example Usage:

  `(map->topic-partition {:topic \"pontifications\" :partition 613})`"
  (^TopicPartition [topic partition]
   (TopicPartition. (name topic) (int partition)))
  (^TopicPartition [{:keys [topic partition]}]
   (TopicPartition. (name topic) (int partition))))

(defn maps->topic-partitions
  "Converts a collection of maps to topic partitions."
  [topic-partitions]
  (->>
    topic-partitions
    (map map->topic-partition)))

;;TODO: get rid of this, can probably manage without this extra fn and combine with the previous conversion fn
(defn maps->topic-partition-array
  "Converts a collection of maps to topic partition arrays."
  [topic-partitions]
  (->> topic-partitions
       (maps->topic-partitions)
       (into [])
       (into-array TopicPartition)))

(defn map->node
  "Converts a node map to a Kafka Node."
  ^Node
  [{:keys [id host port]}]
  (Node. id host port))

(defn node-maps->node-array
  "Converts a collection of node maps to a Kafka Node[]"
  [nodes]
  (->> nodes
       (map map->node)
       (into-array Node)))

(defn map->partition-info
  ^PartitionInfo
  [{:keys [topic partition leader replicas in-sync-replicas]}]
  (let [leader-node (map->node leader)
        replica-nodes (node-maps->node-array replicas)
        in-sync-replica-nodes (node-maps->node-array in-sync-replicas)]
    (PartitionInfo. topic partition leader-node replica-nodes in-sync-replica-nodes)))

(defn map->producer-record
  ^ProducerRecord
  ([{:keys [topic partition key value]}]
   (map->producer-record topic partition key value))
  ([^String topic partition key value]
   "Creates a record used by a producer when sending data to Kafka.

   * topic - The topic the record will be appended to (required)
   * partition - The partition to which the record should be sent (optional)
   * key - The key that will be included in the record (optional)
   * value - The record contents (required)"
   (if (nil? partition)
     ;Note: Producer Record has 3 constructors, but just proxies with a null key if key is null, so it's not needed here
     (ProducerRecord. topic key value)
     ;Note: we could assume that a nil partition should :or to 1, but we let Kafka handle this behavio rather than force it
     (ProducerRecord. topic (int partition) key value))))

(defn map->offset-metadata
  ^OffsetAndMetadata
  [{:keys [offset metadata]}]
  ;;TODO: schema
  (OffsetAndMetadata. offset metadata))

(defn map->topic-partition-offsets-map
  "Takes a Clojure map where the keys are topic partition maps and the values are offset metaedata maps, then converts it to a java.util.Map made of TopicPartition as keys and OffsetAndMetadata as values.

  Example:

  `(map->topic-partition-offsets-map
  {{:topic \"theweather\" :partition 0} {:offset 89 :metadata \"seti alpha 6\"}
  {:topic \"thegovernment\" :partition 1}  {:offset 2112 :metadata \"1984\"}
  {:topic \"popsongs\" :partition 1} {:offset 69 :metadata \"All I need is a miracle\"}})`"
  [m]
  (->> m
       (reduce
         (fn [m [k v]] (assoc! m (map->topic-partition k) (map->offset-metadata v))) (transient {}))
       (persistent!)))

(defn map->consumer-record
  "Convert a map of to a Kafka ConsumerRecord"
  ^ConsumerRecord
  [{:keys [key offset partition topic value]}]
  ;;TODO: not sure why/when some of these might be optional (topic is required) per the Java Constructor - Need to deal with this vs. schema
  (ConsumerRecord. (name topic) (some-> partition int) (some-> offset long) key value))

(defn consumer-record->map
  "Converts a Kafka Java API ConsumerRecord to a map."
  [^ConsumerRecord consumer-record]
  {:topic     (.topic consumer-record)
   :partition (.partition consumer-record)
   :offset    (.offset consumer-record)
   :key       (.key consumer-record)
   :value     (.value consumer-record)})

(defn consumer-records->map
  "Converts a Kafka Java API ConsumerRecords to a map."
  [^ConsumerRecords consumer-records]
  (map map->consumer-record (iterator-seq (.iterator consumer-records))))

;;I have no idea why they chose not just send in the same topic partition+offset data structures as the client, but so be it....
(defn map->topic-partition-offset-number
  "Converts a map where the keys are topic partitions and the values are offset positions (Long) to a map of Map<TopicPartition, Long>

  Example:

  `(map->topic-partition-offset-number
  {{:topic \"fixins\" :partition 0} 0
  {:topic \"fixins\" :partition 1} 0}
  {:topic \"expired-condiments\" :partition 55} 23})`"
  [m]
  (->> m
       (reduce
         (fn [m [k v]] (assoc! m (map->topic-partition k) v)) (transient {}))
       (persistent!)))

(defn lazy-consumer-records
  "Creates a lazy wrapper around Java ConsumerRecordsConcatenatedIterables.
  Useful if you want to create a wrapper or consume existing Java Kafka client code."
  [^ConsumerRecords$ConcatenatedIterable iterable]
  (lazy-seq
    (when-let [s (iterator-seq (.iterator iterable))]
      (cons (first s) (rest s)))))

;;TODO: strategy for better dealing with parsing enums
(defn keyword->offset-reset-strategy
  ^OffsetResetStrategy [offset-reset-strategy]
  (some->> offset-reset-strategy
           (name)
           (.toUpperCase)
           (OffsetResetStrategy/valueOf)))

(extend-protocol FranzCodec
  TopicPartition
  (encode [topic-partition] topic-partition)
  (decode [topic-partition]
    {:topic     (.topic topic-partition)
     :partition (.partition topic-partition)})

  ConsumerRecord
  (encode [consumer-record] consumer-record)
  (decode [consumer-record]
    ;;here we are preferring the record form over the map form
    ;;you can replace this with consumer-record->map if you want maps
    ;;doto just sounds mean
    (ct/->ConsumerRecord (.topic consumer-record)
                         (.partition consumer-record)
                         (.offset consumer-record)
                         (.key consumer-record)
                         (.value consumer-record)))

  ;;Prefer reified consume records, leaving this here for perf testing
  ConsumerRecords
  (encode [consumer-records] consumer-records)
  (decode [consumer-records]
    (map decode (iterator-seq (.iterator consumer-records))))

  OffsetAndMetadata
  (encode [offset-metadata] offset-metadata)
  (decode [offset-metadata]
    {:offset   (.offset offset-metadata)
     :metadata (.metadata offset-metadata)})
  Node
  (encode [node] node)
  (decode [node]
    {:id   (.id node)
     :host (.host node)
     :port (.port node)})

  PartitionInfo
  (encode [partition-info] partition-info)
  (decode [partition-info]
    {:topic            (.topic partition-info)
     :partition        (.partition partition-info)
     :leader           (-> (.leader partition-info)
                           (decode))
     ;;TODO: better hanlding of this issue w/ protocol: http://dev.clojure.org/jira/browse/CLJ-1790
     :replicas         (->> (.replicas partition-info)
                            (into [] decode-xf))
     :in-sync-replicas (->> (.inSyncReplicas partition-info)
                            (into [] decode-xf))})

  ProducerRecord
  (encode [producer-record] producer-record)
  (decode [producer-record]
    {:topic     (.topic producer-record)
     :partition (.partition producer-record)
     :key       (.key producer-record)
     :value     (.value producer-record)})

  RecordMetadata
  (encode [record-metadata] record-metadata)
  (decode [record-metadata]
    {:topic     (.topic record-metadata)
     :partition (.partition record-metadata)
     :offset    (.offset record-metadata)})

  MetricName
  (encode [metric-name] metric-name)
  (decode [metric-name]
    {:name        (.name metric-name)
     :description (.description metric-name)
     :group       (.group metric-name)
     :tags        (->> (.tags metric-name)
                       (decode))})

  KafkaMetric
  (encode [metric] metric)
  (decode [metric]
    (when metric
      (let [metric-value (.value metric)]
        {:metric-name (-> (.metricName metric)
                          (decode))
         ;;some weirdness here with things like -Infinity/Infinity when they probably didn't want to use a nullable double,
         ;;might want to drop it from the map, return nil for the entire map, or just leave it as -Infinity?
         :value       (if (Double/isInfinite metric-value) nil metric-value)})))

  List
  (encode [v] v)
  (decode [v]
    (when (seq v)
      (into [] decode-xf v)))

  Collection
  (encode [v] v)
  (decode [v]
    (when (seq v)
      (into [] decode-xf v)))

  Set
  (encode [v] v)
  (decode [v]
    (when (seq v)
      (into #{} decode-xf v)))

  ;;might want to toss this out
  Iterable
  (encode [it] it)
  (decode [it]
    (map decode (iterator-seq (.iterator it))))

  Map
  (encode [v] v)
  (decode [v]
    (->> v
         (reduce (fn [m [k val]]
                   (assoc! m (as-> (decode k) dk
                                   (if (string? dk) (keyword dk) dk))
                           (decode val)))
                 (transient {}))
         persistent!))

  nil
  (encode [v] v)
  (decode [v] v)

  Object
  (encode [v] v)
  (decode [v] v))

(def decode-xf
  "Transducer, applied on decode of collections that may be overriden using alter-var-root for example."
  (map decode))
