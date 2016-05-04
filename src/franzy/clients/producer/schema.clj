(ns franzy.clients.producer.schema
  "Schemas for Kafka Producers and related types.

  For some context, see http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/package-frame.html"
  (:require [schema.core :as s]
            [franzy.common.schema :as fs]
            [franzy.common.models.schema :as fms])
  (:import (org.apache.kafka.clients.producer Callback)))

;;;TODO: more restrictive schema
;(def ProducerCallback
;  (s/make-fn-schema s/Any [s/Any s/Any]))

;;TODO: more restrictive schema, callback schema
(def ProducerOptions
  "Schema for a Franzy-specific Kafka Producer."
  {(s/optional-key :close-timeout)      fs/SPosInt
   (s/optional-key :close-timeout-unit) fms/TimeUnitEnum
   (s/optional-key :send-callback)      Callback})

;;TODO: more restrictive schema
(def ProducerConfig
  "Schema for a Kafka Producer Configuration.

  http://kafka.apache.org/documentation.html#producerconfigs"
  {(s/required-key :bootstrap.servers)                        fs/NonEmptyStringOrStringList ;;TODO: more strict schema
   (s/optional-key :key.serializer)                           s/Str
   (s/optional-key :value.serializer)                         s/Str
   (s/optional-key :acks)                                     fms/KafkaAck
   (s/optional-key :buffer.memory)                            fs/SPosLong
   (s/optional-key :compression.type)                         s/Str
   (s/optional-key :retries)                                  fs/SPosInt
   (s/optional-key :ssl.key.password)                         s/Str
   (s/optional-key :ssl.keystroke.location)                   s/Str
   (s/optional-key :ssl.keystore.password)                    s/Str
   (s/optional-key :ssl.truststore.location)                  s/Str
   (s/optional-key :ssl.truststore.password)                  s/Str
   (s/optional-key :batch.size)                               fs/SPosInt
   (s/optional-key :client.id)                                s/Str
   (s/optional-key :connections.max.idle.ms)                  fs/SPosLong
   (s/optional-key :linger.ms)                                fs/SPosLong
   (s/optional-key :max.block.ms)                             fs/SPosLong
   (s/optional-key :max.request.size)                         fs/SPosInt
   (s/optional-key :partitioner.class)                        s/Str
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
   (s/optional-key :timeout.ms)                               fs/SPosInt
   (s/optional-key :block.on.buffer.full)                     s/Bool
   (s/optional-key :max.in.flight.requests.per.connection)    fs/PosInt
   (s/optional-key :metadata.fetch.timeout.ms)                fs/SPosLong
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
   (s/optional-key :ssl.trustmanager.algorithm)               s/Str
   (s/optional-key :schema.registry.url)                      s/Str})

(def ProducerRecord
  "Schema for a Kafka Producer Record.

  See http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html"
  {(s/required-key :topic)     fs/NonEmptyString
   ;Optional for now, but always need to check and set it to zero in that case.
   ;I prefer explicit behavior of which partition to send data to and probably an awful idea to default data to a partition in a system like Kafka
   (s/optional-key :partition) fs/SPosInt
   ;Optional, but a really bad idea to omit, unless using a string-based for a key. Kafka itself considered axing this behavior
   ;Making key required though might break a lot of existing code. Make required? TBD...
   (s/optional-key :key)       fs/AnyButNil
   (s/required-key :value)     fs/AnyButNil})

(def RecordMetadata
  "Schema for Kafka Record Metadata

  See http://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/RecordMetadata.html"
  {(s/required-key :topic)     fs/NonEmptyString
   (s/required-key :partition) fs/SPosInt
   (s/required-key :offset)    fs/SPosLong})
