#!/bin/bash

# delete topics if already existed
kafka-topics --delete --zookeeper localhost:2181 --topic fav-.*

# create input topic with one partition to get full ordering
kafka-topics --create --zookeeper localhost:2181\
 --replication-factor 1 --partitions 1 \
 --topic fav-color \

 # create input topic with one partition to get full ordering
 kafka-topics --create --zookeeper localhost:2181\
  --replication-factor 1 --partitions 1 \
  --topic fav-color-intern \
  --config cleanup.policy=compact

 # create input topic with one partition to get full ordering
 kafka-topics --create --zookeeper localhost:2181\
  --replication-factor 1 --partitions 1 \
  --topic fav-color-output \
  --config cleanup.policy=compact

 # config is not really essential for using it here, it's just some optimization for production

# launch a Kafka consumer
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic fav-color-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

# launch the streams application

# then produce data to it
bin/kafka-console-producer \
--broker-list localhost:9092 \
--topic favourite-colour-input \
--property "parse.key=true" \
--property "key.separator=,"

stephane,blue
john,green
stephane,red
alice,red


# list all topics that we have in Kafka (so we can observe the internal topics)
bin/kafka-topics.sh --list --zookeeper localhost:2181
