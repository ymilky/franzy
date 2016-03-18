(ns franzy.clients.producer.defaults
  (:require [schema.core :as s]
            [franzy.clients.producer.schema :as ps]
            [franzy.clients.producer.types :as pt]
            [franzy.clients.producer.callbacks :as callbacks])
  (:import (java.util.concurrent TimeUnit)))

(s/defn default-producer-options [] :- ps/ProducerOptions
  "Default producer options."
  {:close-timeout      3000
   :close-timeout-unit TimeUnit/MILLISECONDS
   :send-callback      (callbacks/send-callback)})

(s/defn make-default-producer-options :- ps/ProducerOptions
  "Creates default producer options, mergining any provided options accordingly."
  ([]
    (make-default-producer-options nil))
  ([options :- (s/maybe ps/ProducerOptions)]
    (pt/make-producer-options (merge (default-producer-options) options))))
