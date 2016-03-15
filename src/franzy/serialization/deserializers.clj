(ns franzy.serialization.deserializers
  "Implementations of core Apache Kafka and Franzy deserializers.
  For more serializers, see Franzy docs."
  (:require [clojure.edn :as edn])
  (:import (org.apache.kafka.common.serialization Deserializer StringDeserializer LongDeserializer IntegerDeserializer ByteArrayDeserializer)
           (java.io PushbackReader ByteArrayInputStream)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Implementors - Please read notes in serializers.clj
;;
;; A general word of caution:
;; Many applications and developers have a tendancy to serialize things directly from user input to Kafka.
;; Be aware that this is a potential attack vector, especially during deserialization. Always validate your inputs!
;; Consider yourself warned. Not that anyone really wants YOUR data anyway, however someone may send cat pictures to your
;; Storm job as a result. Or worse.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;Options vs. just passing raw params?
;; for performance, it seems better to close over any functions passed rather than pass a map with function lookups here
;; despite the cumbersome arity/impl requirements - most implementors don't need all fns
(defn deserializer
  ^Deserializer
  ([deserialize-fn]
   (deserializer deserialize-fn nil nil))
  ^Deserializer
  ([deserialize-fn configure-fn close-fn]
   (reify
     Deserializer
     (configure [_ configs is-key?]
       "Configures a deserializer as necessary, for any stateful configuration.
       Typically Clojure-based serializers will not need an implementation for this function.
       A common use-case is to differentiate between deserializing a key vs. a value.

       See the source of org.apache.kafka.common.serialization.StringDeserializer for an example."
       (when configure-fn
         (configure-fn configs is-key?)))
     (deserialize [_ topic data]
       "Main deserialization function. All deserializers must implement this function."
       (deserialize-fn topic data))
     (close [_]
       "Closes the deserializer.
       Any stateful deserializers should implement close.
       Close may be called multiple times and thus must be idempotent."
       (when close-fn
         (close-fn))))))

(defn byte-array-deserializer
  "Kafka raw byte array deserializer.
  Useful for value deserialization."
  ^Deserializer []
  (ByteArrayDeserializer.))

(defn integer-deserializer
  "Kafka integer deserializer.
  Useful for key deserialization."
  ^Deserializer []
  (IntegerDeserializer.))

(defn long-deserializer
  "Kafka long deserializer.
  Useful for key deserialization."
  ^Deserializer []
  (LongDeserializer.))

(defn string-deserializer
  "Kafka string deserializer.
  Useful for key deserialization."
  ^Deserializer []
  (StringDeserializer.))

(deftype EdnDeserializer [opts]
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (when data
      (with-open [r (PushbackReader. (clojure.java.io/reader (ByteArrayInputStream. data)))]
        ;;Can't remember if this binding is needed anymore with safer edn/read, but we like safe(r/ish) via edn/read
        ;;Hey you're sending raw EDN over the network, you like to live on the wild side, friend!
        (binding [*read-eval* false]
          (edn/read (or opts {}) r)))))
  (close [_]))

(defn edn-deserializer
  "An EDN deserializer for Kafka.
  Contents of each item serialized must fit in memory.

  > Note: Any users of EDN deserializers should note the usual serialization/deserialization attack vectors.
  You should always validate any data before it is serialized so that an attack may not be executed on deserialization.
  Although EDN facilities try to protect you against this, nothing in this life is ever for sure. Be vigilant."
  (^EdnDeserializer [] (edn-deserializer nil))
  (^EdnDeserializer [opts]
   (EdnDeserializer. opts)))

(deftype SimpleEdnDeserializer [opts]
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (edn/read-string (or opts {}) (String. ^bytes data "UTF-8")))
  (close [_]))

(defn simple-edn-deserializer
  "A Simple EDN deserializer for Kafka.
  Useful for value deserialization."
  ^SimpleEdnDeserializer
  ([] (simple-edn-deserializer nil))
  ^SimpleEdnDeserializer
  ([opts]
   (SimpleEdnDeserializer. opts)))

(deftype KeywordDeserializer []
  Deserializer
  (configure [_ _ _])
  (deserialize [_ _ data]
    (when data
      (keyword (String. ^bytes data "UTF-8"))))
  (close [_]))

(defn keyword-deserializer
  "A deserializer that deserializes string values as keywords.
  Useful for key deserializers."
  ^Deserializer []
  (KeywordDeserializer.))

(deftype DebugDeserializer [logging-fn ^Deserializer deserializer]
  Deserializer
  (configure [_ configs is-key]
    (logging-fn {:deserializer deserializer
                 :fn           :configure
                 :configs      configs
                 :is-key       is-key})
    (.configure deserializer configs is-key))
  (deserialize [_ topic data]
    (logging-fn {:deserializer deserializer
                 :fn           :serialize
                 :topic        topic
                 :data         data})
    (.deserialize deserializer topic data))
  (close [_]
    (logging-fn {:deserializer deserializer
                 :fn           :close})
    (.close deserializer)))

(defn debug-deserializer
  "Simple debug serializer that wraps your deserializer and desired logging function.
  The logging function should take at least a single arity.
  The function will receive a map of state information with the following possible keys, which you may choose to destructure accordingly:

  * :deserializer - An instance of the deserializer itself
  * :fn - Keyword name of the function being logged. Possible values `[:configure :serialize :close]`
  * :configs - Optional, present when configuring serializer, and only applicable for certain types of serializers
  * :is-key - Optional, present when configuring the serializer, and only applicable for certain types of serializers
  * :topic The topic being serialized, when calling serialize.
  * :data The data being serialized, when calling serialize.

  Example usage:

  `(debug-deserializer
          (fn [{:keys [fn deserializer configs is-key topic data] :as m}]
            (timbre/debug \"full debug map:\" m)
            (when data
              (timbre/info \"data:\" data)))
            (edn-deserializer))`"
  ^DebugDeserializer [logging-fn ^Deserializer deserializer]
  (DebugDeserializer. logging-fn deserializer))

;;TODO: composite deserializer?
