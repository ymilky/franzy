(ns franzy.clients.consumer.schema-tests
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.clients.consumer.schema :as cs]
            [franzy.clients.consumer.types :as ct]
            [franzy.clients.consumer.defaults :as defaults]
            [franzy.clients.consumer.callbacks :as callbacks])
  (:import (java.nio ByteBuffer)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests to ensure that was the schema involves, we don't murder Kafka.
;; While these tests are perhaps repetitive and overlapping, we test schema more and more aggressively as time goes on.
;; In the real-world, someone can and will make a giant mistake. The problem is Kafka, especially as features are added
;; will happily accept our bad data. Putting a distributed system in an undefined is not fun, and thus, this mess.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(facts
  "ConsumerOptions must pass schema validation."
  (let [valid-options {:consumer-records-fn         seq
                       :poll-timeout-ms             3000
                       :offset-commit-callback      (callbacks/offset-commit-callback)
                       :rebalance-listener-callback (callbacks/consumer-rebalance-listener)}]
    (fact
      "Consumer options that are valid pass validation."
      (s/check cs/ConsumerOptions valid-options) => nil
      (s/check cs/ConsumerOptions {}) => nil
      (s/check cs/ConsumerOptions (dissoc valid-options :consumer-records-fn)) => nil
      (s/check cs/ConsumerOptions (dissoc valid-options :poll-timeout-ms)) => nil
      (s/check cs/ConsumerOptions (dissoc valid-options :offset-commit-callback)) => nil
      (s/check cs/ConsumerOptions (dissoc valid-options :rebalance-listener-callback)) => nil
      (s/check cs/ConsumerOptions (assoc valid-options :offset-commit-callback nil)) => nil)
    (fact
      "Consumer option defaults must pass validation."
      (s/check cs/ConsumerOptions (defaults/default-consumer-options)) => nil)
    (fact
      "Consumer options that are not valid must fail validation."
      (s/check cs/ConsumerOptions (assoc valid-options :consumer-records-fn "number muncher")) =not=> nil
      (s/check cs/ConsumerOptions (assoc valid-options :poll-timeout-ms "unnecessary kitchen accessory")) =not=> nil
      (s/check cs/ConsumerOptions (assoc valid-options :poll-timeout-ms nil)) =not=> nil
      (s/check cs/ConsumerOptions (assoc valid-options :consumer-records-fn nil)) =not=> nil
      (s/check cs/ConsumerOptions (assoc valid-options :rebalance-listener-callback "foolery")) =not=> nil
      (s/check cs/ConsumerOptions (assoc valid-options :rebalance-listener-callback identity)) =not=> nil

      (s/check cs/ConsumerOptions (assoc valid-options :offset-commit-callback identity)) =not=> nil)))

;; (set! Soviet Paranoia)
(facts
  "ConsumerRecord must pass schema validation."
  (let [valid-cr {:topic     "My book about me"
                  :partition 79
                  :offset    4
                  :key       15
                  :value     {:type                "Spaghetti Monster"
                              :description         "Pastafarian"
                              :other               "Touched by his noodly appendage"
                              :win-api-enum-values [true false 0 "FileNotFound" {:yes "certainly!"}]}}]
    (fact
      "Valid consumer records must pass schema validation."
      (s/check cs/ConsumerRecord valid-cr) => nil
      (s/check cs/ConsumerRecord (assoc valid-cr :value [1 2 3])) => nil
      (s/check cs/ConsumerRecord (assoc valid-cr :topic 32)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :partition Integer/MAX_VALUE)) => nil
      (s/check cs/ConsumerRecord (assoc valid-cr :offset Long/MAX_VALUE)) => nil
      (s/check cs/ConsumerRecord (assoc valid-cr :offset 9)) => nil)
    (fact
      "Invalid consumer records must fail schema validation."
      (s/check cs/ConsumerRecord (dissoc valid-cr :topic)) =not=> nil
      (s/check cs/ConsumerRecord (dissoc valid-cr :partition)) =not=> nil
      (s/check cs/ConsumerRecord (dissoc valid-cr :offset)) =not=> nil
      (s/check cs/ConsumerRecord (dissoc valid-cr :key)) =not=> nil
      (s/check cs/ConsumerRecord (dissoc valid-cr :value)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :topic 32)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :topic nil)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :topic [])) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :topic #{})) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :topic '())) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :topic {})) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :partition "99")) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :partition [23])) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :partition nil)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :partition (+ 1 Integer/MAX_VALUE))) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :partition -1)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :partition Double/MAX_VALUE)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :offset nil)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :offset [23])) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :offset Double/NaN)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :offset (+ Long/MAX_VALUE 1))) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :offset -1)) =not=> nil
      (s/check cs/ConsumerRecord (assoc valid-cr :key nil)) =not=> nil)))

