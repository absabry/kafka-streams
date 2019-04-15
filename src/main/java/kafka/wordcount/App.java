package kafka.wordcount;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;
import java.util.Arrays;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static kafka.Utils.Common.createKafkaStreamsConfiguration;

public class App {
    static Logger logger = LoggerFactory.getLogger(App.class.getSimpleName());

    public static void main(String[] args) {
        Properties config = createKafkaStreamsConfiguration("wordcount-app");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream("word-count");

        textLines.print(Printed.toSysOut());

        KTable<String, Long> wordCounts = textLines // <NULL, " Hello WorLD ">
                .flatMapValues(textLine -> Arrays.asList(textLine.split("\\W+"))) // <NULL, " Hello ">, <NULL, "WorLD ">
                .mapValues(val -> val.replaceAll("\\s+", "").toLowerCase())  // <NULL, "hello">, <NULL, "world">
                .filter((key, val) -> !val.equals(""))// remove null strings
                .selectKey((key, word) -> word)// <"hello", "hello">, <"world", "world">
                .groupByKey()// <"hello">, <"world">
                .count();// <"hello",1>, <"world",1>

        wordCounts.toStream().print(Printed.toSysOut());

        wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
