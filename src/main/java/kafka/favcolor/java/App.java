package kafka.favcolor.java;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static kafka.Utils.Common.createKafkaStreamsConfiguration;

public class App {
    /**
     * Topics travels like :
     * fav-color --> fav-color-intermediate  --> fav-color-output
     */

    public static void main(String[] args) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Properties config = createKafkaStreamsConfiguration("favcolor-app");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> inputData = builder.stream("fav-color");
        inputData.filter((user, color) -> user != null && color != null);
        inputData.to("fav-color-intern");
        inputData.foreach((x, y) -> System.out.println(
                "Got from input data topic " + x + ":" + y +
                        " @" + dateFormat.format(new Date())));

        KTable<String, String> userColors = builder.table("fav-color-intern");
        userColors.toStream().foreach((x, y) -> System.out.println(
                "Got from intern KTable topic " + x + ":" + y +
                        " @" + dateFormat.format(new Date())));
        KTable<String, Long> countColors = userColors // <stephane, blue>
                .groupBy((user, color) -> new KeyValue<>(color, color))
                .count(Materialized.as("Count-fav-color"));
        countColors.toStream().foreach((x, y) -> System.out.println(
                "Got from resulting KTable topic " + x + ":" + y +
                        " @" + dateFormat.format(new Date())));
        countColors.toStream().to("fav-color-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp(); // when we launch the app multiple times, we better add this
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