(facts
  "OffsetMetadata must pass schema validation."
  (let [offset-metadata {:offset   1
                         :metadata "what network is not social?"}]
    (fact
      "Valid offset metadata must pass schema validation."
      (s/check cs/OffsetAndMetadata offset-metadata) => nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset Long/MAX_VALUE)) => nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset 0)) => nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :metadata nil)) => nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :metadata "")) => nil)
    (fact
      "Invalid offset metadata must fail schema validation."
      (s/check cs/OffsetAndMetadata {}) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset -1)) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset nil)) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset (+ Long/MAX_VALUE 1))) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset [])) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset #{})) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset '())) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset {})) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :offset "1")) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :key 123)) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :metadata 1)) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :metadata [1 2 3])) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :metadata {})) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :metadata #{})) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :metadata '())) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :metadata true)) =not=> nil
      (s/check cs/OffsetAndMetadata (dissoc offset-metadata :metadata)) =not=> nil
      (s/check cs/OffsetAndMetadata (assoc offset-metadata :meat-data "meat on meat sandwich")) =not=> nil)))

(facts
  "OffsetResetStrategyEnum must pass schema validation."
  (let [strat :earliest
        strategies [:latest :earliest :none]]
    (fact
      "Valid offset reset strategies pass schema validation."
      (s/check cs/OffsetResetStrategyEnum strat) => nil
      (doseq [reset-strat strategies]
        (s/check cs/OffsetResetStrategyEnum reset-strat) => nil))
    (fact
      "Invalid offset reset strategies must fail schema validation."
      (s/check cs/OffsetResetStrategyEnum "") =not=> nil
      (s/check cs/OffsetResetStrategyEnum "the beginning of time") =not=> nil
      (s/check cs/OffsetResetStrategyEnum nil) =not=> nil
      (s/check cs/OffsetResetStrategyEnum [:earliest]) =not=> nil
      (s/check cs/OffsetResetStrategyEnum "earliest") =not=> nil)))

(facts
  "PartitionAssignment values must pass schema validation."
  (let [assignment {:topics    ["userdata-to-sell" "modified-privacy policies" "cheesy-buttons"]
                    :user-data (ByteBuffer/allocate 1)}]
    (fact
      "PartitionAssignment values that are valid must pass schema validation."
      (s/check cs/PartitionAssignment assignment) => nil
      (s/check cs/PartitionAssignment (assoc assignment :user-data nil)) => nil
      (s/check cs/PartitionAssignment (assoc assignment :topics [])) => nil
      (s/check cs/PartitionAssignment (assoc assignment :topics nil)) => nil
      (s/check cs/PartitionAssignment (assoc assignment :topics "laughing to the bank")) => nil)
    (fact
      "PartitionAssignment values that are invalid must not pass schema validation."
      (s/check cs/PartitionAssignment nil) =not=> nil
      (s/check cs/PartitionAssignment {}) =not=> nil
      (s/check cs/PartitionAssignment (assoc assignment :topic "funding schools for a tax dodge")) =not=> nil
      (s/check cs/PartitionAssignment (assoc assignment :user-data (byte-array 1))) =not=> nil)))

(facts
  "ConsumerRebalanceListener callbacks must pass schema validation."
  (fact
    "Valid callbacks pass schema validation."
    (s/check cs/ConsumerRebalanceListenerCallback (callbacks/no-op-consumer-rebalance-listener)) => nil)
  (fact
    "Invalid callbacks fail schema validation."
    (s/check cs/ConsumerRebalanceListenerCallback identity) =not=> nil))

;;TODO: more validations
(facts
  "Consumer configuraitons must pass schema validation."
  (let [cc {:bootstrap.servers ["127.0.0.1:8080"]}]
    (fact
      "Consumer configurations that are valid pass schema validation."
      (s/check cs/ConsumerConfig cc) => nil)
    (fact
      "Consumer configurations that are invalid fail schema validation."
      (s/check cs/ConsumerConfig {}) =not=> nil
      (s/check cs/ConsumerConfig (assoc cc :format.harddrive true)) =not=> nil
      (s/check cs/ConsumerConfig (dissoc cc :bootstrap.servers)) =not=> nil
      (s/check cs/ConsumerConfig (assoc cc :bootstrap.servers [])) =not=> nil
      (s/check cs/ConsumerConfig (assoc cc :bootstrap.servers nil)) =not=> nil)))

;;cold, hard
(facts
  "ConsumerRecords records must pass schema validation."
  (let [cr (ct/->ConsumerRecord "coifs" 12 0 "abc"
                                {:vicious-bytes (byte-array 1)
                                 :bad-remakes   ["I am Legend" "Star Trek 2" "Hitchhiker's Guide to the Galaxy"]})]
    (fact
      "Valid consumer records pass validation."
      (s/check cs/ConsumerRecord cr) => nil
      (s/check cs/ConsumerRecord (ct/map->ConsumerRecord {:topic "hot-dog-bun-allocation" :partition 0 :offset 12 :key 123 :value 0})) => nil)
    (fact
      "Invalid consumer records fail validation."
      (s/check cs/ConsumerRecord (assoc cr :topic nil)) =not=> nil
      (s/check cs/ConsumerRecord (assoc cr :partition nil)) =not=> nil
      (s/check cs/ConsumerRecord (assoc cr :offset nil)) =not=> nil
      ;;I have become....map
      (s/check cs/ConsumerRecord (dissoc cr :key)) =not=> nil)))

