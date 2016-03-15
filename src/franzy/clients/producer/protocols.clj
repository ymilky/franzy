(ns franzy.clients.producer.protocols)

(defprotocol FranzyProducer
  "Protocol for implementing a Kafka Producer."
  (flush! [producer])
  (send-async!
    [this m]
    [this m opts]
    [this topic partition k v opts])
  (send-sync!
    [this m]
    [this m options]
    [this topic partition k v opts])
  (close
    ;[this]
    [this opts]))
