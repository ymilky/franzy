(ns franzy.clients.consumer.defaults
  (:require [schema.core :as s]
            [franzy.clients.consumer.schema :as cs]
            [franzy.clients.consumer.callbacks :as callbacks]
            [franzy.clients.consumer.types :as ct]))

(s/defn default-consumer-options [] :- cs/ConsumerOptions
  "Default consumer options."
  {:consumer-records-fn         seq
   :poll-timeout-ms             1000
   :offset-commit-callback      (callbacks/offset-commit-callback)
   :rebalance-listener-callback (callbacks/no-op-consumer-rebalance-listener)})

(s/defn ^:always-validate make-default-consumer-options :- cs/ConsumerOptions
  "Creates default consumer options, merging any provided options accordingly."
  ([]
    (make-default-consumer-options nil))
  ([options :- (s/maybe cs/ConsumerOptions)]
    (ct/make-consumer-options (merge (default-consumer-options) options))))
