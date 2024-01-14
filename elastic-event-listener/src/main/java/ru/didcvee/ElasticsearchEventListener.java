package ru.didcvee;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;

public class ElasticsearchEventListener {
    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = createClient();

        // Создаем запрос для поиска новых записей с определенными словами
        SearchRequest searchRequest = new SearchRequest("vk");
        // Устанавливаем скролл для поиска всех результатов
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().size(100);
        sourceBuilder.query(QueryBuilders.termsQuery("text", "выбор"));
//        sourceBuilder.sort("your_timestamp_field", SortOrder.DESC);
        searchRequest.source(sourceBuilder);
        searchRequest.scroll(new Scroll(TimeValue.timeValueMinutes(1L)));

        // Выполняем запрос
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        String lastExecutionTime = null;

        while (true) {
            SearchRequest searchRequest = new SearchRequest("your_index");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().size(100);

            // Добавьте условие времени
            if (lastExecutionTime != null) {
                sourceBuilder.query(QueryBuilders.rangeQuery("your_timestamp_field").gt(lastExecutionTime));
            }

            searchRequest.source(sourceBuilder);
            searchRequest.scroll(new Scroll(TimeValue.timeValueMinutes(1L)));

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            String scrollId = searchResponse.getScrollId();
            SearchHits hits = searchResponse.getHits();

            for (SearchHit hit : hits.getHits()) {
                String documentId = hit.getId();
                String documentSource = hit.getSourceAsString();
                System.out.println("Found new document with ID: " + documentId);
                System.out.println("Document source: " + documentSource);

                // Добавьте код для обработки новых записей в базе данных
                // ...
            }

            // Добавьте задержку
            Thread.sleep(1000);

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(new Scroll(TimeValue.timeValueMinutes(1L)));

            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);

            if (searchResponse.getHits().getHits().length == 0) {
                // Обновите время последнего выполнения запроса
                lastExecutionTime = Instant.now().toString();

                // Перезапустите цикл скроллинга
                continue;
            }
        }





        client.close();
    }

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



}
