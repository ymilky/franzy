(ns franzy.clients.consumer.partitioners
  (:require [franzy.clients.consumer.protocols :refer :all])
  (:import
    (org.apache.kafka.clients.consumer.internals PartitionAssignor)
    (org.apache.kafka.clients.consumer RangeAssignor RoundRobinAssignor)))

;;Note: instead of doing this, we could probably do something better with https://github.com/ztellman/potemkin
;;For now, prefer not to add more complexity and dependencies unless it is really needed as implemented Java interfaces directly is fine in most cases.
;;Probably a macro could also replace this...thoughts?
;;This is here for a colleague for now...
(defn make-partition-assignor
  "Convenience wrapper for implementors that prefer implementing a Clojure protocol (ex: FranzPartitionAssignor)
  that maps to the PartitionAssignor Java interface, rather than implementing the interface directly.
  It is recommended for performance to implement the Java PartitionAssignor interface directly, use gen-class,
  and any of the previous with a combination of extend-type, however this may be inconvenient in existing code-bases and
   in cases where it is more convenient to use a protocol to add some extra partition related methods."
  ^PartitionAssignor [partition-assignor]
  (reify
    PartitionAssignor
    (subscription [_ topics]
      (partition-subscription partition-assignor topics))
    (assign [_ partitions-per-topic subscriptions]
      (assign-partition! partition-assignor partitions-per-topic subscriptions))
    (onAssignment [_ assignment]
      (partition-assigned partition-assignor assignment))
    (name [_]
      (partition-assignor-name partition-assignor))))

(defn range-assignor
  "The range assignor works on a per-topic basis. For each topic, we lay out the available partitions in numeric order and the consumers in lexicographic order. We then divide the number of partitions by the total number of consumers to determine the number of partitions to assign to each consumer. If it does not evenly divide, then the first few consumers will have one extra partition.

  For example, suppose there are two consumers C0 and C1, two topics t0 and t1, and each topic has 3 partitions, resulting in partitions:

  > t0p0, t0p1, t0p2, t1p0, t1p1, and t1p2.

  The assignment will be:

  > C0: [t0p0, t0p1, t1p0, t1p1] C1: [t0p2, t1p2]"
  ^RangeAssignor []
  (RangeAssignor.))

(defn round-robin-assignor
  "The roundrobin assignor lays out all the available partitions and all the available consumers.
  It then proceeds to do a roundrobin assignment from partition to consumer.

  If the subscriptions of all consumer instances are identical, then the partitions will be uniformly distributed.
  (i.e., the partition ownership counts will be within a delta of exactly one across all consumers.)

  For example, suppose there are two consumers C0 and C1, two topics t0 and t1, and each topic has 3 partitions,
  resulting in partitions:

  > t0p0, t0p1, t0p2, t1p0, t1p1, and t1p2.

  The assignment will be:

  > C0: [t0p0, t0p2, t1p1] C1: [t0p1, t1p0, t1p2]"
  ^RoundRobinAssignor []
  (RoundRobinAssignor.))
