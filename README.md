# Franzy

Franzy is a suite of Clojure libraries for [Apache Kafka](http://kafka.apache.org/documentation.html). It includes libraries for Kafka consumers, producers, partitioners, callbacks, serializers, and deserializers. Additionally, there are libraries for administration, testing, mocking, running embedded Kafka brokers and zookeeper clusters, and more.

The main goal of Franzy is to make life easier for working with Kafka from Clojure. Franzy provides a foundation for building higher-level abstractions for whatever your needs might be.

## Platform

Franzy breaks up its functionality into several different libraries to minimize dependency issues, especially on differing Kafka dependencies (ex: Server vs. Consumer/Producer).

| Name                                                         | Type               | Description                                                                                                                 | Major Dependencies                                   |
|--------------------------------------------------------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| [Franzy](https://github.com/ymilky/franzy)                   | client             | This library - core client-oriented functionality, i.e. consumer, producer, schemas, more.                                  | Franzy-Common, Kafka client                          |
| [Franzy Admin](https://github.com/ymilky/franzy-admin)       | client             | Administer Kafka with Clojure, get Clojure data in/out, create topics, add partitions, list brokers, etc.                   | Franzy-Common, Kafka server (Scala/Java)             |
| [Franzy Common](https://github.com/ymilky/franzy-common)     | lib                | Common functionality for any Franzy development, and useful for Kafka in general                                            | Clojure, Schema                                      |
| [Franzy Nippy](https://github.com/ymilky/franzy-nippy)       | de/serializer      | Nippy Serializer/Deserializer for Kafka.                                                                                    | [Nippy](https://github.com/ptaoussanis/nippy)        |
| [Franzy Transit](https://github.com/ymilky/franzy-transit)   | de/serializer      | Transit Serializer/Deserializer for Kafka.                                                                                  | [Transit](https://github.com/cognitect/transit-clj)  |
| [Franzy JSON](https://github.com/ymilky/franzy-json)         | de/serializer      | JSON/Smile Serializer/Deserializer for Kafka.                                                                               | [Cheshire](https://github.com/dakrone/cheshire)      |
| [Franzy Fressian](https://github.com/ymilky/franzy-fressian) | de/serializer      | Fressian Serializer/Deserializer for Kafka.                                                                                 | [Fressian](https://github.com/clojure/data.fressian) |
| [Franzy Avro](https://github.com/ymilky/franzy-avro)         | de/serializer      | AVRO Serializer/Deserializer for Kafka.                                                                                     | [Abracad](https://github.com/damballa/abracad/blob/master/src/clojure/abracad/avro/edn.clj)                                                  |
| [Franzy Embedded](https://github.com/ymilky/franzy-embedded) | embedded broker    | Full featured embedded Kafka server for testing/dev, with multiple implementations including concrete types and components. | Kafka server                                         |
| [Franzy Mocks](https://github.com/ymilky/franzy-mocks)       | testing            | Test your consumers and producers without a running Kafka cluster, and more in the future.                                  | Franzy, Kafka client                                 |
| [Franzy Examples](https://github.com/ymilky/franzy-examples) | examples           | Growing project of examples using all the above, to learn at your leisure.                                                  | All                                                  |
| [Travel Zoo](https://github.com/ymilky/travel-zoo)           | embedded Zookeeper | Embedded Zookeeper servers and clusters for testing and development, with concrete type and component versions available.   | Curator Test                                         |
## Features

* Support for Kafka 0.9 (and above)
* Clojure types in and out of Kafka, no worrying about the Java API or Java types
* Support for Clojure 1.8+
* A light core of external dependencies to keep things light, future-proof, and in minimal conflict with your code
* Comprehensive consumer API with support for both manual and automatic partition assignment as well as offset management
* Producer API with support for synchronous and asynchronous production
* Support for metadata and metrics
* Choice of partitioning strategies (round-robin, range) and simple helpers/framework to implement your own
* Validation for all significant data types, including configuration, via schema
* Full, validated configuration from Clojure for Consumers, Producers, Brokers, and Kafka Connect - build your config as data
* Protocols and conversions for implementing your own consumers, producers, tests, conversions, and more
* Mock producer and consumer, via [Franzy-Mocks](https://github.com/ymilky/franzy-mocks)
* Comprehensive Admin interface, including wrapping many undocumented/command-line only features via [Franzy-Admin](https://github.com/ymilky/franzy-admin)
* Helpers/framework for implementing custom callbacks for producers and consumers
* Helpers/framework for implementing your own serializers/deserializers
* Built-in serializers for keys and values for many data types and formats, including Strings, Integers, Longs, UUID, and Clojure Keywords, and EDN
* Add-on serializers for Nippy, JSON/JSON SMILE, and Fressian, with more to come
* A set of custom record types that fully wrap any data returned to and from Kafka, if you need, want, or prefer to use records rather than pure maps
* Ability to pass any complex parameters using provided record types which also conform to validateable schemas
* Embedded Kafka Server and components for testing via [Franzy-Embedded](https://github.com/ymilky/franzy-embedded)
* Extensive examples, code comments, and documentation
* More, coming soon....

## Why?

In addition to raw features, some reasons you may want to use Franzy:

* Comprehensive Kafka client
* Extreme care to not remove, distort, break, or diminish anything in the existing Java API
* Sane balance of performance vs. Clojure best-practices vs. ease-of-use
* Does not force any viewpoint about producing, consuming, administration, etc. on you beyond what Kafka already does
* À la carte - Lots of goodies and sugar, even for projects that are using mostly Java or don't need the consumer or producer at all. Build out what you need, no more, no less.
* Currently being used in a real project, where Kafka is the "spine" of the application, and thus, must be updated, fixed, and changed as needed
* Relatively future proof
* Designed to be a good fit with stream processors, particularly [Onyx](https://github.com/onyx-platform/onyx)
* See [Rationale](https://github.com/ymilky/franzy/blob/master/doc/rationale.md)

## Requirements

Requirements may vary slightly depending on your intended usage.

* Clojure 1.8+ - You may be able to compile this library on/with earlier versions, but this is untested.
* Kafka 0.9+ - Some parts may work on earlier versions, but this is untested.

A good way to get started with Kafka is to use Docker and/or Vagrant. I recommend using a Docker compose stack with Kafka and Zookeeper that lets you scale up/down to test. You can also use the embedded Kafka and Zookeeper libraries listed above and discussed in the Testing/Dev section.

## Installation

These libraries have had a few weeks of peer review and no issues thus far. I will be releasing some new versions shortly as I have time in the coming weeks. Thus far, there are no breaking API changes but I am open to any suggested changes or submissions. Please let me know and be ready for an upgrade soon. Thanks for your support.

```
[ymilky/franzy "0.0.1"]
```

[![Clojars Project](https://img.shields.io/clojars/v/ymilky/franzy.svg)](https://clojars.org/ymilky/franzy)

## Docs

* Read the browsable [API Docs](http://ymilky.github.io/franzy/)
* [Franzy Examples](https://github.com/ymilky/franzy-examples) for lots of notes, advice, and growing examples
* See source for more information about schemas, types, etc.
* For more about using, validating, and developing schemas, see [Schema](https://github.com/plumatic/schema)
* Commented source and tests
* See the doc folder for more.

## Usage

The best way to learn is [Franzy Examples](https://github.com/ymilky/franzy-examples) and viewing the [API docs](http://ymilky.github.io/franzy/), source, etc.

Below are a few naive examples to get you started.

### Serialization

You'll need to pick a format in/out of Kafka.

I recommend you use [Franzy-Nippy](https://github.com/ymilky/franzy-nippy), but think carefully about your use-case. If you're just getting started, the built-in EDN Serializer is a good choice to keep things simple. Of course, all the built-in serializers in Kafka are accessible as well.

For the built-in serializers/deserializers, simply do something like this:

```clojure
(ns my.ns
  (:require [franzy.serialization.deserializers :as deserializers]
            [franzy.serialization.serializers :as serializers]))
```

For the add-ons you'll have to reference them as separate dependencies obviously. They follow a pattern like this, replacing `nippy` with the serializer/deserializer name:

```clojure
(ns my.ns
  (:require [franzy.serialization.nippy.deserializers :as deserializers]
            [franzy.serialization.nippy.serializers :as serializers]))
```


See [Serializers](https://github.com/ymilky/franzy/blob/master/doc/serialization.md) for a discussion.

### Producers

For some general information about producers, check the source for many comments, read the browsable api, and skim this short, but growing [producer guide](https://github.com/ymilky/franzy/blob/master/doc/producers.md).

There are many ways to use and create producers. Below are a few naive examples of creating producers.

```clojure
;;Creating a producer with a few simple config values and options to show what can be done, your usage will vary

(let [pc {:bootstrap.servers ["127.0.0.1:9092"]
          :retries           1
          :batch.size        16384
          :linger.ms         1
          :buffer.memory     33554432}
      ;;normally, just inject these direct and be aware some serializers may need to be closed,
      ;; adding to binding here to make this clear
      
      ;;Serializes producer record keys as strings
      key-serializer (serializers/string-serializer)
      ;;Serializes producer record values as strings
      value-serializer (serializers/string-serializer)
      ]
  (with-open [p (producer/make-producer pc key-serializer value-serializer)]
    (partitions-for p "test")))

;;=>
[{:topic "test",
  :partition 0,
  :leader {:id 1001, :host "127.0.0.1", :port 9092},
  :replicas [{:id 1001, :host "127.0.0.1", :port 9092}],
  :in-sync-replicas [{:id 1001, :host "127.0.0.1", :port 9092}]}]
```

Synchronous and asynchronous production, using a different producer arity:

```clojure
(let [;;Use a vector if you wish for multiple servers in your cluster
      pc {:bootstrap.servers ["cliffs-of-insanity.guilder:9092" "fire-swamp.guilder:9092"]}
        ;;Serializes producer record keys that may be keywords
        key-serializer (serializers/keyword-serializer)
        ;;Serializes producer record values as EDN, built-in
        value-serializer (serializers/edn-serializer)
        ;;optionally create some options, even just use the defaults explicitly
        ;;for those that don't need anything fancy...
        options (pd/make-default-producer-options)
        topic "land-wars-in-asia"
        partition 0]
    (with-open [p (producer/make-producer pc key-serializer value-serializer options)]
      (let [send-fut (send-async! p topic partition :inconceivable {:things-in-fashion
                                                                    [:masks :giants :kerry-calling-saul]} options)
            record-metadata (send-sync! p "land-wars-in-asia" 0 :conceivable
                                        {:deadly-poisons [:iocaine-powder :ska-music :vegan-cheese]}
                                        options)
            ;;we can also use records to produce, wrapping our per producer record value (data) as usual
            record-metadata-records (send-sync! p (pt/->ProducerRecord topic partition :vizzini
                                                                       {:quotes ["the battle of wits has begun!"
                                                                                 "finish him, your way!" ]})
                                                options)]
        (println "Sync send results:" record-metadata)
        (println "Sync send results w/record:" record-metadata-records)
        (println "Async send results:" @send-fut))))
```

### Consumer Overview

In order to consume data from Kafka, you must have one or more valid partitions per topic assigned. There are 2-ways to get a partition assignment from Kafka - manually and automatically.

The manual-assignment case is well suited to users who are tracking offsets manually or want to override and commit offset positions. Automatic assignment is performed via subscription and best used when storing offsets in Kafka itself.

The process to consume data from Kafka follows a pattern something like this:

* Setup threads for consuming
* Assign or Subscribe (Automatically assign) at least one partition from a given topic to the consumer
* Resume consumption from last committed offset or seek to a specific offset to determine where consumptions begins
* Begin consuming by polling in a polling thread, repeating this step to the end of time
* Close the consumer when finished (or time itself ends)

It is vitally important that you understand the implications of threading, polling, partition assignments, and offsets. This is documented in the official [Kafka](http://kafka.apache.org/documentation.html)  docs, go read it, now, and the [consumer Java API](https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html) docs too.

This is perhaps an over-simplification as there are a few other nuances.

For the impatient among you, the major differences between the "manual" consumer and "subscription" or "automatic" consumer are generally offset management and partition assignment.

Franzy lets you choose, and there's nothing stopping you from manually assigning offsets.
Likewise, there's nothing stopping you from committing offsets to Kafka itself manually (rather than Zookeeper, Redis, Aerospike, Cassandra, etc.).

You will find most of what works for the manual and subscription consumers is the same. The details are mainly in which protocols you use. If you want to force one paradigm over another, simply don't call or require the protocols of the other.

It is extremely important to note that consuming via subscriptions and using manual assignments are mutually exclusive. If you attempt to do so at the same time, your code will fail. Although Kafka itself will protect you from any adverse effects of this behavior by throwing an exception, it is not guaranteed that this behavior will remain in future versions as the Kafka API changes.

The examples given here do not create threads for the sake of simplicity. I leave this as an exercise to you as your use-cases will determine your threading model. My colleagues and I prefer to use core.async which makes it simple to transduce results directly on to a channel, manage thread lifecycles, etc. We've also used core.async with Manifold to great success. Understand that when you poll, work with offsets, etc., many of these are blocking operations.

### Manual Consumer

Below, we create a manual consumer and demo a bit of the important assignment capabilities you might want to use when working with a manual consumer:

```clojure
  (let [cc {:bootstrap.servers ["127.0.0.1:9092"]
            :group.id          "hungry-eels"
            :auto.offset.reset :earliest}
        ;;notice now we are using keywords, to ensure things go as we planned when serializing
        key-deserializer (deserializers/keyword-deserializer)
        ;;notice now we are using an EDN deserializer to ensure we get back the data correctly
        value-deserializer (deserializers/edn-deserializer)
        options (cd/make-default-consumer-options)
        topic "land-wars-in-asia"
        ;;notice, we are setting the topics and partitions to what we produced to earlier...
        topic-partitions [{:topic topic :partition 0}]]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer options)]
      ;;first, lets get some information about the currently available topic partitions...
      ;;we will see a list of topics, along with partition info for each one
      (println "Topic Partition Info per Topic:" (list-topics c))
      ;;maybe you just want an eager list of topics, that's it....a simple solution with many possible solutions
      (println "An inefficient vector of topics:" (->> (list-topics c)
                                                       ;;or (keys), but here we want to stringify our keys a bit
                                                       (into [] (map (fn [[k _]] (name k))))))
      ;;something more specific in scope
      (println "Topic Partitions for our topic:" (partitions-for c topic))
      ;;now let us manually assign a partition
      ;;if you really wanted some dynamic behavior, you could use some of the results above from list-topics
      (assign-partitions! c topic-partitions)
      ;;list the assigned partitions - shocking revelations follow:
      (println "Assigned Partitions:" (assigned-partitions c))
      ;;now lets say we don't like to be labeled, and thus, we don't want any more assignments
      (println "Clearing partition assignments....")
      (clear-partition-assignments! c)
      (println "After clearing all partition assignments, we have exactly this many assignments (correlates to wall-street accountability):"
               (assigned-partitions c))))
```

### Subscription-based Consumer

Below, we create a subscription-based consumer that auto-commits its offsets to Kafka. A point of interest that applies both to the subscription-based consumer and the manual consumer is working with consumer records.


```clojure
(let [cc {:bootstrap.servers       ["127.0.0.1:9092"]
          :group.id                "submissive-blonde-aussies"
          ;;jump as early as we can, note this isn't necessarily 0
          :auto.offset.reset       :earliest
          ;;here we turn on committing offsets to Kafka itself, every 1000 ms
          :enable.auto.commit      true
          :auto.commit.interval.ms 1000}
      key-deserializer (deserializers/keyword-deserializer)
      value-deserializer (deserializers/edn-deserializer)
      topic "land-wars-in-asia"
      ;;Here we are demonstrating the use of a consumer rebalance listener. Normally you'd use this with a manual consumer to deal with offset management.
      ;;As more consumers join the consumer group, this callback should get fired among other reasons.
      ;;To implement a manual consumer without this function is folly, unless you care about losing data, and probably your job.
      ;;One could argue though that most data is not as valuable as we are told. I heard this in a dream once or in intro to Philosophy.
      rebalance-listener (consumer-rebalance-listener (fn [topic-partitions]
                                                        (println "topic partitions assigned:" topic-partitions))
                                                      (fn [topic-partitions]
                                                        (println "topic partitions revoked:" topic-partitions)))
      ;;We create custom producer options and set out listener callback like so.
      ;;Now we can avoid passing this callback every call that requires it, if we so desire
      ;;Avoiding the extra cost of creating and garbage collecting a listener is a best practice
      options (cd/make-default-consumer-options {:rebalance-listener-callback rebalance-listener})]
  (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer options)]
    ;;Note! - The subscription will read your comitted offsets to position the consumer accordingly
    ;;If you see no data, try changing the consumer group temporarily
    ;;If still no, have a look inside Kafka itself, perhaps with franzy-admin!
    ;;Alternatively, you can setup another threat that will produce to your topic while you consume, and all should be well
    (subscribe-to-partitions! c [topic])
    ;;Let's see what we subscribed to, we don't need Cumberbatch to investigate here...
    (println "Partitions subscribed to:" (partition-subscriptions c))
    ;;now we poll and see if there's any fun stuff for us
    (let [cr (poll! c)
          ;;a naive transducer, written the long way
          filter-xf (filter (fn [cr] (= (:key cr) :inconceivable)))
          ;;a naive transducer for viewing the values, again long way
          value-xf (map (fn [cr] (:value cr)))
          ;;more misguided transducers
          inconceivable-transduction (comp filter-xf value-xf)]

      (println "Record count:" (record-count cr))
      (println "Records by topic:" (records-by-topic cr topic))
      ;;;The source data is a seq, be careful!
      (println "Records from a topic that doesn't exist:" (records-by-topic cr "no-one-of-consequence"))
      (println "Records by topic partition:" (records-by-topic-partition cr topic 0))
      ;;;The source data is a list, so no worries here....
      (println "Records by a topic partition that doesn't exist:" (records-by-topic-partition cr "no-one-of-consequence" 99))
      (println "Topic Partitions in the result set:" (record-partitions cr))
      (clojure.pprint/pprint (into [] inconceivable-transduction cr))
      ;(println "Now just the values of all distinct records:")
      (println "Put all the records into a vector (calls IReduceInit):" (into [] cr))
      ;;wow, that was tiring, maybe now we don't want to listen anymore to this topic and take a break, maybe subscribe
      ;;to something else next poll....
      (clear-subscriptions! c)
      (println "After clearing subscriptions, a stunning development! We are now subscribed to the following partitions:"
               (partition-subscriptions c)))))
```

### Working with Consumer Results

You may have noticed in earlier examples we were binding the result set and then doing operations like take, into, etc. on it. That's because Kafka returns richer results than just a map, though you can simply skip the binding and use a map if you like.

Consumers get results by polling Kafka until a timeout, then repeating over-and-over. The consumer will get what it asks for based on the position of the consumer and other consumers influencing this within its consumer group. A poll will return "consumer records" which is an object containing 0 or more instances of "consumer record" objects. Franzy uses records to represent these for speed, memory footprint, and ease of use.

If you don't understand how consumers with Kafka work, you must read more in the official [Kafka](http://kafka.apache.org/documentation.html) documentation. This is crucial for any Kafka client library to be useful and not seem "broken" to you.

A common problem that new Kafka users have is that they do not understand the consumption model. Many new users assume the server or the client library must be broken when no results are returned. Take a moment, ensure you understand, read through [franzy-examples](http://wwww.github.com), and try some examples interactively with your cluster.

The following capabilities are available to you when working with consumer records:

* Lazy/Non-lazy chunked/unchunked access to results from Kafka, with optional transducers applied without excessive intermediate objects.
* Full fidelity of the results returned from Kafka (by topic, partition, all, record count, and future additions from the Java side). Nothing lost, much gained.
* Ability to further slice records via transducer or by calling built-in functions to slice on topic or topic partition.
* Preservation of the result type and referential transparency of results from Kafka. No inadvertent consumption of iterators or eagerly realizing things if not desired.
* Ability to call seq operations via Seqable implementation, and return only Clojure types consistent with the rest of the API.
* Ability to reduce the result set itself in a high performance way via IReduceInit, and return only Clojure types consistent with the rest of the API.
* Frees client implementations, testing, etc. from dealing with this behavior - no complecting the client implementation with handling the result set behavior.
* Ability to override the result set output by introducing your own conversions early in sequence creation or by extending the codec protocol used for result conversions.

### Offset Management

Franzy supports both automatically committing offsets to Kafka and manually managing offsets yourself.

Callbacks for offset commits, consumer rebalance events, and more are provided to help you with this process depending on your needs. If you are managing your own offsets, please use a highly available datastore that is reasonably fast. You may trade reliability for speed if you don't care about losing data.

The following code demonstrates some offset management operations and gotchas with Kafkas that newcomers often struggle with:

```clojure
(let [cc {:bootstrap.servers ["127.0.0.1:9092"]
          :group.id          "hungry-eels"
          :auto.offset.reset :earliest}
      key-deserializer (deserializers/keyword-deserializer)
      value-deserializer (deserializers/edn-deserializer)
      options (cd/make-default-consumer-options)
      topic "land-wars-in-asia"
      first-topic-partition {:topic topic :partition 0}
      second-topic-partition {:topic topic :partition 1}
      topic-partitions [first-topic-partition second-topic-partition]]
  (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer options)]
    ;;first we'll make sure we can assign some partitions. We could also subscribe instead, but for examples, this is easier.
    (assign-partitions! c topic-partitions)
    (seek-to-beginning-offset! c topic-partitions)
    ;;let's peek at what the next offset is.....it should be 0 if we're at the beginning
    (println "Next offset:" (next-offset c first-topic-partition))
    ;;now maybe we want to save some metadata about the beginning offset....
    ;;Notice, we're sending a map with the keys a topic partition map as the key, and the value as an offset metadata map
    (commit-offsets-sync! c {first-topic-partition {:offset 0, :metadata "In the beginning.....that was a long time ago."}})
    ;;Now let's have a peek at what we committed. If you've done this before, there might be other data obviously
    (println "Committed offsets so far:" (committed-offsets c first-topic-partition))
    ;;Now let's commit the next offset (there should be one if you produced data already), but this time async
    (commit-offsets-async! c {first-topic-partition {:offset 1 :metadata "Those who count from one, are but two."}})
    ;;Another peek at the results, but this might surprise you if your thinking cap is at the cleaners
    (println "Committed offsets after first async call:" (committed-offsets c first-topic-partition))
    ;;The problem here is you passed the offsets as the options map! Don't do it.
    ;; OK, if not then what about other arities?
    (commit-offsets-async! c {first-topic-partition {:offset 1 :metadata "Those who count from one, are but two."}} nil)
    (println "Committed offsets after proper async call:" (committed-offsets c first-topic-partition))
    ;;Nope, still no new data, but what about doing it sync
    (commit-offsets-sync! c {first-topic-partition {:offset 1 :metadata "Those who count from one, are but two."}})
    (println "Committed offsets after 2nd sync call:" (committed-offsets c first-topic-partition))
    ;;OK, great, doing it sync worked, but why?
    ;;Let's create some callbacks so we have a better idea what is going on
    ;;We could use these to do all kinds of fun stuff, like store this metadata in our own shiny database
    (let [occ (offset-commit-callback (fn [offset-metadata]
                                        (println "By the wind shalt be, commit succeeded:" offset-metadata))
                                      (fn [e]
                                        (println "Offsets failed to commit, just like you:" e)))]
      ;;notice the different arity and the fact we pass our callback.
      ;; We could have also just set this in the consumer options, in which case, there would be no need to use this arity
      ;; Unless the callback changed per-call, in which case, someone somewhere has read your code, then engaged the grumble-drive.
      ;;beware of committing async offsets in a separate thread from the poller
      (commit-offsets-async! c {first-topic-partition {:offset 2 :metadata "A Nancy to a Tanya"}} {:offset-commit-callback occ})
      (println "Committed offsets after async callback version:" (committed-offsets c first-topic-partition))
      ;;ok, why are there still no offsets?
      ;;let's try to follow the Franzy documentation! READ IT!
      ;;first, let's poll from offset 2, so we'll need to seek to it
      (seek-to-offset! c first-topic-partition 2)
      ;;and to poll, results are not important as long as we got at least 1 - you did populate the data, didn't you?
      (poll! c)
      (commit-offsets-async! c {first-topic-partition {:offset 2 :metadata "A Nancy to a Tanya"}} {:offset-commit-callback occ})
      (println "Committed offsets after listening to the doc about polling with async commits:" (committed-offsets c first-topic-partition))
      ;;all is well, that was certainly traumatic.....
      )))
```

### Metrics

Producing and consuming from Kafka is not an exact science. Fortunately, Kafka provides a metrics API that you can use in addition to any self-collected metrics to help determine when to use back-pressure, spawn threads, kill threads, fire up new machines, etc.

The code for a producer and consumer uses the same protocol. The consumer case is demonstrated below.

```clojure
  (let [cc {:bootstrap.servers ["127.0.0.1:9092"]
            :group.id          "mawage"}
        key-deserializer (deserializers/keyword-deserializer)
        value-deserializer (deserializers/edn-deserializer)
        options (cd/make-default-consumer-options)]
    (with-open [c (consumer/make-consumer cc key-deserializer value-deserializer options)]
      ;;Now let's say we want to know something about how consuming is going. Perhaps we are too greedy.
      ;;We can get a plethora of metrics, log them, exert back-pressure on the producer if needed, eject, etc.
      ;;All of this, by parsing this wonderful thing below. JMXers, rejoice.
      ;; If there is more of a demand, we can add more transducers, helpers, etc. for metrics
      ;;WARNING - prepare your REPL for a feast. You won't receive any real values unless you've kept the consumer consuming....
      (metrics c)))

;;=> example output....truncated
{:name "io-ratio",
  :description "The fraction of time the I/O thread spent doing I/O",
  :group "consumer-metrics",
  :tags {:client-id "consumer-55"}} {:metric-name {:name "io-ratio",
                                                   :description "The fraction of time the I/O thread spent doing I/O",
                                                   :group "consumer-metrics",
                                                   :tags {:client-id "consumer-55"}},
                                     :value 0.0}
```

### Partitioners

The default partioners are provided if you want to pass them around in Clojure or for consumer configuration:

* Round Robin Assignor - `round-robin-assignor`
* Range Assignor `range-assignor`

```clojure
(ns my.ns
  (:require [franzy.clients.consumer.partitioners :as partitioners]))
```

If you want to control how your partition assignments are laid-out and assigned to consumers, you can implement your own partitioner. A protocol is also available `FranzPartitionAssignor` that will allow you to write your partition assignor with a protocol, for example via defrecord or deftype. You can then call `make-partition-assignor` and it will turn your protocol into a valid interface implementation. You should prefer to simply implement the `PartitionAssignor` interface directly for better performance as the protocol is only meant as a crude-shim for existing code.

### Types/Records

Various concrete type implementations are scattered throughout Franzy and add-ons. Using these will in some cases give you a performance boost and/or reduced memory consumption, and avoid reflection in other cases. This is particularly relevant and beneficial if you find yourself allocating large batches of maps for production, consumption, admin bulk operations, offset management, etc.

Most records are stored in `types.clj` files across many namespaces. These correspond directly to the context they are used, i.e. common, consumer, producer, admin, etc.

For example, you can use the ProducerRecord Clojure record like-so:

```clojure
(ns my.ns
  (:require [franzy.clients.producer.types] :as pt))

;;allocating a few hundred thousand of these, it's just a record....  
(pt/->ProducerRecord topic partition my-glorious-key my-odoriferous-eminating-value)
```

More importantly, these can easily be validated since each type has a corresponding schema, usually in a `schema.clj` file.

### Validation

Franzy provides validation for all map structures and data types used. You may use this functionality even if you are not interested in the rest of Franzy.

Schemas for validation are created and validation using the excellent [Schema](https://github.com/plumatic/schema) library.


Simple configuration validation example:

```clojure
(let [cc {:bootstrap.servers ["127.0.0.1:9092"]}]
    (s/validate cs/ConsumerConfig cc))

;;valid config
;;=> {:bootstrap.servers ["127.0.0.1:9092"]}

(let [cc {:bootstrap.servers ["127.0.0.1:8080"]
          :timeout.typo 123
          :auto.commit.interval.ms "forever"
          :auto.offset.reset nil}]
    (s/validate cs/ConsumerConfig cc))

;;=> ExceptionInfo Value does not match schema: {:auto.offset.reset (not (#{:latest :earliest :none} nil)), :auto.commit.interval.ms (throws? (GreaterThanOrEqualToZero? "forever")), :timeout.typo disallowed-key}  schema.core/validator/fn--4313 (core.clj:151)
```

Schemas are constantly being tweaked and exist for the following so far:

* Producer Configuration
* Consumer Configuration
* Broker Configuration
* Kafka Connect Configuration
* Topic Configuration
* Producer Types
* Consumer Types
* Admin Types
* Admin Zookeeper Types
* General Types used in Kafka

Most of the above will help you validate just about any map that comes in/out of Kafka should you choose. Validation can be toggled on/off thanks to schema and by your implementation.

### Broker Discovery

If you need to discover one or more available brokers, there are a few ways to do this:

* Use [Franzy-Admin](https://github.com/ymilky/franzy-admin) and one of the broker functions such as `all-brokers` - this will give you a full list of brokers for all channels, including Plaintext, SSL, SASSL, etc. This is likely the preferred discovery method for most use-cases. You can also perform more specific queries by channel, broker id, and more using some of the functions in [cluster.clj](https://github.com/ymilky/franzy-admin/blob/master/src/franzy/admin/cluster.clj).
* If you already are inside a consumer or producer, `list-topics` and `partitions-for` return some information, based on the context.
* Query Zookeeper directly. This is generally unneeded since under the hood, Franzy-Admin does this for you, but if you are already connected to Zookeeper with an existing client, you can go ahead and do this safely. Franzy-Admin provides a few convenience functions that will return the correct paths in Zookeeper for you to query against, so no need to hard-code paths.

**coming soon - helper functions for tranducing the results of the above into useful forms for use cases such as bootstrap.servers**

### Testing/Dev

You can run an embedded cluster of Zookeeper and/or Kafka using the following together or in conjunction with other libraries that provide similar functionality:

* For embedded Kafka, see [Franzy-Embedded](https://github.com/ymilky/franzy-embedded), see tests and docs for examples.
* For embedded Zookeeper servers and clusters, see [Travel Zoo](https://github.com/ymilky/travel-zoo), see tests and docs for examples.

Both libraries above provide concrete types for auditing, avoiding reflection, ease-of-use as well as protocols and versions using component. Both also have full Clojure APIs.

Another option is to use docker containers. I have successfully tested both of the above options mixed with docker without issue. In general, be sure all servers can see each other on the network to avoid problems.

Here's at least one Docker image that uses Docker compose and includes Zookeeper and Kafka.

* https://github.com/wurstmeister/kafka-docker

## Contributing/Roadmap

This library is still very young and is surely filled with bugs. Pull requests are welcome.

The following items are planned or have received partial development, I make no guarantees on timelines but plan to release some of these items in conjunction with getting other real-world work done using them:

* Pool for holding on to consumers/producers and related objects where there is less of a clear path for managing the lifetime/instantiation of an object and disposing it. Some examples - Logging, Plugins for other libraries such as Onyx, Service Calls
* Logging directly to Kafka via Timbre - dump logs directly into Kafka, presumably to process/dump them somewhere else. Useful for people with high log volumes or need some secondary processing of logs in a system like Logstash, Samza, Onyx, etc.
* Some async helpers/patterns - Many of these might just be samples, but for more generic async tools, more may be released.
* Additional tools and testing helpers, ex: parsing broker lists from franzy-admin directly to producer/consumer connnection strings.
* Even more admin tools - combining some of the existing franzy-admin operations that are naturally part of common larger operations
* Performance tweaks - some minor optimization and tweaks where possible given real-world benchmarking and usage
* Additional partitioning strategies - ex: using different hashing techniques or supporting more narrow, but common use-cases for producer partitioning

Please contact me if any of these are high-demand for you so I can guage the urgency better.

Of particular concern/value to fix/refactor/enhance currently:

* Schemas - raw, and while working, may have mistakes, which in part may be to incorrect Kafka documentation. While the Kafka source was used for some of the harder parts of this library, much of the schema came from reading the documentation. Many mistakes have already been caught. Moreover, as Kafka grows and changes, config values are often the most in flux.
* Serializers - More will be added as needed. Certainly the existing serializers can be enhanced and are meant as a blue-print and general usage. As your usage may vary, you may wish to fork or modify the existing serializers accordingly. In order to avoid this, options are easily injected into most serializers. Additional features can also be easily added by closing over values.
* Testing - more unit tests are required, but adding them is an ongoing effort.
* Field/Integration testing - If you're using this library in the wild, it may be too early, but I'd love to hear from you and fix any problems.
* Async helpers, examples, particularly with core.async and/or manifold
* Useful transformations/eductions/transductions/whatever for working with the various data structures to/from Kafka

Please be aware many problems/issues may be due to Kafka itself or the Java API. As such, before submitting issues, please check the Kafka official issue trackers first. If there is a reasonable workaround or solution, please leave a note and link to the underlying issues.

## Contact

Find me on [Clojurians Slack](https://clojurians.slack.com/) - @ymilky

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.

## Acknowledgements

Thanks to the following people for advice, support, code, and/or inspiration:

* [Apache Kafka](http://kafka.apache.org) - Kafka Team, for the Java client, Kafka itself, docs, etc.
* [Lucas Bradstreet](https://github.com/lbradstreet) 
* A thank you to all authors of other Kafka clients - for inspiration and creating valuable libraries I used until my requirements changed
