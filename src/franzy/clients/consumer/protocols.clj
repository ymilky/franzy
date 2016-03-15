(ns franzy.clients.consumer.protocols)

(defprotocol KafkaConsumerRecords
  "Protocol for behaviors of Kafka result sets (ConsumerRecords)"
  (record-count [this])
  (record-partitions [this])
  (records-by-topic [this topic])
  (records-by-topic-partition
    [this topic partition]
    [this topic-partition]))

(defprotocol OffsetCommiter
  "Commits Kafka offsets, typically to Kafka itself or a highly reliable, fast datastore.
  For example, an implementor may choose with great reservation to commit to Zookeeper. Then divorce.
  One day someone will appreciate that you can commit."
  (commit-offsets-async!
    [this]
    [this opts]
    [this offsets opts])
  (commit-offsets-sync!
    [this]
    [this offsets])
  (committed-offsets [this topic-partition]))

(defprotocol SeekableLog
  "Protocol for a log, such as Kafka that is positionally seekable."
  (next-offset [this topic-partition])
  (seek-to-offset! [this topic-partition offset])
  (seek-to-beginning-offset! [this topic-partitions])
  (seek-to-end-offset! [this topic-partitions]))

;;Partitionable?
(defprotocol PartitionAssignable
  "Capable of being assigned, and thus auditing assigned partitions."
  ;;Like a space captain, a captain in space.
  (assigned-partitions [this]))

;;TODO: these 2 protocols should really be either/or
;;For now, we wanted 1 consumer that given the choice per the Java Client rather than re-implementing things
;;and splitting things up. As such, these need different names to avoid collisions, but it should be noted they are functionally
;;very different with very different characteristics that may not be managable with the same # of functions.
(defprotocol ManualPartitionAssignor
  "Manually assigns topic partitions to consumers."
  (assign-partitions! [this topic-partitions])
  ;;see clear-subscriptions! commentary
  (clear-partition-assignments! [this]))

(defprotocol AutomaticPartitionAssignor
  "Automatically assigns topic partitions to consumers."
  (subscribe-to-partitions!
    [this topics]
    [this topics opts])
  (partition-subscriptions [this])
  ;;essentially in the automatic and manual case, these do the same things due to the Java client,
  ;;however in practice, manually assigning and subscribing might require very different semantics
  ;;there may be a future method introduced into the Java client to separate these due to some discussions, thus we separate here
  (clear-subscriptions! [this]))

(defprotocol FranzyConsumer
  "Protocol for implementing a Kafka consumer.

  For more details regarding Kafka Consumers, see:
  https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/Consumer.html

  For an example of the Java implementation of this interface for Kafka 0.9 and above, see:
  https://kafka.apache.org/090/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html"
  (pause! [this topic-partitions])
  (poll!
    [this]
    [this opts])
  (resume! [this topic-partitions])
  (wakeup! [this])
  ;;via ICloseable instead of shutdown
  ;(close [this])
  )

(defprotocol FranzPartitionAssignor
  "Protocol used for implementors that need a specialized algorithm for assigning partitions to Kafka.
  Example strategies include by range or round-robin.
  See franzy.clients.consumer.partitioners examples."
  (partition-subscription [this topics])
  (assign-partition! [this partitions-per-topic subscriptions])
  (partition-assigned [this assignment])
  (partition-assignor-name [this]))
