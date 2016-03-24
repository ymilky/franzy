(ns franzy.clients.consumer.client-tests
  (:require [midje.sweet :refer :all]
            [franzy.clients.consumer.client :as cl]))

(facts "Clients should instantiate properly."
       (fact "Invoking make-consumer with just a config map should not throw a ClassCastException when converting config map to Properties instance."
             (let [config {:bootstrap.servers "127.0.0.1"
                           :value.deserializer "org.apache.kafka.common.serialization.ByteArrayDeserializer"
                           :value.serializer "org.apache.kafka.common.serialization.ByteArrayDeserializer"}]
               (cl/make-consumer config) =not=> (throws ClassCastException "clojure.lang.PersistentArrayMap cannot be cast to java.util.Properties"))))