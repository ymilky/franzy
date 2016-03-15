(ns franzy.clients.consumer.results
  "Kafka result set (ConsumerRecords) implementations."
  (:require [franzy.clients.codec :as codec]
            [franzy.clients.consumer.protocols :as proto])
  (:import (clojure.lang IReduceInit Seqable)
           (org.apache.kafka.clients.consumer ConsumerRecords)))

(defn consumer-records
  "Implementation of a Kafka result set.
  As Kafka result sets are stateful and provide various behaviors as well as shapes of information, it is not suitable for many advanced clients to receive only maps as results.

  This implementation guarantees the following:

  * Lazy/Non-lazy chunked/unchunked access to results from Kafka, with optional transducers applied without excessive intermediate objects.
  * Full fidelity of the results returned from Kafka (by topic, partition, all, record count, and future additions from the Java side). Nothing lost, much gained.
  * Ability to slice records via transducer or by calling built-in functions to slice on topic or topic partition.
  * Preservation of the result type from Kafka. No inadvetent consumption of iterators or eagerly realizing things if not desired.
  * Ability of sequence operations to be applied to result set via Seqable, and return only Clojure types consistent with the rest of the API.
  * Ability to reduce the result set itself in a high performance way via IReduceInit, and return only Clojure types consistent with the rest of the API.
  * Frees client implementations, testing, etc. from dealing wtih this behavior - no complecting the client implementation with handling the result set behavior."
  [^ConsumerRecords records]
  (reify
    proto/KafkaConsumerRecords
    (record-count [_]
      "Counts the number of records in the topic"
      (.count records))
    (record-partitions [_]
      "Lists the topic partitions present in the results."
      (->>
        (.partitions records)
        (codec/decode)))
    (records-by-topic [_ topic]
      "Returns consumer records by topic name."
      (->> (.records records ^String topic)
           (codec/lazy-consumer-records)
           (map codec/decode)))
    (records-by-topic-partition [_ topic partition]
      "Returns consumer records from a specific topic partition.

      > Note: The results are realized eagerly."
      (->> (codec/map->topic-partition topic partition)
           (.records records)
           (codec/decode)))
    (records-by-topic-partition [this {:keys [topic partition]}]
      (proto/records-by-topic-partition this topic partition))
    Seqable
    (seq [_]
      "Creates a lazy, seqable compliant wrapper around the results and ensures the results are mapped to the correct type."
      (->> records
           (codec/lazy-consumer-records)
           (map codec/decode)))
    IReduceInit
    (reduce [_ f init]
      "Allows the results to be reduced and mapped efficiently to the correct type."
      ;;TODO: iterator-seq or just use java iterator? Need to make sure this is safe and may not want to hang on to this iterator here.
      (when-let [iter (.iterator records)]
        (loop [ret init]
          (if (.hasNext iter)
            (let [record (->> (.next iter)
                              (codec/decode))
                  ret (f ret record)]
              (if (reduced? ret)
                @ret
                (recur ret)))
            ret))))))
