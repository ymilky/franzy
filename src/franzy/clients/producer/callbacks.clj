(ns franzy.clients.producer.callbacks
  (:require [franzy.clients.codec :as codec])
  (:import (org.apache.kafka.clients.producer Callback)))

;;deftype for debugging/meta purposes
(deftype NoOpSendCallback []
  Callback
  (onCompletion [_ _ _]))

(defn ^NoOpSendCallback no-op-send-callback []
  "Creates a no-op send callback, for testing, defaults, etc."
  (NoOpSendCallback.))

(defn send-callback
  "Creates a Kafka Java compatible callback for use with a producer send function.

  The callback will execute when the request is complete.
  This callback will generally execute in the background I/O thread so it should be fast, taking minimal time to execute.

  You may pass a Clojure function to create this callback, however it must be of 2 arity.
  The first argument will be record metadata as a map (converted from Java), and the second argument will be an exception.
  Your callback will receive one or the other as a value and should respond accordingly.

  Example:
  (send-callback (fn naming-me-might-help-debug [record-metadata e]
    (println \"Record metadata:\" record-metadata)
    (println \"Kafka said no, here's why:\" e))

  See https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/Callback.html for more details."
  (^Callback []
   "Creates a no-op callback."
   (no-op-send-callback))
  (^Callback [send-callback-fn]
   (reify Callback
     (onCompletion [_ record-metadata exception]
       (println "calling send callback...")
       (send-callback-fn (codec/decode record-metadata) exception))))
  (^Callback [record-metadata-fn exception-handler-fn]
   "Takes 2 functions, 1 to process record metadata, and another to process exceptions.
   This is a convenience function for those that prefer to separately handle record metadata and exceptions.
   You may alternatively prefer the 1-arity version and a function that closes over 2 functions."
   (reify Callback
     (onCompletion [_ record-metadata exception]
       (when record-metadata
         (record-metadata-fn (codec/decode record-metadata)))
       ;;rather than an if, an extra when to be a bit more bullet-proof because this was a bug for at least 1 build of Kafka
       ;;normally, these should be mutually exclusive, but trust is for the young
       (when exception
         (exception-handler-fn exception))))))

