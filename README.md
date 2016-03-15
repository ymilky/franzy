# Franzy

Franzy is a Clojure [Apache Kafka](http://kafka.apache.org/documentation.html) client.

The main goal of Franzy is to make life easier for producing, consuming, administering, configuring, and generally working with all things, Kafka.

## Platform

Franzy breaks up its functionality into several different libraries to minimize dependency issues, especially on differing Kafka dependencies (ex: Server vs. Consumer/Producer).

* [Franzy](https://github.com/ymilky/franzy) - This library - core client-oriented functionality, i.e. consumer, producer, schemas, more.
* [Franzy Common](https://github.com/ymilky/franzy-common) - Common functionality for any Franzy development, and useful code that doesn't require any Kafka dependencies
* [Franzy Admin](https://github.com/ymilky/franzy-admin) - Administer Kafka with Clojure, get Clojure data in/out.
* [Franzy Mocks](https://github.com/ymilky/franzy-mocks) -  Test your consumers and producers without a running Kafka cluster, and more in the future.
* [Franzy Nippy](https://github.com/ymilky/franzy-nippy) - Nippy Serializer/Deserializer for Kafka.
* [Franzy Fressian](https://github.com/ymilky/franzy-fressian) - Fressian Serializer/Deserializer for kafka.
* [Franzy JSON](https://github.com/ymilky/franzy-json) - JSON/SMILE Serializer/Deserialzer for Kafka.
* [Franzy Transit](https://github.com/ymilky/franzy-transit) - Transit Serializer/Deserializer for Kafka, coming soon.
* [Franzy Embedded](https://github.com/ymilky/franzy-common) - Full featured embedded Kafka server for testing/dev, with multiple implementations including concrete types and components.
* [Franzy Examples](https://github.com/ymilky/franzy-examples) - Growing project of examples using all the above, to learn at your leisure.

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

## Installation

Don't - Currently undergoing peer-review and field testing. But you're welcome to try. Please provide feedback if you encounter an issue.

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
(my.ns
  (:require [franzy.serialization.deserializers :as deserializers]
            [franzy.serialization.serializers :as serializers]))
```

For the add-ons you'll have to reference them as separate dependencies obviously. They follow a pattern like this, replacing `nippy` with the serializer/deserializer name:

```clojure
(my.ns
  (:require [franzy.serialization.nippy.deserializers :as deserializers]
            [franzy.serialization.nippy.serializers :as serializers]))
```


See [Serializers](https://github.com/ymilky/franzy/blob/master/doc/serialization.md) for a discussion.

### Producers

Creating a producer, using some options just like in a Kafka properties file, but more Clojure-like:

```clojure
(let [pc {:bootstrap.servers ["127.0.0.1:9092"]
          :retries           1
          :batch.size        16384
          :linger.ms         1
          :buffer.memory     33554432}
      ;;normally, just inject these direct and be aware some serializers may need to be closed,
      ;; adding to binding here to make this clear
      key-serializer (serializers/string-serializer)        ;;Serializes producer record keys as strings
      value-serializer (serializers/string-serializer)      ;;Serializes producer record values as strings
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
        topic " land-wars-in-asia "
        partition 0]
    (with-open [p (producer/make-producer pc key-serializer value-serializer options)]
      (let [send-fut (send-async! p topic partition :inconceivable {:things-in-fashion
                                                                    [:masks :giants :kerry-calling-saul]} options)
            record-metadata (send-sync! p " land-wars-in-asia " 0 :conceivable
                                        {:deadly-poisons [:iocaine-powder :ska-music :vegan-cheese]}
                                        options)
            ;;we can also use records to produce, wrapping our per producer record value (data) as usual
            record-metadata-records (send-sync! p (pt/->ProducerRecord topic partition :vizzini
                                                                       {:quotes [" the battle of wits has begun!"
                                                                                 "finish him, your way!" ]})
                                                options)]
        (println " Sync send results: " record-metadata)
        (println " Sync send results w/record: " record-metadata-records)
        (println "Async send results: " @send-fut))))
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

It is vitally important that you undestand the implicaitons of threading, polling, partition assignments, and offsets. This is documented in the official [Kafka](http://kafka.apache.org/documentation.html)  docs, go read it, now, and the [consumer Java API](https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html) docs too.

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
      ;(clojure.pprint/pprint (into [] inconceivable-transduction cr))
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

## Contributing/Roadmap

This library is still very young and is surely filled with bugs. Pull requests are welcome.

Of particular concern/value to fix/refactor/enhance currently:

* Schemas - raw, and while working, may have mistakes, which in part may be to incorrect Kafka documentation. While the Kafka source was used for some of the harder parts of this library, much of the schema came from reading the documentation. Many mistakes have already been caught. Moreover, as Kafka grows and changes, config values are often the most in flux.
* Serializers - More will be added as needed. Certainly the existing serializers can be enhanced and are meant as a blue-print and general usage. As your usage may vary, you may wish to fork or modify the existing serializers accordingly. In order to avoid this, options are easily injected into most serializers. Additional features can also be easily added by closing over values.
* Testing - more unit tests are required, but adding them is an ongoing effort.
* Field/Integration testing - If you're using this library in the wild, it may be too early, but I'd love to hear from you and fix any problems.
* Async helpers, examples, particularly with core.async and/or manifold
* Useful transformations/eductions/transductions/whatever for working with the various data structures to/from Kafka

Please be aware many problems/issues may be due to Kafka itself or the Java API. As such, before submitting issues, please check these issue trackers first. If there is a reasonable workaround or solution, please leave a note and link to the underlying issues.

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
