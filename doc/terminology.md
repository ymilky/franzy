# Basic Terminology

The following terminology is used heavily in this library, documentation, and code comments:

* Topic - feeds of messages in categories
* Partition - Divides topics, for consumption, load balancing, etc. Each partition may exist on a separate machine, or the same machine. Multiple consumers may read from multiple partitions.
* Offset - A position in a particular topic partition, used on a per-consumer basis
* Partitioner/Partition Strategy - How data will be distributed within Kafka.
* Consumer Group - a logical grouping of consumers, used to implement queuing and publish-subscribe semantics, depending on the consumer.
* Assignment - The topic, partition, and offset a consumer is assgined to
* Consumer Rebalance - Occurs when consumers come to a consensus on which consumer is consuming which partitions. Triggered on each addition or removal of both broker nodes and other consumers within the same consumer group.
* Broker - A node in a Kafka cluster. These will be specified in your connection parameters when you use a producer, consumer, or other APIs that need to interact with the Kafka cluster.
* Producer - processes that publishes message.
* Consumer - process that consumes a message.
* Producer Record - a message that will be sent by the producer to a topic.
* Consumer Record - a message that will be returned to a consumer from a topic.
* Topic Partition - a logical grouping of a topic and any valid partition.
* Leader - each partition has one server that acts as the leader, and zero or more servers as followers.
