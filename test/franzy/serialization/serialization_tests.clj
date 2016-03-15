(ns franzy.serialization.serialization-tests
  (:require [midje.sweet :refer :all]
            [franzy.serialization.serializers :as serializers]
            [franzy.serialization.deserializers :as deserializers])
  (:import (org.apache.kafka.common.serialization Deserializer Serializer)))

(facts
  "String serializers serialize strings." :serializers
  (let [serializer (serializers/string-serializer)
        deserializer (deserializers/string-deserializer)
        topic "wild-kingdom"
        data "A quick brown fox jumped over the fence and then was hit by a gas guzzling SUV."]
    (fact
      "A string serializer should be able to produce the same string in a round trip." :serializers
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)))

(facts
  "Long serializers correctly serialize longs." :serializers
  (let [serializer (serializers/long-serializer)
        deserializer (deserializers/long-deserializer)
        topic "machine-settings"
        data Long/MAX_VALUE]
    (fact
      "A long serializer should be able to produce the same long in a round trip."
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)))

(facts
  "Integer serializers serialize integers." :serializers
  (let [serializer (serializers/integer-serializer)
        deserializer (deserializers/integer-deserializer)
        topic "liters-of-carbonated-sugar"
        data Integer/MAX_VALUE]
    (fact
      "An integer serializer should be able to produce the same integer in a round trip."
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)))

(facts
  "Byte array serializers serialize byte arrays." :serializers
  (let [serializer (serializers/byte-array-serializer)
        deserializer (deserializers/byte-array-deserializer)
        topic "not-safe-for-work"
        data (byte-array 55)]
    (fact
      "A byte array serializer should be able to produce the same integer in a round trip."
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)))

