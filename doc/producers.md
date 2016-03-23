# Producers

For production use, ensure you are not creating/recreating the producer constantly. Additionally, be sure you are not holding any stray references before shutting down the producer. The examples given in Franzy and Franzy-examples are only for getting a feel for what is possible in the API, actual usage will vary greatly depending on your data flow.

A common pattern for producers is to either directly accept input for production or to take values off of some queuing mechanism, for example a core.async channel. Your durability and latency requirements should reflect this choice. Once you have a value, at the simplest level, production involves sending your value with some information about where it should go - a topic, partition, and a key. 

Kafka guarantees ordering within a partition, but not between partitions. You can manually select a partition to send data to, rely on a partitioning algorithm you've configured via the producer configuration, or call some other function in your code that will select a partition. The `default-partitioner' function in the Franzy producer package is one example of a partitioner (built-in). The key you provide will help a partitioner decide in what partition to place your value. 

In practice, it is generally best to know up front where you are sending your data and why. For example, to provide parallelism to scale consumers, you might elect to place different user data in different partitions. Selecting a partition per user would usually be a bad idea and is a common misconception for beginners. Among many reasons, you will eventually hit scalability limits that are linked more to the underlying file descriptors that need to be allocated than anything else in Kafka. Instead, when deciding how to partition your data when producing, you should think how you can bucket data in ways that make sense for both your ordering and scalability needs. If you have for example a few super users who cause logjams in your consumers, you might elect to spread their data into different partitions.

A producer can produce data by sending a producer record to Kafka. A producer record can be passed to the `send-sync!` or `send-async!` protocols as a map, producer record type, or as explicit parameters. You should explicitly provide the topic, partition, key, and value if you know up front where your data should go. If you are a more advanced user and want to let a Kafka partitioner do the job, you may provide only the topic and value, or topic, key, and value depending on the partitioner implementation and data format. 

## Partitioners

### Default Partitioner

The default partitioning strategy is as follows, via (DefaultPartitioner):

 * If a partition is specified in the record, use it
 * If no partition is specified but a key is present choose a partition based on a hash of the key
 * If no partition or key is present choose a partition in a round-robin fashion

Take special notice of the case when the key is present - your key will be hashed with murmur2.

### Rolling Your Own Partitioner

If you want to avoid calling your own partition function each time to calculate a partition before you produce, you can provide a `partitioner.class` key to your producer configuration. This class should be discoverable on your classpath, given the string value fully-qualified class name for the configuration key.

You will need to implement the Partitioner interface. You of course can roll this in pure Java, or simply use deftype/gen-class and if needed, AOT compilation. Of course there's nothing preventing you from instead just manually calling your own function, but the advantage to providing it via configuration is it prevents any of your client code from missing this curcial step which would lead to undesirable effects in your partitioning strategy given an error.

As a best practice, you should decide your partitioning strategy before creating your topic and adding data to partitions. Failure to do so may lead to situations where your pre-existing data is not partitioned according to the same strategy as future data. Since your partitioning strategy will play a role in how the data is consumed and in what order (per partition), this can have some dire consequences in some systems. If you forget, a simple fix is to replay your log until the point in time where you changed your partitioning strategy, and then write the old records into new partitions if possible, or replay all the data into a new topic. The former is doable only if the old data does not need to be ordered before existing data or you are going to rewrite all the day. The latter solution is usually cleaner and easier to implement.

## Encapsulating a Producer

Typically you will want to encapsulate your producer somehow to be able to maintain a reference to it and avoid recreating it as previously discussed.

It is highly recommended not to globally declare your producer using def or defonce as a singleton if possible. This often can create subtle bugs and shutdown issues, and can result in multiple instantiations of the same producer do to the namespace being evaluated at different points.

The most common patterns to safely manage your producer are one of the following (but not limited to):

* Component
* Mount
* Atom/Maps

Usually you should construct the producer inside whatever own it via the config and pass any supporting data into a construction method as well such as configuration for core.async channel sizes, producer options, topics, partitioning strategies, and other dependencies. In the case of component, this usually just means a simple make/new function for the component in conjunction with the component's start protocol implementation.

## Possible Flow

The component strategy has many permutations and is similar to the other strategies. A common pattern I have used is to create a component with a thread or go-loop set to a key(s) in the component. You will also have a few channels for input, output, errors, control (kill/pause/etc), as keys in the component. The thread/go-loop will take values from an inbox, usually a core.async channel, process them, and write output such as acks to one or more output channels.

The acks can then be used to notify other parts of your application such as a UI that the write to Kafka succeeded. Moreover, you may have other threads and go-loops that manage the ack data to write this information to another store, for example Redis or Cassandra, or simply to notify other parts of your application more directly. 

If production fails, you can retry either by looping again before taking another inbox value or by using the mechanisms provided by Kafka itself to retry. If you cannot proceed on a failure due to durability requirements or a network outage, then simply close the producer and act accordingly in your application. It is very important that you always consider how you will cleanup your resources. In a component, this is down during the stop protocol. Your actual implementation may vary a lot depending on how you manage this phase. For example you may elect to take values from your loops to block until they shut down. If you have high durability requirements, you may need to flush your channels completely before a clean shutdown can happen. In other words, if you are using an input channel, you need to make sure it has no pending values left before shutting down the production loop.

For shutdown, always be sure you cleanup thing in the proper order and be sure to close:

* Threads/Channels that you own, i.e. not from the consumer-side of the channel
* The producer, via close. Note that close can take an optional timeout via the producer options or directly in the 1-arity close method.
* Any other resources such as open connections to databases, file systems, etc.

Finally, again remember that if you are queueing values, it is your responsibility to decide if a shutdown requires emptying the queue first or discarding any unprocessed data. This issue is a common mistake I've come across in production code for Kafka.


...more soon.
