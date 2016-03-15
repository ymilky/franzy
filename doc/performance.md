# Performance

## General

Franzy attempts to make some compromises regarding elegant code, while still trying to maintain general Clojure best practices.

Performance tuning is an ongoing concern and development activity. Relative to the native Java producer/consumer interfaces it wraps, it should be slower due to its nature. Some performance benefits related to creating results for production and consumption when interfacing with the client may be as or even more efficient than comparable Java, but it will vary on a per-implementation basis.

## Serialization

Serializers are built on well-tested libraries that offer good performance relative to most options. All provided serializers use `deftype` for usability elsewhere and performance or are pure Java.

Nippy serialization currently offer a good usability-to-compression-to-performance option.

## Conversions

Performance of consumers and producers can be further tweaked by extending and overriding built-in implementations as desired. A compromise of accurate/future-proof conversions vs. performance was made. Originally, all conversions were hand-rolled, but this lead to less extensibility and much uglier code.

Conversions are primarily done at the time of writing using protocols, with hand-rolled methods only as necessary. Clever methods of using reflection, undocumented metadata, etc. have been intentionally avoided. Likewise, using add-on libraries that make possibly accurate claims about fixing some of the underlying issues are actively avoided for now.

## Callbacks

All provided callbacks are implemented using `deftype` or `reify` for performance and usability. This was done to avoid any extra overhead imposed and to free up optimizing callbacks as a purely user-based exercise.

More specifically, if your callbacks are slow, it is probably your fault. If your callback is a bottleneck, there is probably an issue with your design.

## Construction

Extreme care and notes are made in the code to avoid recreating objects where possible to avoid excessive GC and allocations. For example, consumers may have callbacks passed on creation to help avoid the temptation to create callbacks in a loop.

You should always avoid creating/recreating consumers, producers, callbacks, etc. in loops. Instead, pool your objects, close over them, cache, etc.

## Validation

Validation may be enabled/disabled at any time to avoid any performance overhead. See [Schema](https://github.com/plumatic/schema) for more details.

## Offset Management

Franzy offers automatic offset management via Kafka. This is a high-performance and user-friendly option.

You may also elect to manually manage offsets. If you decide to do so, ensure your data store is both high-performance and reliable. The trade-offs between these are per-application specific. For example, you can still use Zookeeper if desired, or you may elect to use Redis, Aerospike, etc. for offset management. Understand that some datastores are less reliable than others.

If you are ensure about this choice, please send me a message and I will be happy to discuss it with you. I will not bash specific data stores here, but will happily do so in private.

## Partitioning

Understand Kafka partitioning fully if you want to tweak your implementation.

Here is a good starting place - [How to Choose the Number of Topics and Partitions in a Kafka Cluster](http://www.confluent.io/blog/how-to-choose-the-number-of-topicspartitions-in-a-kafka-cluster/)

## Data Types

Favor record implementations over maps if possible. Records will provide a better memory footprint and will shine if your operations require large collections of map structures. For instance, if you are creating a large list of topic partitions to make an API call, create them as records. They will work just the same as records.

Understand that records behave slightly differently than maps. If you don't know why or how this is, please take the time to research the issue.

## Crude Producer Benchmark

Non-scientific benchmark based off another Kafka client benchmark to test on your machine:

```clojure
(defn nippy-producer-bench []
  (let [pc {:bootstrap.servers ["127.0.0.1:9092"]}
        key-serializer (serializers/keyword-serializer)
        value-serializer (nippy-serializers/nippy-serializer)
        options (pd/make-default-producer-options)
        pr (pt/->ProducerRecord "test-nippy" 0 "key" "data")]
    (with-open [p (producer/make-producer pc key-serializer value-serializer options)]
      (time
        (doseq [i (range 1000000)]
           (send-async! p pr))))))
```

Many factors may affect this and results will vary highly. As stated, extremely non-scientific.

If you have real-world benchmarks using this library, please let me know.