;;TODO: more tests here
(facts
  "EDN serializers serialize EDN." :serializers
  (let [serializer (serializers/edn-serializer)
        deserializer (deserializers/edn-deserializer)
        simple-serializer (serializers/simple-edn-serializer)
        simple-deserializer (deserializers/simple-edn-deserializer)
        topic "coffee-talk"
        data {:clo-to-my-jure
              {:string-key "can be a string"
               :key-master :gatekeeper
               :long-key   Long/MAX_VALUE
               :int-key    Integer/MAX_VALUE
               :double-key Double/MAX_VALUE
               :short-key  Short/MIN_VALUE
               :vector-key ["A good year is not a tire." 56.2 nil {:the-key "is bbq"}]
               :map-key    {:nested                        {:cool-runnings '("alligators" "with" "nailguns" "cranially" "mounted")}
                            :michael-stipe                 "loves the swim move with his hands"
                            :have-you-a-frequency-kenneth? false
                            :frequency-of-kenneth          nil}
               :set-key    #{"the set" "a last bastion" "of unique values" "traveling through the internets"}
               :list-key   '("listerine" "is" "recommended" "when standing" "over" "my" "desk")
               :zero-key   0
               :nil-key    nil}}]
    (fact
      "An edn serializer should be able to produce the same edn in a round trip."
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)
    (fact
      "A simple edn serializer should be able to produce the same edn in a round trip."
      (->> (.serialize simple-serializer topic data)
           (.deserialize simple-deserializer topic)) => data)
    (fact
      "For smaller results, a simple edn serializer/deserializer should produce the same results as its big brother."
      (->> (.serialize simple-serializer topic data)
           (.deserialize simple-deserializer topic)) => (->> (.serialize serializer topic data)
                                                             (.deserialize deserializer topic)))
    (fact
      "Mixing and matching edn serializers for small results should produce the same, misguided results."
      ;;I expect stupidity because I've met myself
      (->> (.serialize serializer topic data)
           (.deserialize simple-deserializer topic)) => data
      (->> (.serialize simple-serializer topic data)
           (.deserialize deserializer topic)) => data)
    (fact
      "Large results serialize." :high-memory
      ;;TODO: go bigger, but this is annoying for running tests when gc overhead limit exceeds...
      (let [large-data (vec (repeat 4096 Long/MAX_VALUE))   ;(vec (repeat Long/MAX_VALUE UUID/randomUUID))
            ]
        (->> (.serialize serializer topic large-data)
             (.deserialize deserializer topic)) => large-data))))

;;TODO: more tests here
(facts
  "Keyword serializers serialize keywords." :serializers
  (let [serializer (serializers/keyword-serializer)
        deserializer (deserializers/keyword-deserializer)
        topic "not-safe-for-work"
        ;;we test a "big" keyword to be sure, because we like big
        ;;credit: JAMC - bside version, a soundtrack for this test - https://www.youtube.com/watch?v=rZjDdXRC5N8
        data :unlike-the-mole-im-not-in-a-hole-and-I-cant-see-anyway-just-like-a-doll-im-one-foot-tall-but-dolls-cant-see-anyway-the-frozen-stare-the-clothes-and-hair-these-make-me-taste-like-a-man-tied-to-a-door-chained-to-a-floor-an-hour-glass-grain-of-sand-Life-in-a-sack-Is-coming-back-Im-like-the-clock-Im-like-the-clock-Im-like-the-clock-On-the-wall-On-the-wall-On-the-wall-Swim-in-the-sea-Swim-inside-me-But-you-cant-swim-far-away-I-never-grew-Covered-up-by-you-And-nothing-grows-anyway-Life-in-a-sack-is-coming-back-Im-like-the-clock-Im-like-the-clock-Im-like-the-clock-On-the-wall-On-the-wall-On-the-wall
        ]
    (fact
      "A keyword serializer should be able to produce the same keyword in a round trip."
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)))

(facts
  "Debug serializers serialize wrap serializers." :serializers
  (let [serializer (serializers/edn-serializer)
        deserializer (deserializers/edn-deserializer)
        logging-fn identity
        debug-ser (serializers/debug-serializer logging-fn serializer)
        debug-deser (deserializers/debug-deserializer logging-fn deserializer)
        topic "not-safe-for-work"
        data {:recommended-combinations [["machego quince"] ["cheese" "chocolate"] ["burritos" "all the time"]
                                         ["Michael Bay movies" "garbage can"]]}]
    (fact
      "A debug serializer should proxy its data."
      (->> (.serialize debug-ser topic data)
           (.deserialize deserializer topic)) => data)
    (fact
      "A debug deserializer should proxy its data."
      (->> (.serialize serializer topic data)
           (.deserialize debug-deser topic)) => data)
    (fact
      "A debug serializer and deserializer should proxy data."
      (->> (.serialize debug-ser topic data)
           (.deserialize debug-deser topic)) => data)
    (fact
      "A debug serializer should pass its data in a map during serialization."
      (let [log-fn (fn [m]
                     (:data m) => data
                     (:serializer m) => serializer)
            debug-data-ser (serializers/debug-serializer log-fn serializer)]
        (->> (.serialize debug-data-ser topic data))))))

(facts
  "Custom serializers can be created by reifying and closing over Clojure functions" :serializers
  (fact
    "Custom serializers that are created wtih reify satisfy the Serializer interface."
    (instance? Serializer (serializers/serializer (fn [_ _]))) => true)
  (fact
    "Custom deserializers that are created wtih reify satisfy the Deserializer interface."
    (instance? Deserializer (deserializers/deserializer (fn [_ _]))) => true)
  (let [^Deserializer deserializer (deserializers/deserializer (fn [_ ^bytes data]
                                                                 (some-> ^bytes data (String. "UTF-8"))))
        ^Serializer serializer (serializers/serializer (fn [_ ^String data]
                                                         (some-> data name .getBytes)))
        topic "shoegazers"
        data "https://www.youtube.com/watch?v=U2qbMP4YSu0"]
    (fact
      "A reified string serializer can round-trip serialize strings to Kafka."
      (->> (.serialize serializer topic data)
           (.deserialize deserializer topic)) => data)))
