package ru.didcvee;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ElasticSearchConsumer {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchConsumer.class);
    public static RestHighLevelClient createClient(){
        String hostname = "spb-search-80213326.eu-central-1.bonsaisearch.net";
        String username = "b9xigm3uj6";
        String password = "6g8dbhghkd";

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username,password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        RestHighLevelClient client = createClient();


        KafkaConsumer<String, String> consumer = createConsumer("VK_TOPIC");

        BulkRequest bulkRequest = new BulkRequest();

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            Integer recordCount = records.count();
            System.out.println("Received: " + recordCount + " records");

            records.forEach(record -> {
                Post post = new Post(record.value());
                String id1 = record.value().split("\n")[record.value().split("\n").length - 1];
                System.out.println(id1);
                String jsonString = new Gson().toJson(post);
                System.out.println(jsonString);
                IndexRequest indexRequest = new IndexRequest(
                        "vk",
                        "_doc",
                        id1
                ).source(jsonString, XContentType.JSON);

                bulkRequest.add(indexRequest);

            });
            if(recordCount>0){

            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            System.out.println("Committing offsets...");
            consumer.commitSync();
            System.out.println("Offsets have been committed");
            Thread.sleep(1000);
            }

        }

        // close the client gracefully
//        client.close();

    }

    public static KafkaConsumer<String, String> createConsumer(String topic){
            String bootstrapServers = "localhost:9092";
            String groupId = "kafka-demo-elasticsearch";


            // Создание настроек консюмера
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // earliest/latest/none
            properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
            // Создание консюмера
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(Arrays.asList(topic));

            return consumer;
    }
}
