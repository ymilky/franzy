# Best Practices

Some general best practices:

* Decide early on a data format and serializer/deserializer. It will be difficult to change later on without replaying the complete logs.
* Decide as early as possible on your partitioning and clustering strategies. This will influence how and where you run a Franzy producer or consumer.
* Limit the creation of data structures and objects when possible. This is particularly recommended for your actual consumer and producer instances, as well as callbacks.
     * Creating and re-creating producers, consumers, and callbacks is usually an anti-pattern.
     * Creating and re-creating objects will lead to lots of garbage collection in addition to frequent reconnects, rebalances, and so on.
     * Prefer records over maps, particularly for consumption. You can use records to produce as well if you prefer them over maps, or simply use explicit arities of production functions.
* Keep in mind that most of what you do, ideally, should be called in a loop over and over. Keep this loop predictable and efficient, as any garbage, cache thrashing, etc. will cut your throughput consuming or producing.
* Use transducers to extract and process your results
* Do explore and read about the Kafka config settings. Tweaking them can make a huge difference in performance, but these settings will be relative to your use-case.
* Store your configuration as EDN or using some kind of configuration management library. Minimum dynamically build it or inject it, rather than defining it globally.
* Wrap your consumers and producers in a library like [Component](https://github.com/stuartsierra/component) or [Mount](https://github.com/tolitius/mount).
     * Examples: coming soon
* Use log4j and create a log4j.properties or a compatible alternative for Kafka if you need more information about what is going on (wrong) with Kafka. Likewise, as Kafka uses Zookeeper, you may elect to also separately process Zookeeper logs.
     * For general logging, I highly recommend [Timbre](https://github.com/ptaoussanis/timbre).

## Consumers and Producers

* Define keys up-front. Many serializers will require you to explicitly provide a key for producer records.
* Use core.async, manifold, or a similar library for asynchronous producing and consuming. Generally, Kafka recommends that you limit consumers and producers to a single thread each. If you have multiple consumers, you could for example have a thread for each.
* Always close your consumer and producer. You can use with-open or a custom macro to close your consumers and producers, but be sure that you do so in try/catch/finally style. This is automatic when using with-open.
     * When closing a consumer, be sure to attempt to wake it up from another thread when shutting it down. This prevents the consumer from delaying or deadlocking shut down when stuck in a long-poll.
* Remember to assign your consumers a meaningful group.id
* Distribute consumers and producers across machines if possible. If you use a thread per consumer for example, it is not hard to imagine that with many consumers you may exhaust your JVM threads/thread pools.
* Assign your producer or a consumer a `:client.id` in its configuration. This helps trace problems when they happen and will make it easier for you to understand what is going on in Zookeeper.
* Set your `:metric.num.samples` and `:metric.sample.window.ms` for more useful and possibly better performing metrics to fit your use-case.
* Pass your serializers/deserializers explicitly rather than as a config property strings. Although this is fully supported in Kafka, it is far easier to maintain code that will fail to compile/run because of a changed or missing namespace.

### Consumers

* Set `:fetch.min.bytes` and `:max.partition.fetch.bytes` where possible to prevent consumers from getting stuck or taking long CPU/network cycles. The more predictable your fetches, the better. Be aware if you have large messages, you could cause the consumer to be stuck on a message with too small a max setting.
* If your partitioning is very predictable and constant, that is you know the layout of your data will not change, you can optimize your consumer by providing a `:partition.assignment.strategy` that fits with your use-case. You may use one of the provided or implement your own using the tools provided by Franzy.
* Set `:retry.backoff.ms` to avoid failed fetching hammering the server. For example, if you set a high number of retries per fetch, you can bog down the consumer in a tight loop.
* Set `:fetch.max.wait.ms` to avoid repeated network IO from failed/empty polls. The consumer will block if there isn't enough data as specified by `:fetch.min.bytes`, but answer when it is able to retrieve enough data. For a topic partition that isn't seeing much production where the consumer is near the end of the log, this may be a bad idea, however.

### Producers

* Use batching when possible via `:batch.size` for better performance. Be aware of the trade-offs between small and large batches, and try to make your batches predictable.

## Offsets

* When dealing with offset management and processing in general, try to create idempotent functions and solutions. Kafka in many cases will call callbacks multiple times with the same data. Additionally it may be useful to have an idempotent design when things go wrong and you need to reset the consumer's position to replay the logs.
* Do not subscribe and attempt to manually assign offsets at the same time. They are mutually exclusive. You have been warned, again.
* Do not assume offset 0 is the beginning of a topic. As the log grows, messages may be removed over time.
* If manually managing offsets, ensure you use a high-performance, fault-tolerant, fast data store. Fault-tolerance should be stressed in this situation as losing an offset could cause data loss.
* Ensure you commit offsets in the same thread you are polling.
* It is often a more workable solution for those manually comitting offsets to use batching. Take a few items from your poll in a batch, process them, and commit when you know your batch has ack'd sucessfully in your application code. A consumer poll can return thousands of records and assuming autocommit will always work if you cannot miss data requires that you have great confidence in what is processing your batch.
* One way to get a bit more robust capabilities when doing manual offset committing is to pass batches to a stream processor such as Onyx that can ack when it succeeds. When the job succeeds, you can then commit your offsets to Kafka safely. Some advantages are many stream processors have at-least-once or at-most-once semantics, retries, job durability, job state management, and more to make your life easier. If storing your offsets externally or in a separate process from the consumer, you can consider using the job itself to committ offsets as the last step/complete step.


