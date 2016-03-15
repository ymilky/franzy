(ns franzy.clients.producer.schema-test
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.clients.producer.schema :as ps]
            [franzy.clients.producer.defaults :as defaults]
            [franzy.clients.producer.callbacks :as callbacks])
  (:import (java.util.concurrent TimeUnit)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests to ensure that was the schema involves, we don't murder Kafka.
;; While these tests are perhaps repetitive and overlapping, we test schema more and more aggressively as time goes on.
;; In the real-world, someone can and will make a giant mistake. The problem is Kafka, especially as features are added
;; will happily accept our bad data. Putting a distributed system in an undefined is not fun, and thus, this mess.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(facts
  "ProducerOptions must pass schema validation."
  (let [valid-options {:close-timeout      1000
                       :close-timeout-unit TimeUnit/MILLISECONDS
                       :send-callback      (callbacks/send-callback)}]
    (fact
      "ProducerOptions that are valid pass schema validation."
      (s/check ps/ProducerOptions valid-options) => nil
      (s/check ps/ProducerOptions {}) => nil
      (s/check ps/ProducerOptions (dissoc valid-options :close-timeout)) => nil
      (s/check ps/ProducerOptions (dissoc valid-options :close-timeout-unit)) => nil
      (s/check ps/ProducerOptions (dissoc valid-options :send-callback)) => nil)
    (fact
      "ProducerOptions that are invalid must fail schema validation."
      (s/check ps/ProducerOptions (assoc valid-options :close-timeout "5000")) =not=> nil
      (s/check ps/ProducerOptions (assoc valid-options :close-timeout nil)) =not=> nil
      (s/check ps/ProducerOptions (assoc valid-options :send-callback "pickle party")) =not=> nil)
    (fact
      "Producer option defaults must pass schema validation."
      (s/check ps/ProducerOptions (defaults/default-producer-options)) => nil)))

(facts
  "ProducerRecords must pass schema validation."
  (let [valid-pr {:topic     "people-at-the-beach-at-2-pm"
                  :partition 12
                  :key       "master"
                  :value     {:dana "only zuul"}}]
    (fact
      "ProducerRecords that are valid pass schema validation, despite the best intentions of..."
      (s/check ps/ProducerRecord valid-pr) => nil
      (s/check ps/ProducerRecord (dissoc valid-pr :partition)) => nil ;;I don't like that this works, but according to Kafka it does
      (s/check ps/ProducerRecord (dissoc valid-pr :key)) => nil ;;as does this bad idea
      ;;below we test with extreme paranoia as screwing up keys and values is as about as we can do
      (s/check ps/ProducerRecord (assoc valid-pr :value "bran is at home with a raisin")) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :value 1234)) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :value [1 2 3 4])) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :value '("cats" "will" "break" "your" "glass" "in" "the" "morning"))) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :value #{99 "years" "is" "a" "longgg" "long" "time"})) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :value "bran is at home with a raisin")) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :key "leftover chinese never reheats well")) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :key 1234)) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :key [1 2 3 4])) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :key '("only" "love" "can" "break" "your" "heart"))) => nil
      (s/check ps/ProducerRecord (assoc valid-pr :key #{92 "goals" "had" "the" "Gretzky"})) => nil)
    (fact
      "ProducerRecords that are invalid fail schema validation."
      (s/check ps/ProducerRecord {}) =not=> nil
      (s/check ps/ProducerRecord (assoc valid-pr :topic nil)) =not=> nil
      (s/check ps/ProducerRecord (assoc valid-pr :topic "")) =not=> nil
      (s/check ps/ProducerRecord (assoc valid-pr :topic 2314)) =not=> nil
      (s/check ps/ProducerRecord (dissoc valid-pr :topic)) =not=> nil
      (s/check ps/ProducerRecord (assoc valid-pr :partition nil)) =not=> nil
      (s/check ps/ProducerRecord (assoc valid-pr :partition "remaking ghostbusters")) =not=> nil
      (s/check ps/ProducerRecord (assoc valid-pr :value nil)) =not=> nil

      (s/check ps/ProducerRecord (assoc valid-pr :key nil)) =not=> nil
      (s/check ps/ProducerRecord (dissoc valid-pr :value)) =not=> nil)))

(facts
  "RecordMetadata must pass schema validation."
  (let [valid-metadata {:topic     "a-boy-his-blob-and-a-trashbag-of-illicit-substances"
                        :partition 1
                        :offset    99}]
    (fact
      "Valid record metadata must pass schema validation."
      (s/check ps/RecordMetadata valid-metadata) => nil
      (s/check ps/RecordMetadata (assoc valid-metadata :partition Integer/MAX_VALUE)) => nil
      (s/check ps/RecordMetadata (assoc valid-metadata :offset Long/MAX_VALUE)) => nil)
    (fact
      "Invalid record metadata must fail schema validation."
      (s/check ps/RecordMetadata {}) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :topic nil)) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :topic "")) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :partition nil)) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :offset nil)) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :topic nil)) =not=> nil
      (s/check ps/RecordMetadata (dissoc valid-metadata :topic)) =not=> nil
      (s/check ps/RecordMetadata (dissoc valid-metadata :partition)) =not=> nil
      (s/check ps/RecordMetadata (dissoc valid-metadata :offset)) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :offset Double/NaN)) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :partition Double/NaN)) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :partition Double/NEGATIVE_INFINITY)) =not=> nil
      (s/check ps/RecordMetadata (assoc valid-metadata :partition Double/POSITIVE_INFINITY)) =not=> nil)))

;;TODO: more tests
(facts
  "ProducerConfig must pass schema validation."
  (let [pc {:bootstrap.servers ["127.0.0.1:2181"]}]
    (fact
      "Valid producer configurations pass schema validation."
      (s/check ps/ProducerConfig pc) => nil)
    (fact
      "Invalid producer configurations fail schema validation."
      (s/check ps/ProducerConfig {}) =not=> nil
      (s/check ps/ProducerConfig (assoc pc :format.harddrive true)) =not=> nil
      (s/check ps/ProducerConfig (dissoc pc :bootstrap.servers)) =not=> nil
      (s/check ps/ProducerConfig (assoc pc :bootstrap.servers [])) =not=> nil
      (s/check ps/ProducerConfig (assoc pc :bootstrap.servers nil)) =not=> nil)))
