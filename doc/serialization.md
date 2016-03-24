# Serialization

## Usage

Serializers/deserializers can be passed in the factory function when creating a consumer or producer.

Your usage of serializer and options should always be symmetrical. That is, whatever you encode, you must be able to decode. Do not for example mix a nippy serializer and a fressian deserializer. This applies at the partition level, but it is recommended you also do not mix across partitions as your consumer will be unable to handle the results of an encoding it is not prepared to decode. Likewise, the same applies to a producer.

Alternatively, you may specify the serializer/deserializer via qualified class reference by using the Kafka config properties:

* `value.serializer` - serializes your values, ex: "org.apache.kafka.common.serialization.StringSerializer"
* `key.serializer` - serializes your keys, ex: "org.apache.kafka.common.serialization.LongSerializer"
* `value.deserializer` - deserializes your values "org.apache.kafka.common.serialization.StringDeserializer"
* `key.deserializer` - deserializes your keys, ex: "org.apache.kafka.common.serialization.LongDeserializer"

## Serializers/Deserializers

The following serializers/deserializers are currently/planned to be available for your use:

| Name                 | Speed                      | Compression              | Ease of Use                                                                      |
|----------------------|----------------------------|--------------------------|----------------------------------------------------------------------------------|
| EDN                  | great                      | minimal, but can compose | great                                                                            |
| String               | great                      | none                     | poor, useful for keys or simple data only                                        |
| Keyword              | great                      | none                     | poor, useful for keys or simple data only                                        |
| Integer              | great                      | none                     | poor, useful for keys or simple data only                                        |
| Long                 | great                      | none                     | poor, useful for keys or simple data only                                        |
| Byte Array           | great                      | none, but can compose    | worst, hard in-line code, but versatile                                          |
| Simple EDN           | good, great for small data | minimal, but can compose | great, but can lead to easy OOM error                                            |
| JSON                 | good                       | none/native json         | good, but requires settings/handlers for complex types/preservations of types    |
| JSON Smile           | ok                         | good                     | good, but same as JSON with possible risk of Smile-issues                        |
| Nippy                | very good                  | great                    | great, probably the best current balance of speed and compression                |
| Fressian             | good                       | very good                | good, but prefer nippy in most cases unless domain reasons                       |
| Debug                | poor/depends               | depends                  | ok, not intended for production use, but can compose anything and pass back info |
| Transit              | great                      | good                     | great, but should match use case, otherwise prefer nippy                         |
| Avro                 | great                      | great                    | poor, a bit pedantic and requires up-front schema                                |
| UUID/SQUID (planned) | great                      | none                     | poor, useful for keys or simple data only                                        |
| Gzip (planned)       | great                      | very good                | prefer nippy, unless specific domain reason                                      |

# Available Serializers

* EDN - built-in, good for Clojure data values
* Simple EDN - built-in, good for small Clojure values
* Integer - built-in, good for keys
* Long - built-in, good for keys
* String built-in, good for keys
* Byte Array - built-in, good for values, use if you want to manually handle de/serialization for some strange reason
* Keyword - built-in, good for keys
* Debug - built-in, good for debugging, a shocker - can compose other serializers and log the output
* [Franzy-JSON](https://github.com/ymilky/franzy-json) - JSON Serialization with optional Smile support
* [Franzy-Nippy](https://github.com/ymilky/franzy-nippy) - Nippy serialization - *highly recommended*
* [Franzy-Fressian](https://github.com/ymilky/franzy-fressian) - [Fressian](https://github.com/Datomic/fressian) serialization, especially useful for those integrating with [Datomic](http://www.datomic.com)
* [Franzy Transit](https://github.com/ymilky/franzy-transit) - [Transit]() with support for JSON, JSON-verbose, msgpack
* [Franzy Avro](https://github.com/ymilky/franzy-avro) - [Avro](https://avro.apache.org/) with support for EDN
