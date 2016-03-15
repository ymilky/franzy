(ns franzy.clients.decoding-tests
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.clients.consumer.schema :as cs]
            [franzy.clients.consumer.types :as ct]
            [franzy.clients.codec :as codec])
  (:import (org.apache.kafka.clients.consumer ConsumerRecord OffsetAndMetadata)))

(facts
  "ConsumerRecord objects should be properly decoded to Clojure."
  (let [jcr (ConsumerRecord. "murdered-starks" 99 500 :sean-bean 1)
        cr (ct/->ConsumerRecord "murdered-starks" 99 500 :sean-bean 1)]
    (fact
      ;;Department of Redundancy Lives On!
      "ConsumerRecord objects should decode properly to ConsumerRecord Records."
      (codec/decode jcr) => cr
      (s/check cs/ConsumerRecord (codec/decode jcr)) => nil)))

(facts
  "OffsetAndMetadata objects should be properly decoded to Clojure."
  (let [jomd (OffsetAndMetadata. 33 "What is dead may never die, but can be flayed.")
        omd (ct/->OffsetMetadata 33 "What is dead may never die, but can be flayed.")
        omd-map {:offset 33 :metadata "What is dead may never die, but can be flayed."}]
    ;;TODO: if switching to record, use this instead, and change map version to call function instead of codec decode protocol
    ;(fact
    ;  "OffsetAndMetadata objects should be decoded to OffsetMetadata records."
    ;  (codec/decode jomd) => omd
    ;  (s/check cs/OffsetAndMetadata (codec/decode jomd)) => nil)
    (fact
      "OffsetAndMetadata objects should be decoded properly to Clojure maps."
      (codec/decode jomd) => omd-map
      (s/check cs/OffsetAndMetadata (codec/decode jomd)) => nil)))
