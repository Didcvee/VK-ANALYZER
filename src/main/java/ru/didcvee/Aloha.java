package ru.didcvee;


import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.newsfeed.responses.SearchResponse;
import com.vk.api.sdk.objects.wall.WallpostFull;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Aloha {
    private static final Logger log = LoggerFactory.getLogger(Aloha.class);

    private static Set<Integer> processedPosts = new HashSet<>();

    public static void main(String[] args) {

        Instant now = Instant.now();
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);
        long startTime = threeDaysAgo.getEpochSecond() + 10 * 3600; // добавляем 10 часов, чтобы начало интервала было с 10 утра
        long endTime = threeDaysAgo.getEpochSecond() + 18 * 3600; // добавляем 18 часов, чтобы конец интервала был с 6 вечера

        TransportClient transportClient = new HttpTransportClient();

        VkApiClient vk = new VkApiClient(transportClient);


        ServiceActor actor = new ServiceActor(788823275, "1SYjTklk0jYm2RdDsISe", "vk1.a.PfudU4ftZPJ-Y29MIFFHxuwE3XKeJC9nfnkBwXMbCH2J6TIMK4jYgKNEsU6b0904QToNTU-P3mtLBO6LXooiJDGjINm416O9spHRcc9fS6xY6haheu4TygMJjgsd7q2aUczyHQr6ActEuz52Kv3fWi1iWXeh1WhVgjw5dAsNWFnGzyg0DhoUHD5ZkbjLrACi");

        KafkaProducer<String, String> producer = createKafkaProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("app stop");
            producer.close(); // закрываем, чтобы кафка приняла все сообщения, которые есть в памяти приложения
        }));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    SearchResponse response = vk.newsfeed().search(actor)
                            .q("путин")
                            .count(200)
                            .startTime((int) startTime)
                            .endTime((int) endTime)
                            .execute();
                    System.out.println("RESPONSE" + response);
                    response.getItems().stream()
                            .filter(item -> !processedPosts.contains(item.getId())) // проверяем, не обработан ли уже этот пост
                            .forEach(item -> {
                                System.out.println("CREATED BY: " + item.getCreatedBy() + " DATE: " + LocalDateTime.ofEpochSecond(item.getDate(), 0, ZoneOffset.UTC) + " TYPE: " + item.getType() + ""
                                        + " ID: " + item.getId() + " OWNER ID: " + item.getOwnerId() + " LIKES: " + item.getLikes()+"\n "+item.getText() + "\n" + "-----------------------------------------------------------------");
                                processedPosts.add(item.getId()); // добавляем идентификатор поста в множество обработанных постов
                                // отправка


                                    producer.send(new ProducerRecord<>("VK_TOPIC", null, item.getText() + "\n" + item.getOwnerId()), new Callback() {
                                        @Override
                                        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                                            if(e!=null){
                                                log.error("Something bad happen", e);
                                            }
                                        }
                                    });


                            });
                } catch (ApiException | ClientException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 30000);
    }
    public static KafkaProducer<String, String> createKafkaProducer(){
        String BOOTSTRAP_SERVERS = "localhost:9092";
        // зависимости кафки
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // создание safe producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        // высокая пропускная способность (за счет небольшой задержки и загрузки процессора)
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024)); // 32KB BATCH SIZE


        // создание продюсера

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        return producer;
    }
}



