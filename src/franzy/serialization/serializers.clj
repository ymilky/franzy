(ns franzy.serialization.serializers
  "Implementations of core Apache Kafka and Franzy serializers.
  For more serializers, see Franzy docs."
  (:import (org.apache.kafka.common.serialization LongSerializer Serializer IntegerSerializer StringSerializer ByteArraySerializer)
           (java.io ByteArrayOutputStream)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Notes:
;;
;; These serializers (and deserializers) are used to send your data to/from Kafka. As such, any implementors should
;; be very sensitive to performance, state, and memory usage. Your serializer should ideally be barebones, stateless,
;; and close using the provided close method or each time within the serialize call (if the overhead to open/close is minimal).
;; The serializers/deserializers provided use deftype as an optimization and for JVM friendliness.
;; A convenience method that reifies a type given some set of related functions is available if you do not want/cannot, or do not
;; need the semantics of deftype. You should prefer deftype, however, for performance critical applications.
;;
;; 2 ways of constructing serializers that are acceptable
;;
;; 1. Using the serializer convenience function to reify a type - use this if you are lazy, want a quick-off, etc.
;; 2. Using deftype, implementing "Serializer". Use this if you want the possibility of using your serializer from
;; other JVM languages via a named type.
;;
;; Quite often in Kafka, your data will be consumed from a variety of places. If you're not 100% Clojure, I strongly
;; advise using deftype and enabling AOT compilation so your serializer can be used easily from Java, Scala, Groovy,
;; etc. Of course you should probably be writing this logic in Clojure, because you are that girl/guy.
;;
;; All these serializers have to fit in memory. If you need something more, you'll have to write some lower-level Kafka encoding/decoding
;; You can then register them on the server. This is close to the way the old Storm Kafka serialization worked, but all has changed in Kafka 0.9
;; These serializers piggy-back on the byte array serializer built-in, which is the most flexible way, albeit with the following caveats:
;;
;; 1. Your data must fit into memory as it sends a raw byte array across the wire.
;; 2. Your data must not be some weird byte format that Kafka doesn't understand or it will throw CRC errors if you have those checks turned on.
;;    Stick to conventional formats,m your are not special. (why are you always my former co-workers?)
;; 3. You probably shouldn't be sending a gig or something to/from Kafka. Chances are no one likes your data anyway.
;;    The network overhead alone of bringing down a single record of your monstrosity will negate the reason for using Kafka.
;;
;; Nevertheless, stupid people do indeed live among us. Before you implement your own serializer, think why.
;; If you wish to serialize something to leverage better compression, closer to your use-case, etc, you're doing it right.
;; Bearing in mind these warnings/diatribes, if you do really need large data, I recommend chunking it across several records.
;; You should do so atomically and block writes until you are sure those records have comitted in that partition so you know the order, otherwise it will
;; be in your unfortunate hands to re-assemble chunks in the correct order, when they arrive, if they arrive when you poll for records.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;TODO: refactor + schema
;;Options vs. just passing raw params?
;; for performance, it seems better to close over the values rather than pass a map with function lookups here despite the cumbersome arity/impl requirements
(defn serializer
  ^Serializer
  ([serializer-fn]
   (serializer serializer-fn nil nil))
  ^Serializer
  ([serializer-fn configure-fn close-fn]
   (reify
     Serializer
     (configure [_ configs is-key?]
       "Configures a serializer as necessary, for any stateful configuration.
       Typically Clojure-based serializers will not need an implementation for this function.
       A common use-case is to differentiate between serializing a key vs. a value.

       See the source of org.apache.kafka.common.serialization.StringSerializer for an example."
       (when configure-fn
         (configure-fn configs is-key?)))
     (serialize [_ topic data]
       "Main deserialization function. All deserializers must implement this function."
       (serializer-fn topic data))
     (close [_]
       "Closes the serializer.
       Any stateful serializers should implement close.
       Close may be called multiple times and thus must be idempotent."

       (when close-fn (close-fn))))))

(defn byte-array-serializer
  "Kafka raw byte array serializer.
  Useful for value serialization."
  ^Serializer []
  (ByteArraySerializer.))

(defn string-serializer
  "Kafka string serializer.
  This serializer allows serializing values without a key."
  ^Serializer []
  (StringSerializer.))

(defn integer-serializer
  "Kafka integer serializer.
  Useful for key serialization."
  ^Serializer []
  (IntegerSerializer.))

(defn long-serializer
  "Kafka long serializer.
  Useful for key serialization."
  ^Serializer []
  (LongSerializer.))

(deftype EdnSerializer [opts]
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    ;;TODO: process + inject more options? better defaults via configure or opts?
    ;;no reason to close bos, but we do so to keep clean
    (with-open [bos (ByteArrayOutputStream. 1024)]
      (with-open [w (if opts (clojure.java.io/writer bos opts) (clojure.java.io/writer bos))]
        (binding [*print-length* false
                  *out* w]
          (pr data)))
      ;;death to efficiency, but easiest way without writing something low-level to encode a stream directly into Kafka
      (.toByteArray bos)))
  (close [_]))

(defn edn-serializer
  (^EdnSerializer [] (edn-serializer nil))
  (^EdnSerializer [opts]
   (EdnSerializer. opts)))

(deftype SimpleEdnSerializer []
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    (some-> data pr-str .getBytes))
  (close [_]))

(defn simple-edn-serializer
  "A simple EDN deserializer for small amounts of data for Kafka.
  Useful for value serialization."
  ^SimpleEdnSerializer []
  (SimpleEdnSerializer.))

(deftype KeywordSerializer []
  Serializer
  (configure [_ _ _])
  (serialize [_ _ data]
    (some-> data name .getBytes))
  (close [_]))

(defn keyword-serializer
  "A serializer that serializers string values as keywords.
  Useful for key serializers."
  ^KeywordSerializer []
  (KeywordSerializer.))

(deftype DebugSerializer [logging-fn ^Serializer serializer]
  Serializer
  (configure [_ configs is-key]
    (logging-fn {:serializer serializer
                 :fn         :configure
                 :configs    configs
                 :is-key     is-key})
    (.configure serializer configs is-key))
  (serialize [_ topic data]
    (logging-fn {:serializer serializer
                 :fn         :serialize
                 :topic      topic
                 :data       data})
    (.serialize serializer topic data))
  (close [_]
    (logging-fn {:serializer serializer
                 :fn         :close})
    (.close serializer)))

(defn debug-serializer
  "Simple debug serializer that wraps your serializer and desired logging function.
  The logging function should take at least a single arity.
  The function will receive a map of state information with the following possible keys, which you may choose to destructure accordingly:

  * :serializer - An instance of the serializer itself
  * :fn - Keyword name of the function being logged. Possible values `[:configure :serialize :close]`
  * :configs - Optional, present when configuring serializer, and only applicable for certain types of serializers
  * :is-key - Optional, present when configuring the serializer, and only applicable for certain types of serializers
  * :topic The topic being serialized, when calling serialize.
  * :data The data being serialized, when calling serialize.

  Example usage:

  `(debug-serializer
          (fn [{:keys [fn serializer configs is-key topic data] :as m}]
            (timbre/debug \"full debug map:\" m)
            (when data
              (timbre/info \"data:\" data)))
            (edn-serializer))`"
  ^DebugSerializer
  [logging-fn ^Serializer serializer]
  (DebugSerializer. logging-fn serializer))

;;TODO: composite serializer?
