package ru.didcvee;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.wall.GetFilter;
import com.vk.api.sdk.objects.wall.responses.GetResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;

public class Main {
    public static void main(String[] args) throws Exception {
        TransportClient transportClient = new HttpTransportClient();

        VkApiClient vk = new VkApiClient(transportClient);



        Integer APP_ID = 51831767;
        String CLIENT_SECRET = "1SYjTklk0jYm2RdDsISe";
        String REDIRECT_URI = "https://kafka-test-123.com";
//        String code = "12e4128312e4128312e41283dc11f2f154112e412e412837746b780999802b7f0bbc930"; // vk1.a.1YUKvTMdlBE4sSePNS9BGWpqY5l6CXCCxoUeSHCk7gzLuOH-9a81wbw4JWx1SFbNsNyifCywYV2Dwa_oC2cMmXhK_R13bVCVFTdK-cTk5k5gH4VByoLZ2YEa0j1b1Y1TuhB_V9ksh6VfYl9abkaTeP1f7SW1W-bKrxGingIbf-K63rTXgjz9uIIx9dR_WBSp
        String code = "59aae23ae0c5b3527e";
        // https://oauth.vk.com/authorize?client_id=51831767&display=page&redirect_uri=https://kafka-test-123.com&response_type=code&v=5.131


//


//        UserAuthResponse authResponse;
//        try {
//            authResponse = vk.oAuth()
//                    .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI, code)
//                    .execute();
//        } catch (ApiException e) {
//            throw new RuntimeException(e);
//        } catch (ClientException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println(authResponse.getUserId()+"\n"+authResponse.getAccessToken());

        UserActor actor = new UserActor(788823275L, "vk1.a.PfudU4ftZPJ-Y29MIFFHxuwE3XKeJC9nfnkBwXMbCH2J6TIMK4jYgKNEsU6b0904QToNTU-P3mtLBO6LXooiJDGjINm416O9spHRcc9fS6xY6haheu4TygMJjgsd7q2aUczyHQr6ActEuz52Kv3fWi1iWXeh1WhVgjw5dAsNWFnGzyg0DhoUHD5ZkbjLrACi");
        GetResponse getResponse = vk.wall().get(actor)
                .ownerId(1L)
                .count(100)
                .offset(5)
                .filter(GetFilter.OWNER)
                .execute();
        System.out.println(getResponse.getItems().get(3));
        System.out.println(actor.getAccessToken());

    }
    private static String getAccessToken(Integer appId, String clientSecret, String redirectUri) throws Exception {
        String url = "https://oauth.vk.com/authorize?" +
                "client_id=" + appId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&response_type=code&v=5.131";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        System.out.println(response);

        String accessToken = response.toString().split("\"")[3];

        return accessToken;
    }
}