(ns franzy.clients.producer.client
  (:require [schema.core :as s]
            [franzy.clients.producer.schema :as ps]
            [franzy.common.configuration.codec :as config-codec]
            [franzy.clients.codec :as codec]
            [franzy.common.metadata.protocols :refer [KafkaMeasurable PartitionMetadataProvider]]
            [franzy.clients.producer.protocols :refer :all]
            [franzy.common.async.wrappers :as async-wrappers]
            [franzy.clients.producer.defaults :as defaults])
  (:import (org.apache.kafka.clients.producer KafkaProducer Callback Producer)
           (org.apache.kafka.common.serialization Serializer)
           (java.util Properties)
           (java.io Closeable)))

(deftype FranzProducer
  [^Producer producer producer-options]
  FranzyProducer
  (flush! [_]
    "Invoking this function makes all buffered records immediately available to send (even if linger.ms is greater than 0) and blocks on the completion of the requests associated with these records."
    (.flush producer))
  (send-async! [this m]
    (send-async! this m nil))
  (send-async! [this {:keys [topic partition key value]} options]
    {:pre [(not (nil? topic))
           (not (nil? value))]}
    (send-async! this topic partition key value options))
  (send-async! [_ topic partition k v {:keys [send-callback]
                                       :or   {send-callback (:send-callback producer-options)}}]
    "Asynchronously sends a record to a topic, and invokes the provided callback when the send has been acknowledged.

    You must provide a topic, partition, key, and value. Keys are optional for some serializers that can automatically
    create a key for you. This behavior is strongly discouraged.
    You should always specify your keys if possible to be explicit about your intentions.
    Failure to provide a key for many serializers, example binary-based will results in a CRC error if CRC checking is enabled.

    You may provide a send-callback either via the ProducerOptions or by passing it in the options map at the call-site.
    You can use `(callbacks/send-callback my-2-arity-callback)` to create a callback from a 2-arity Clojure function.
    Alternatively, you can implement your own callback via the Java type.
    You are strongly discouraged from creating new callbacks every call, rather deftype, defrecord, cache, bind, close over, or otherwise store your callback.
    For more information on callbacks, see franzy.clients.producer.callbacks

    Kafka sends asynchronously. This function will return immediately once the record has been stored in the buffer of records waiting to be sent.
    This allows sending many records in parallel without blocking to wait for the response after each one.

    The result of the send is a map constructed from Kafka RecordMetadata specifying the partition the record was sent to and the offset it was assigned.
    This map is available via the send function callback.
    If a send callback function is specified, the callback will be invoked with the map of RecordMetadata and/or any exception if present.

    The RecordMetadata will be returned as a map, for example:
    {:topic \"80s-movies\" :partition 0 :offset 1024}

    Since the send call is asynchronous it returns a Future for the RecordMetadata that will be assigned to this record.
    Dereferencing the future will block, returning the record metadata map.

    For more information, please see: https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html#send(org.apache.kafka.clients.producer.ProducerRecord,%20org.apache.kafka.clients.producer.Callback)"
    (-> producer
        (.send (codec/map->producer-record topic partition k v) ^Callback send-callback)
        (async-wrappers/wrap-future codec/decode)))
  (send-sync! [this m]
    (send-sync! this m nil))
  (send-sync! [this m options]
    "Blocking version of `(send-async! p)`.

    Return when the associated send future completes.

    > Note: If you have enabled any callbacks via the producer options, they will be invoked asynchronously when the send completes.
    Be aware that this may produce unwanted side-effects if you are running the same logic synchronously after this call completes.
    Either don't set the producer send-callback option, or pass {:send-callback nil} to override it in these cases."
    (deref (send-async! this m options)))
  (send-sync! [this topic partition k v options]
    (deref (send-async! this topic partition k v options)))
  (close [_ {:keys [close-timeout close-time-unit]
             :or   {close-timeout   (:close-timeout producer-options)
                    close-time-unit (:close-time-unit producer-options)}}]
    (.close producer close-timeout close-time-unit))
  Closeable
  (close [_]
    (.close producer))
  PartitionMetadataProvider
  (partitions-for [_ topic]
    "Get metadata about the partitions for a given topic."
    (->> topic
         (.partitionsFor producer)
         (codec/decode)))
  KafkaMeasurable
  (metrics [_]
    (->>
      (.metrics producer)
      (codec/decode))))

(s/defn make-producer :- FranzProducer
  "Create a Kafka Producer from a configuration, with optional serializers and optional producer options.
   If a callback is given, call it when stopping the consumer.
   If deserializers are provided, use them, otherwise expect deserializers via class name in the config map.

   This producer implementation wraps the Kafka Java Producer API.
   It provides a Clojure (ish) wrapper, with Clojure data structures to/from Kafka,
   and implements various protocols to allow more specialized consumers following this implementation.
   If you prefer a lower-level implementation or wish to test your producer, you may wish to browse this implementation
   and implement one or all the protocols provided.

   For per function documentation, please see the source for extensive comments, usage examples, etc.

   > Note: This implementation stresses a reasonable compromise between raw performance, extensibility, and usability, all things considered as:

   1. A wrapper
   2. Clojure

   Producer options serve the following purposes:

   * Avoid repeated/inconvenient passing of defaults to various methods requiring options such as timeouts. Many producers do not need per-call options.
   * Long-term extensibility as more features are added to this client, mitigating signature changes and excessive arities
   * Cheaper lookups and smaller memory footprint as the options are created in final form as records.
   * Dynamic construction of producer options via stream processors, back-off logic, etc.
   * Reduction in garbage collection for producers that do not need per-call options. Overall, less intermediate maps and reified objects.
   * Avoid slow memory allocations for the aforementioned cases.
   * Mitigate Kafka Java API changes. The API has often been in flux and sometimes it is necessary for extra options to handle weirdness from Java API bugs.

   > Note: Consumer options are distinct from the Kafka Consumer Configuration."
  ([config :- ps/ProducerConfig]
    (make-producer config nil))
  ([config :- ps/ProducerConfig
    options :- (s/maybe ps/ProducerOptions)]
    (-> config
        ^Properties (config-codec/encode)
        (KafkaProducer.)
        (FranzProducer. (defaults/make-default-producer-options options))))
  ([config :- ps/ProducerConfig
    key-serializer :- Serializer
    value-serializer :- Serializer]
    (make-producer config key-serializer value-serializer nil))
  ([config :- ps/ProducerConfig
    key-serializer :- Serializer
    value-serializer :- Serializer
    options :- (s/maybe ps/ProducerOptions)]
    (-> config
        ^Properties (config-codec/encode)
        (KafkaProducer. key-serializer value-serializer)
        (FranzProducer. (defaults/make-default-producer-options options)))))
