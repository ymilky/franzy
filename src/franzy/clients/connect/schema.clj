(ns franzy.clients.connect.schema
  (:require [schema.core :as s]
            [franzy.common.schema :as fs]
            [franzy.common.models.schema :as fms]))

;;TODO: more restrictive schema + review
(def KafkaConnectConfig
  "Schema for a Kafka Connect Config.

  Note: Although Kafka Connect itself is not yet (or ever - rumblings about deprecation, also prefer Onyx, Storm, Spark, etc.), supported by this system, the configuration is provided here for integration with other Kafka clients or any application validation users may require.

  See http://kafka.apache.org/documentation.html#connectconfigs"
  {(s/required-key :bootstrap.servers)                        fs/NonEmptyStringOrStringList ;;TODO: more strict schema
   (s/optional-key :group.id)                                 s/Str
   (s/optional-key :internal.key.converter)                   s/Str ;TODO: class
   (s/optional-key :internal.value.converter)                 s/Str ;TODO: class
   (s/optional-key :key.converter)                            s/Str ;TODO: class
   (s/optional-key :value.converter)                          s/Str ;TODO: class
   (s/optional-key :cluster)                                  s/Str
   (s/optional-key :heartbeat.interval.ms)                    fs/PosInt
   (s/optional-key :session.timeout.ms)                       fs/SPosInt
   (s/optional-key :ssl.key.password)                         s/Str
   (s/optional-key :ssl.keystroke.location)                   s/Str
   (s/optional-key :ssl.keystore.password)                    s/Str
   (s/optional-key :ssl.truststore.location)                  s/Str
   (s/optional-key :ssl.truststore.password)                  s/Str
   (s/optional-key :connections.max.idle.ms)                  fs/SPosLong
   (s/optional-key :receive.buffer.bytes)                     fs/SPosInt
   (s/optional-key :request.timeout.ms)                       fs/SPosInt
   (s/optional-key :sasl.kerberos.service.name)               s/Str ;;TODO: list
   (s/optional-key :security.protocol)                        fms/SecurityProtocolEnum
   (s/optional-key :send.buffer.bytes)                        fs/SPosInt
   (s/optional-key :ssl.enabled.protocols)                    fs/StringOrStringList
   (s/optional-key :ssl.keystore.type)                        s/Str
   (s/optional-key :ssl.protocol)                             s/Str
   (s/optional-key :ssl.provider)                             s/Str
   (s/optional-key :ssl.truststore.type)                      s/Str
   (s/optional-key :worker.sync.timeout.ms)                   fs/SPosInt
   (s/optional-key :worker.unsync.backoff.ms)                 fs/SPosInt
   (s/optional-key :metadata.max.age.ms)                      fs/SPosLong
   (s/optional-key :metric.reporters)                         fs/StringOrStringList
   (s/optional-key :metric.num.samples)                       fs/PosInt
   (s/optional-key :metrics.sample.window.ms)                 fs/SPosLong
   (s/optional-key :offset.flush.interval.ms)                 fs/SPosLong
   (s/optional-key :offset.flush.timeout.ms)                  fs/SPosLong
   (s/optional-key :reconnect.backoff.ms)                     fs/SPosLong
   (s/optional-key :rest.advertised.host.name)                s/Str
   (s/optional-key :rest.advertised.host.port)                s/Str
   (s/optional-key :rest.host.name)                           s/Str
   (s/optional-key :rest.host.port)                           s/Str
   (s/optional-key :retry.backoff.ms)                         fs/SPosLong
   (s/optional-key :sasl.kerberos.kinit.cmd)                  s/Str
   (s/optional-key :sasl.kerberos.min.time.before.relogin)    fs/SPosLong
   (s/optional-key :sasl.kerberos.ticket.renew.jitter)        fs/SPosDouble
   (s/optional-key :sasl.kerberos.ticket.renew.window.factor) fs/SPosDouble
   (s/optional-key :ssl.cipher.suites)                        fs/StringOrStringList
   (s/optional-key :ssl.endpoint.identification.algorithm)    s/Str
   (s/optional-key :ssl.keymanager.algorithm)                 s/Str
   (s/optional-key :ssl.trustmanager.algorithm)               s/Str
   (s/optional-key :task.shutdown.graceful.timeout.ms)        fs/SPosLong})

