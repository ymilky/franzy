(ns franzy.clients.consumer.callbacks
  (:require [franzy.clients.codec :as codec])
  (:import (org.apache.kafka.clients.consumer ConsumerRebalanceListener OffsetCommitCallback)))

(deftype NoOpConsumerRebalanceListener []
  ConsumerRebalanceListener
  (onPartitionsAssigned [_ _])
  (onPartitionsRevoked [_ _]))

(defn ^NoOpConsumerRebalanceListener no-op-consumer-rebalance-listener []
  "Creates a no-op consumer rebalance listener."
  (NoOpConsumerRebalanceListener.))

(defn ^NoOpConsumerRebalanceListener no-op-consumer-rebalance-listener
  ;;doing nothing has never been so good...
  "Creates a no-op consumer rebalance listener
  This callback is implemented as a concrete type, which you may use for introspection for debugging, testing, etc."
  ^ConsumerRebalanceListener []
  (NoOpConsumerRebalanceListener.))

(defn consumer-rebalance-listener
  "Creates a ConsumerRebalanceListener from Clojure function(s) that will receive a list of topic partitions assigned or
  revoked, subject to the codec.

   For the 2-arity version of this function, you must provide a 1-arity function receiving a map of topic partitions.
   The partitions-assigned-fn will be called when a partition is assigned and will receive any
   topic partitions assigned. Likewise, when a partition is revoked, the partitions-revoked-fn will be called.

   The 1-arity version of this function works in a similar fashion, but you must provide a 2-arity function that will
   receive topic partitions as its first arugment, and a keyword as its second argument.
   When partitions are assigned, the :assigned keyword will be passed to your function.
   Likewise, when partitons are revoked, the :revoked keyword will be passed.

   The 0-arity version of this function will create a NoOpConsumerRebalanceListener, which you can use for testing,
   defaults, etc.

   You may use this callback to trigger custom actions when the set of partitions assigned to the consumer changes.
   For example, if you want to save offsets to your own datastore, you may do so either when the partition is assigned
   or revoked.

   It is recommended that if this datastore is not Kafka itself, it should be high-performance and fault-tolerant.
   Any consumers manually managing offsets should either use this function to create and later register this callback,
   or you should manually implement the ConsumerRebalanceListener interface yourself on a reified object or via deftype.

   See https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/ConsumerRebalanceListener.html for more information."
  (^ConsumerRebalanceListener []
   "Creates a no-op consumer rebalance listener, useful for testing, defaults, etc."
   (no-op-consumer-rebalance-listener))
  (^ConsumerRebalanceListener [partition-rebalancer-fn]
    ;;per user-request - useful for multi-methods
   (reify ConsumerRebalanceListener
     (onPartitionsAssigned [_ topic-partitions]
       (partition-rebalancer-fn (->> topic-partitions
                                     (codec/decode)) :assigned))
     (onPartitionsRevoked [_ topic-partitions]
       (partition-rebalancer-fn (->> topic-partitions
                                     (codec/decode)) :revoked))))
  (^ConsumerRebalanceListener [partitions-assigned-fn partitions-revoked-fn]
   (reify ConsumerRebalanceListener
     (onPartitionsAssigned [_ topic-partitions]
       (partitions-assigned-fn (->> topic-partitions
                                    (codec/decode))))
     (onPartitionsRevoked [_ topic-partitions]
       (partitions-revoked-fn (->> topic-partitions
                                   (codec/decode)))))))

(deftype NoOpOffsetCommitCallback []
  OffsetCommitCallback
  ;;looks like an atari enemy
  (onComplete [_ _ _]))

(defn ^NoOpOffsetCommitCallback no-op-offset-commit-callback
  "Creates a no-op offset commit callack.
  This callback is implemented as a concrete type, which you may use for introspection, testing, logging, etc."
  []
  (NoOpOffsetCommitCallback.))

(defn offset-commit-callback
  "Creates an OffsetCommitCallback from an optional Clojure function(s) and passes an exceptions and offset metadata
  created while committing offsets.

  There are 3 arities you may use to create this callback

  The single arity version will create a callback given a 2-arity offset commit function.
  The first argument passed to your function will be a map of offset metadata.
  The second argument will be any exceptions. These arguments are mutually exclusive.

  If you prefer a version that separates errors and offsets, use the 2-arity version of this function.
  The 2-arity version receives an offset commit success function of a single arity and will receive the offsets.
  The offset-commit failure version of this function will receive an exception.

  If neither of these suit your use-case or you need something for testing, convenience, defaults, etc, the 0-arity
  version of this function will generate a NoOpOffsetCommitCallback for you. Like the name, this is a no-op.

  An Offset commit callback is used to provide asynchronous handling of offset commit request completion.
  For example, when you wish to add metadata to particular offsets you commit, you can listen for the completition of
  the offset metadata storage using this callback.
  This function will be called when the offset commit request sent to the server has been acknowledged.

  See https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/OffsetCommitCallback.html for more information."
  (^OffsetCommitCallback []
   (no-op-offset-commit-callback))
  (^OffsetCommitCallback [offset-commit-fn]
   (reify OffsetCommitCallback
     (onComplete [_ offsets e]
       (offset-commit-fn (->> offsets (codec/decode)) e))))
  (^OffsetCommitCallback [offset-commit-success-fn offset-commit-failure-fn]
   (reify OffsetCommitCallback
     (onComplete [_ offsets e]
       (if offsets
         (->> offsets (codec/decode) (offset-commit-success-fn))
         (offset-commit-failure-fn e))))))
