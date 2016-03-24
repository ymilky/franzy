(ns franzy.clients.consumer.client
  (:require [schema.core :as s]
            [franzy.clients.consumer.schema :as cs]
            [franzy.common.metadata.protocols :refer [KafkaMeasurable TopicMetadataProvider PartitionMetadataProvider]]
            [franzy.clients.consumer.protocols :refer :all]
            [franzy.clients.codec :as codec]
            [franzy.clients.consumer.callbacks :as callbacks]
            [franzy.clients.consumer.results :as consumer-results]
            [franzy.common.configuration.codec :as config-codec]
            [franzy.clients.consumer.defaults :as defaults])
  (:import (org.apache.kafka.clients.consumer KafkaConsumer ConsumerRebalanceListener Consumer)
           (org.apache.kafka.common.serialization Deserializer)
           (java.util List Properties)
           (java.util.regex Pattern)
           (java.io Closeable)))

(deftype FranzConsumer
  [^Consumer consumer consumer-options]
  FranzyConsumer
  (pause! [_ topic-partitions]
    "Suspend fetching from the requested partitions.
    Future calls to poll, i.e. `(poll! c {:poll-timeout-ms 1000})` will not return any records from these partitions
    until they have been resumed using

    `(resume c topic-partitions)`.

    Note that this method does not affect partition subscription. In particular, it does not cause a group rebalance
    when automatic assignment is used."
    (->>
      topic-partitions
      (codec/maps->topic-partition-array)
      (.pause consumer)))
  (poll! [this]
    (poll! this nil))
  (poll! [_ {:keys [consumer-records-fn poll-timeout-ms]
             :or   {consumer-records-fn (get consumer-options :consumer-records-fn seq)
                    poll-timeout-ms     (:poll-timeout-ms consumer-options)}}]
    "Polls for all topics or topic partitions specified by assign/subscribe and returns a lazy sequence of consumer record maps.

    It is an error to not have subscribed to any topics or partitions before polling for data.

    On each poll, consumer will try to use the last consumed offset as the starting offset and fetch sequentially.
    The last consumed offset can be manually set through `(seek! c topic-partition offset)` or automatically set
    as the last committed offset for the subscribed list of partitions"
    ;;TODO: move last step into codec
    (some->> (.poll consumer poll-timeout-ms)
             (consumer-results/consumer-records)
             ;(consumer-records-fn)
             ))
  (resume! [_ topic-partitions]
    "Resume specified partitions which have been paused with (pause c topic-partitions).
    New calls to (poll c timeout) will return records from these partitions if there are any to be fetched.
    If the partitions were not previously paused, this method is a no-op."
    (->> topic-partitions
         (codec/maps->topic-partition-array)
         (.resume consumer)))
  (wakeup! [_]
    "Wakeup the consumer. This method is thread-safe and is useful in particular to abort a long poll. The thread which is blocking in an operation will throw WakeupException."
    (.wakeup consumer))
  Closeable
  (close [_]
    (.close consumer))
  TopicMetadataProvider
  (list-topics [_]
    "Get metadata about partitions for all topics that the user is authorized to view."
    (->> (.listTopics consumer)
         (codec/decode)))
  PartitionMetadataProvider
  (partitions-for [_ topic]
    "Get metadata about the partitions for a given topic."
    (->> topic
         (.partitionsFor consumer)
         (codec/decode)))
  SeekableLog
  (next-offset [_ topic-partition]
    "Gets the offsets of the next record that will be fetched in the given topic partition.

    Example:

    `(next-offset {:topic \"linked-in-profiles\" :partition 43252})`
    -> {:offset 23432 :metadata \"this headhunter has great opportunities for only me, adding to rolodex.\"}"
    (->> topic-partition
         (codec/map->topic-partition)
         (.position consumer)))
  (seek-to-offset! [_ topic-partition offset]
    "Given a topic partition and an offset number, seeks to an offset in the given partition for that topic.

    Overrides the fetch offsets that the consumer will use on the next poll, ex: (poll! c).
    If this API is invoked for the same partition more than once, the latest offset will be used on the next poll.

    Example: `(seek-to-offset! {:topic \"history-of-world\" :partition 0} 0)` - seeks to the beginning of time, in this dimension.

    > Note: that you may lose data if this API is arbitrarily used in the middle of consumption, to reset the fetch offsets.

    Use with extreme care, realizing that this API statefully repositions where Kafka will seek from.

    > Note: You may not seek to an unassigned partition. For example, if you attempt to seek before/while subscribing, an exception will be thrown by design. This is because the consumer has not yet been assigned a topic and partition that you can seek into. The solution is to either manually assign a partition to your consumer, or if using subscriptions, seek after subscription.

    You can assume that you have been assigned a valid topic and partition when receiving a valid event inside a consumer rebalance callback, and thus seek inside the callback."
    (as-> (codec/map->topic-partition topic-partition) tp
          (.seek consumer tp offset)))
  (seek-to-beginning-offset! [_ topic-partitions]
    "Seeks to the beginning of a given topic and partition.
    Typically identical to calling (seek-to-offset c topic-partition 0), but makes semantic intent clear and leaves the implementation detail of the beginning position to Kafka."
    (->> topic-partitions
         (codec/maps->topic-partition-array)
         (.seekToBeginning consumer)))
  (seek-to-end-offset! [_ topic-partitions]
    "Seeks to the last offset for each of the given partitions.
    This function evaluates lazily, seeking to the final offset in all partitions only when `(poll! c)` or `(next-offset c topic-partition)` are called.

    > Note: Do not attempt to manually seek to the end offset via seek-to-offset!, instead call this function to ensure correct behavior. Though your seek may succeed, it will not be guaranteed to yield the next offset correctly as Kafka is distributed."
    (->> topic-partitions
         (codec/maps->topic-partition-array)
         (.seekToEnd consumer)))
  OffsetCommiter
  (commit-offsets-async! [_]
    "Same as `(commit-offsets-async! c options)`, but behaves as a fire-and-forget commit.
    If you require notification when a commit completes, you must pass a commit offset callback via a different arity of this function."
    (.commitAsync consumer))
  (commit-offsets-async! [_ {:keys [offset-commit-callback]
                             :or   {offset-commit-callback (:offset-commit-callback consumer-options)}}]
    "Commits a list of offsets to Kafka, returned on the last poll, ex: `(poll! c)`.

    You may optionally pass an offset commit callback implement org.apache.kafka.clients.consumer.OffsetCommitCallback.
    The callback will be invoked when the commit completes.
    If you always need the same offset commit callback, prefer setting this via the offset-commit-callback key in the consumer options.
    The function must be a 2 arity function, in the form of `(fn [topic-partition offset-metadata])`.
    You may construct the callback from a Clojure function by calling `(offset-commit-callback my-offset-commit-processing-fn)`.
    You should avoid creating callbacks anew each call, and instead cache, reify, deftype, defrecord, impl Java interface, etc. instead.

    This commits offsets only to Kafka. The offsets committed using this API will be used on the first fetch after every rebalance and also on startup.
    As such, if you need to store offsets in anything other than Kafka, this API should not be used.

    This is an asynchronous call and will not block. Any errors encountered are either passed to the callback (if provided) or discarded."
    (->>
      (.commitAsync consumer offset-commit-callback)))
  (commit-offsets-async! [_ offsets {:keys [offset-commit-callback]
                                     :or   {offset-commit-callback (:offset-commit-callback consumer-options)}}]
    "Commits a list of offsets to kafka given a map where the keys are topic partitions and the values are offset metadataof offset metadata.

    This commits offsets to Kafka. The offsets committed using this API will be used on the first fetch after every rebalance and also on startup.
    As such, if you need to store offsets in anything other than Kafka, this API should not be used.
    The committed offset should be the next message your application will consume, i.e. last-processed-offset + 1.

    This is an asynchronous call and will not block.
    Any errors encountered are either passed to the callback (if provided) or discarded.
           Example:

    `(commit-offsets-async!
    {{:topic \"failed-startups\" :partition 102} {:offset 124, :metadata \"uber for spoiled people\"}
     {:topic \"failed-startups\" :partition 103} {:offset 2006, :metadata \"starting a music and fashion startup was a great decision\"}}
     {:offset-commit-callback call-me-maybe-offsets-callback-fn)`"
    (.commitAsync consumer (codec/map->topic-partition-offsets-map offsets) offset-commit-callback))
  (commit-offsets-sync! [_]
    "Synchronous version of `(commit-offsets-async! c)`.

    If you require a blocking commit of offsets to Kafka, you can either call this function or manually implement blocking using the alternative async version of this function.
    Any exceptions that occur during a commit will be returned to the caller on the calling thread."
    (.commitSync consumer))
  (commit-offsets-sync! [_ offsets]
    "Commit the specified offsets for the specified map of topics and partitions.

    This commits offsets to Kafka. The offsets committed using this API will be used on the first fetch after every rebalance and also on startup.
    As such, if you need to store offsets in anything other than Kafka, this API should not be used.
    The committed offset should be the next message your application will consume, i.e. lastProcessedMessageOffset + 1.

    This is a synchronous commits and will block until either the commit succeeds or an unrecoverable error is encountered (in which case it is thrown to the caller).

    Example:

    `(commit-offsets!
    {{:topic \"failed-startups\" :partition 25} {:offset 14, :metadata \"flooz was my best idea\"}
     {:topic \"failed-startups\" :partition 25} {:offset 4791, :metadata \"billions for yet another chat app\"}})`"
    (->> offsets
         (codec/map->topic-partition-offsets-map)
         (.commitSync consumer)))
  (committed-offsets [_ topic-partition]
    "Get the last committed offset for the given partition (whether the commit was issued consumer or another).

    Returns offset metadata.

    Example:
    `(committed-offsets {:topic \"words-of-advice-for-young-people\" :partition 67})`

     `{:offset 96 :metadata \"Though it is hard to commit, it is easy to buy a fortune cookie.\"}`"
    (->>
      topic-partition
      (codec/map->topic-partition)
      (.committed consumer)
      (codec/decode)))
  PartitionAssignable
  (assigned-partitions [_]
    "Gets the partitions currently assigned to this consumer.

    Returns a set of topic partitions.

    Example:

    `(assigned-partitions c)`

    `#{{:topic \"michael-ironside-action-credits\" :partition 55} {:topic \"michael-ironside-action-credits\" :partition 56} {:topic \"gigantic-collection-of-illegible-logentries\" :partition 93243}}`"
    ;;TODO: rewrite to speed up
    (->>
      (.assignment consumer)
      (codec/decode)))
  AutomaticPartitionAssignor
  (subscribe-to-partitions! [this topics]
    "Given a collection of topics, dynamically assigns partitions to those topics.

    Example:

    `(subscribe-to-partitions! [\"extremely-repetitive-music-recommendations\" \"small-data-called-big-data\" \"cats-watching-cat-videos\"])`"
    (subscribe-to-partitions! this topics nil))
  (subscribe-to-partitions! [_ topics {:keys [^ConsumerRebalanceListener rebalance-listener-callback]
                                       :or   {rebalance-listener-callback (:rebalance-listener-callback consumer-options)}}]
    "Subscribe to the given list of topics to get dynamically assigned partitions.

    Optionally, you can pass a map of consumer rebalance functions if you want to handle when a partition is assigned and/or revoked.
    You can specify a function for either case of revoke, both, or none at all.

    Example:

    `(subscribe! [\"air-guitar-players\" \"bad-hair-days\"] {:rebalance-listener-callback best-callback-on-the-high-seas})`

    Topic subscriptions are not incremental.
    This list will replace the current assignment (if there is one).
    Note that it is not possible to combine topic subscription with group management with manual partition assignment through (assign topics).
    If the given list of topics is empty, it is treated the same as (unsubscribe! c).

    As part of group management, the consumer will keep track of the list of consumers that belong to a particular group and will trigger a rebalance operation if one of the following events trigger

     * Number of partitions change for any of the subscribed list of topics
     * Topic is created or deleted
     * An existing member of the consumer group dies
     * A new member is added to an existing consumer group via the join API

     When any of these events are triggered, the provided listener will be invoked first to indicate that the consumer's assignment has been revoked, and then again when the new assignment has been received.
     Note that this listener will immediately override any listener set in a previous call to subscribe.
     It is guaranteed, however, that the partitions revoked/assigned through this interface are from topics subscribed in this call. See ConsumerRebalanceListener for more details."
    ;;at least in the time of writing, there appears to be a case where if somehow this callback is null, explosions happen
    ;;here we are being extra-safe, but this let can probably be removed in the future
    (let [^ConsumerRebalanceListener listener (or rebalance-listener-callback (callbacks/consumer-rebalance-listener))]
      ;;1) We could bind topics in the let statement using this cond, and call subscribe just once below, this is an experiment to avoid reflection
      ;;2) I am not sure I really like the idea of this cond vs. simply having distinct functions, however this de-clutters the protocol a bit.
      ;;   Since the Java API has arities that are not distinct and by type, it is either this or more functions on the protocol. You saw nothing.
      (cond
        ;;this is like one of those cards in the back of a magazine...
        (sequential? topics)
        (.subscribe consumer ^List topics listener)
        (instance? Pattern topics)
        (.subscribe consumer ^Pattern topics listener)
        (string? topics)
        (.subscribe consumer ^List (vec [topics]) listener)
        :else (throw (ex-info "topics must be a sequence of topic strings, a topic string, or a regular expression pattern." {:topics topics})))))
  (partition-subscriptions [_]
    "Returns a set of the names of any currently subscribed topics.
    Will return the same topics used in the most recent call to `(subscribe-to-partitions! c topics)`, or an empty set if no such call has been made."
    ;;FIXME: I think it might be worth parsing the results out here instead of returning these nasty subscription strings. Le sigh.
    (->> (.subscription consumer)
         (codec/decode)))
  (clear-subscriptions! [_]
    "Clears any subscriptions, and thus currently assigned partitions to this consumer."
    (.unsubscribe consumer))
  ManualPartitionAssignor
  (assign-partitions! [_ topic-partitions]
    "Manually assign a list of topic partitions to this consumer.

    > Note: It is an error to both subscribe and assign partitions manually using the same consumer.

    * Do: Assign a topic partition and manually seek to an offset if desired.
    * Don't: Subscribe to a topic partition, then assign the consumer to another topic partition, the same partition. Seriously, don't do it.

    Example:

    `(assign-partitions! c [{:topic \"piles-of-logs\" :partition 0} {:topic \"piles-of-logs\" :partition 1} {:topic \"overpriced-things\" :partition 999}])`"
    ;;It remains a mystery to me why the Java method accepts a list of topic partitions rather than a set
    ;;Conversely, the assignments call returns a set of topic partitions (probably correct), but alright, let's just live on the wild side and not force passing a set here.
    (->>
      topic-partitions
      (codec/maps->topic-partitions)
      (.assign consumer)))
  (clear-partition-assignments! [_]
    "Clears any currently assigned partitions to this consumer."
    ;;this may seem weird since we're not subscribing in the manual partition assignment case,
    ;; but calling this actually clears assigned partitions, it's a bad method name in Java
    (.unsubscribe consumer))
  KafkaMeasurable
  (metrics [_]
    (->>
      (.metrics consumer)
      (codec/decode))))

