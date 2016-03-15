(ns franzy.clients.consumer.schema
  "Schemas for Kafka Consumers and related types.

  For some context, see http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/package-frame.html"
  (:require [schema.core :as s]
            [franzy.common.schema :as fs]
            [franzy.common.models.schema :as fms])
  (:import (org.apache.kafka.clients.consumer OffsetResetStrategy OffsetCommitCallback ConsumerRebalanceListener)
           (java.nio ByteBuffer)))

;;TODO: cleaner enum handling
(def OffsetResetStrategyEnum
  "Schema for a Kafka Offset Reset Strategy

  See http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/OffsetResetStrategy.html"
  ;;TODO: switch back to keywords via coercer + java.util.EnumSet/allOf per enum
  ;;LATEST, EARLIEST, NONE - must be lower, roar my lion!
  ;;Note the Java implementation of this enum is incomplete (roar), standard tricks don't apply based on what the app is expecting
  (apply s/enum (map (comp keyword str clojure.string/lower-case) (OffsetResetStrategy/values))))

(def ConsumerOptions
  "Schema for options for a Kafka Franzy-specific consumer."
  {(s/optional-key :consumer-records-fn)         fs/Function ;;TODO: more restrictive check?
   (s/optional-key :poll-timeout-ms)             fs/SPosInt
   (s/optional-key :offset-commit-callback)      (s/maybe OffsetCommitCallback)
   (s/optional-key :rebalance-listener-callback) (s/maybe ConsumerRebalanceListener)})

(def ConsumerConfig
  "Schema for a Kafka Consumer configuration, passed as properties to Kafka.

  See http://kafka.apache.org/documentation.html#consumerconfigs"
  {(s/required-key :bootstrap.servers)                        fs/NonEmptyStringOrStringList ;;TODO: more strict schema
   (s/optional-key :key.deserializer)                         s/Str
   (s/optional-key :value.deserializer)                       s/Str
   (s/optional-key :fetch.min.bytes)                          fs/PosInt
   (s/optional-key :group.id)                                 s/Str
   (s/optional-key :heartbeat.interval.ms)                    fs/PosInt
   (s/optional-key :max.partition.fetch.bytes)                fs/PosInt
   (s/optional-key :session.timeout.ms)                       fs/SPosInt
   (s/optional-key :ssl.key.password)                         s/Str
   (s/optional-key :ssl.keystroke.location)                   s/Str
   (s/optional-key :ssl.keystore.password)                    s/Str
   (s/optional-key :ssl.truststore.location)                  s/Str
   (s/optional-key :ssl.truststore.password)                  s/Str
   (s/optional-key :auto.offset.reset)                        OffsetResetStrategyEnum
   (s/optional-key :connections.max.idle.ms)                  fs/SPosLong
   (s/optional-key :enable.auto.commit)                       s/Bool
   (s/optional-key :partition.assignment.strategy)            fs/StringOrStringList
   (s/optional-key :receive.buffer.bytes)                     fs/SPosInt
   (s/optional-key :request.timeout.ms)                       fs/SPosInt
   (s/optional-key :sasl.kerberos.service.name)               s/Str
   (s/optional-key :security.protocol)                        fms/SecurityProtocolEnum
   (s/optional-key :send.buffer.bytes)                        fs/SPosInt
   (s/optional-key :ssl.enabled.protocols)                    fs/StringOrStringList
   (s/optional-key :ssl.keystore.type)                        s/Str
   (s/optional-key :ssl.protocol)                             s/Str
   (s/optional-key :ssl.provider)                             s/Str
   (s/optional-key :ssl.truststore.type)                      s/Str
   (s/optional-key :auto.commit.interval.ms)                  fs/SPosLong
   (s/optional-key :check.crcs)                               s/Bool
   (s/optional-key :client.id)                                s/Str
   (s/optional-key :fetch.max.wait.ms)                        fs/PosInt
   (s/optional-key :metadata.max.age.ms)                      fs/SPosLong
   (s/optional-key :metric.reporters)                         fs/StringOrStringList
   (s/optional-key :metric.num.samples)                       fs/PosInt
   (s/optional-key :metrics.sample.window.ms)                 fs/SPosLong
   (s/optional-key :reconnect.backoff.ms)                     fs/SPosLong
   (s/optional-key :retry.backoff.ms)                         fs/SPosLong
   (s/optional-key :sasl.kerberos.kinit.cmd)                  s/Str
   (s/optional-key :sasl.kerberos.min.time.before.relogin)    fs/SPosLong
   (s/optional-key :sasl.kerberos.ticket.renew.jitter)        fs/SPosDouble
   (s/optional-key :sasl.kerberos.ticket.renew.window.factor) fs/SPosDouble
   (s/optional-key :ssl.cipher.suites)                        fs/StringOrStringList
   (s/optional-key :ssl.endpoint.identification.algorithm)    s/Str
   (s/optional-key :ssl.keymanager.algorithm)                 s/Str
   (s/optional-key :ssl.trustmanager.algorithm)               s/Str})

(def ConsumerRecord
  "Schema for a Kafka Consumer Record.

  See http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/ConsumerRecord.html"
  {(s/required-key :topic)     fs/NonEmptyString
   (s/required-key :partition) fs/SPosInt
   (s/required-key :offset)    fs/SPosLong
   (s/required-key :key)       fs/AnyButNil
   (s/required-key :value)     fs/AnyButNil})

;;TODO: more strict
(def ConsumerRebalanceListenerCallbackFn
  "Schema for a Kafka Consumer Rebalance Listener Callback.

  See http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/ConsumerRebalanceListener.html"
  (s/make-fn-schema s/Any [[s/Any s/Any]]))

;;checks for the right keys, but the fn schema is purely descriptive per schema docs. meh. Probably can be fixed.
(def ConsumerRebalanceListenerCallbacks
  "Schema for creating consumer rebalance callbacks from clojure functions."
  {(s/optional-key :partitions-assigned-fn) ConsumerRebalanceListenerCallbackFn
   (s/optional-key :partitions-revoked-fn)  ConsumerRebalanceListenerCallbackFn})

(def ConsumerRebalanceListenerCallback
  "Schema for a consumer rebalance callback."
  (s/pred (partial instance? ConsumerRebalanceListener) 'consumer-rebalance-listener?))

(def OffsetAndMetadata
  "Schema for Kafka offset commit metadata.

  See http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/OffsetAndMetadata.html"
  {(s/required-key :offset)   fs/SPosLong
   (s/required-key :metadata) (s/maybe s/Str)})

;;TODO: possibly deprecate as this really needs to be an object with methods, not just data - keeping for now to use to return partition assignment data
(def PartitionAssignment
  "Schema for a Kafka partition assignment."
  {(s/required-key :topics)    (s/maybe fs/StringOrStringList)
   (s/required-key :user-data) (s/maybe ByteBuffer)})
