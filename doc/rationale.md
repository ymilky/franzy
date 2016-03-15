# Rationale

Franzy was created to be a self-serving Kafka client with a few specific goals in mind that differ from existing Kafka clients in various ways. It turns out that selfishness is sometimes useful to other people. With that in mind, some of the useful bits from a larger app using this code base were extracted, while the more specific bits continue to be selfish. Maybe one day they will find a home here.

Many people might say, why another Clojure client? There are many reasons, some outlined below. The simple version is I wrote a lot of code for my own project and I was nice enough to open-source. You can be nice enough to either use it or not use it, but don't complain.

I started really getting annoyed with certain clients, including the official clients. At one point I needed something very high-performance, so I started writing a low-level client from scratch in Rust. While it ran awesome for the basics I implemented, I decided I had no interest in maintaining it. Thus, I decided to suck it up and accept that wrappers while crude free me from dealing with most issues while still allowing a decent interface. 

As the Kafka 0.9 client release was announced, I started developing a wrapper directly from their git repo. Months later, I open sourced it and it's what you see now. I have a lot of other projects on my plate, and my interest was a feature complete Kafka platform in addition to the consumer and producer. I hope that's what I have provided and that others will step in to help improve and maintain it. I have a lot of suggestions and ideas, so there's much room for improvement.

## General

* Pure Clojure data structures, no Kafka Java API objects or otherwise floating around for users of this library to worry about managing
* Mostly Clojure style conventions - keyword keys, lowercase functions and keys, Clojure data structures as parameters
* Transparent results regardless of format to/from Kafka - Clojure in, Clojure out
* Do not reinvent the wheel too much - accomplished via wrapping the existing, battle-tested Java APIs as much as possible
     * Franzy originally started implementing the from scratch including the protocol, and while this proved to be faster with more elegant concurrency possibilities, I have no resources to support such a large endeavour. If you plan to do this, I would be happy to collaborate and add to Franzy.
     * Although writing a low-level client is fast and interesting, it has been my experience as well as many others that these implementations are often lacking features and worse, highly problematic and incorrect.
     * As of this writing, I feel Kafka is a bit in flux on some things and better to let the core developers implement it rather than always being a few steps behind.
* Give the option of either storing offsets in Kafka or in your own datastore
     * Avoid forcing a particular datastore on users such as Zookeeper or Redis, within reason
* Extensibility
     * Allow people to easily add serializers, useful callbacks, offset management strategies, partitioning strategies, etc.
     * Use what is already done toward the goal of extensibility in the official Java client, but wrap it in a more Clojure-friendly way
* Easy integration with core.async, manifold, built-in Java/Clojure libraries, or any other async libraries
     * Make data available as persistent collections and support transducers toward this goal

## Compatibility

* Provide a Kafka client with 0.9 (and hopefully above) compatibility
* Support a few important APIs that may be deprecated, but are at least important in the short-term to getting things done
* Clojure 1.8 (and above) support

## Performance

* Reasonable performance, bearing in mind overhead of conversions and wrapping Java in Clojure.
* Avoid realizing lazy results when unnecessary, likewise avoiding lazy overhead when unnecessary.
* No neat Clojure tricks that add needless overhead to key functions. For example, no extra apply tricks and giant cond blocks for producer and consumer related operations for the sake of allowing many ways to call the same thing.
* Ability to toggle validation on/off as required. Sometimes, we like to swim in shark infested waters.

## Stability and Usage

* Validation of configurations and important API data structures.
* No guessing what a map, vector, etc. is supposed to contain or having to refer to the code just to know a parameter type.
* Reasonable amount of schema/type hints.

## Integrations

* Ability to be integrated easily into plug-ins and add-ons for stream processing related libraries and other things that typically interact closely with Kafka
* Easy [Onyx](https://github.com/onyx-platform/onyx) input/output task integration via plug-in
     * Granular control of serialization, deserialization options, batch size, offset seek, and more via catalog
     * No unnecessary dependencies pulled into Onyx
     * No forcing a particular format such as json to/from Onyx

## Other Clients

There are a number of other excellent clients that already exist and I suggest you happily use them instead of this one if it makes sense for you. This includes clients I could have used for Java, Clojure, Scala, and Node.js among other languages.

For Clojure, I found the following clients to be useful in various ways, please thank the authors and/or use their clients instead of this one:

* [clj-kafka](https://github.com/pingles/clj-kafka) - Great client that I used in many other projects
* [Kinsky](https://github.com/pyr/kinsky) - 0.9 and basic async support/example
* [Kafka-Fast](https://github.com/gerritjvv/kafka-fast) - Low-level, excellent, fast client that uses Redis for offset management

## Why Another?

Unfortunately, no existing clients met the various points listed in the sections above. The following section is by no means directed at/only at Clojure clients, but rather clients as a whole. Anything listed here should also apply to Franzy as well (if not already fixed/doesn't apply) and be an issue or work-item.

Generally, a lot of authors, especially those that wrap clients have a nasty habit of dropping functionality. Key areas include security, partitioning, and many of the more advanced settings that users should be using. Kafka makes it very easy to create a configuration and client, then make it work, however this does not mean it will work optimally. Some examples of dropped behavior include overloads, important constructors, etc. While this is understandable, there is often a reason why the source implementations provide this functionality and it is often not just syntatic sugar.

Without getting overly specific, many other clients did one or more of the following to make themselves less suitable to my needs:

* No recent commits or activity otherwise
* Outdated dependencies such as Clojure
* Indecisive about supporting new versions of Kafka
* Slow, too much overhead, or too much extra code for inter-op with Clojure
* Broken on Kafka 0.9 and above or no support for 0.9 consumer API
* Did not follow the Kafka protocol spec correctly - relevant only to any non-wrapped clients
* Mistakes in consumer rebalancing, threading, clustering, and otherwise doing things that made life impossible
* Missing key information in result sets like offsets, metadata, etc. and no ability to fetch them easily without breaking the abstraction or spinning what amounted to an entirely new client anyway
* Slow/neutered/incorrect/baked-in serialization - many clients force unnecessary dependencies for serializers, don't use them efficiently, neglect streams, etc.
* Realizing lazy data eagerly when not necessary or even consistent with Kafka itself
* No support for both synchronous and asynchronous consumption
* Flaws in key algorithms such as offset management and consumer positioning
* API warts such as requiring inline conversions to byte arrays for producers
* Crippled security settings in consumer, producer, admin, etc. SSL support at a minimum should be a non-starter, even if you are not currently required to use it
* Memory leaks, connection leaks, bizarre semantics that are non-standard to the interfaces the client is written against, etc.
* Dropped data, especially metadata from many API method requests/responses. While the author may not have needed that data, many of us do.