(s/defn make-consumer :- FranzConsumer
  "Create a Kafka Consumer from a configuration, with optional deserializers and optional consumer options.
   If a callback is given, call it when stopping the consumer.
   If deserializers are provided, use them, otherwise expect deserializers via class name in the config map.

   This consumer is a wrapper of Kafka Java Consumer API.
   It provides a Clojure (ish) wrapper, with Clojure data structures to/from Kafka, and implements various protocols to
   allow more specialized consumers following this implementation.
   If you prefer a lower-level implementation or wish to test your consumer, you may wish to browse this implementation
   and implement one or all the protocols provided.

   This consumer provides implementations for both a manual and automatic consumer. You must not mix and match
   automatic and manual consumption. If you do violate this rule, an exception will be thrown. Generally, this means
   you either need to subscribe to a specific topic partition to receive an automatic assignment, or manually assign
   yourself.

   Moreover, it is important to note that the offset position will be determined by your consumer configuration and
   whether or not you are saving offsets in Kafka itself, or an external location. If you need to manually reset or position
   the consumer offset in a particular partition, you can seek to it directly. Seeking will only work after partition assignment.
   For a subscription-based consumer, it is an error to seek before being assigned a partition.
   If you want to seek on assignmen for a subscription-based consumer, please do so using a callback to guarantee you
   have been assigned a valid partition.

  For per function documentation, please see the source for extensive comments, usage examples, etc.

   > Note: This implementation stresses a reasonable compromise between raw performance, extensibility, and usability, all things considered as:

   1. A wrapper
   2. Clojure

   Consumer options serve the following purposes:

   * Avoid repeated/inconvenient passing of defaults to various methods requiring options such as timeouts. Many consumers do not need per-call options.
   * Long-term extensibility as more features are added to this client, mitigating signature changes and excessive arities
   * Cheaper lookups and smaller memory footprint as the options are created in final form as records.
   * Dynamic construction of consumer options via stream processors, back-off logic, etc.
   * Reduction in garbage collection for consumers that do not need per-call options. Overall, less intermediate maps and reified objects.
   * Avoid slow memory allocations for the aforementioned cases.
   * Mitigate Kafka Java API changes. The API has often been in flux and sometimes it is necessary for extra options to handle weirdness from Java API bugs.

   > Note: Consumer options are distinct from the Kafka Consumer Configuration."
  ([config :- cs/ConsumerConfig]
    (make-consumer config nil))
  ([config :- cs/ConsumerConfig
    options :- (s/maybe cs/ConsumerOptions)]
    (-> config
        ^Properties (config-codec/encode)
        (KafkaConsumer.)
        (FranzConsumer. (defaults/make-default-consumer-options options))))
  ([config :- cs/ConsumerConfig
    key-deserializer :- Deserializer
    value-deserializer :- Deserializer]
    (make-consumer config key-deserializer value-deserializer nil))
  ([config :- cs/ConsumerConfig
    key-deserializer :- Deserializer
    value-deserializer :- Deserializer
    options :- (s/maybe cs/ConsumerOptions)]
    (-> config
        ^Properties (config-codec/encode)
        (KafkaConsumer. key-deserializer value-deserializer)
        (FranzConsumer. (defaults/make-default-consumer-options options)))))
