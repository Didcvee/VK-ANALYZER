package ru.didcvee;

import com.google.gson.JsonElement;
import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.UserSettingsXtr;
import com.vk.api.sdk.objects.users.responses.GetResponse;

import java.util.List;

public class NewMain {
    public static void main(String[] args) throws ClientException, ApiException {
        TransportClient transportClient = new HttpTransportClient();

        VkApiClient vk = new VkApiClient(transportClient);


    GroupActor actor = new GroupActor(788823275L, "vk1.a.PfudU4ftZPJ-Y29MIFFHxuwE3XKeJC9nfnkBwXMbCH2J6TIMK4jYgKNEsU6b0904QToNTU-P3mtLBO6LXooiJDGjINm416O9spHRcc9fS6xY6haheu4TygMJjgsd7q2aUczyHQr6ActEuz52Kv3fWi1iWXeh1WhVgjw5dAsNWFnGzyg0DhoUHD5ZkbjLrACi");
        List<GetResponse> users = vk.users().get(actor)
                .userIds("1")
                .fields(Fields.COUNTRY)
                .lang(Lang.EN)
                .execute();
        System.out.println(users);

        JsonElement response = vk.execute().code(actor, "return API.wall.get({\"count\": 1});").execute();
        System.out.println(response);

        UserActor userActor = new UserActor(788823275L, "vk1.a.PfudU4ftZPJ-Y29MIFFHxuwE3XKeJC9nfnkBwXMbCH2J6TIMK4jYgKNEsU6b0904QToNTU-P3mtLBO6LXooiJDGjINm416O9spHRcc9fS6xY6haheu4TygMJjgsd7q2aUczyHQr6ActEuz52Kv3fWi1iWXeh1WhVgjw5dAsNWFnGzyg0DhoUHD5ZkbjLrACi");

        JsonElement response23 = vk.execute().batch(actor,
                vk.database().getChairs(userActor).count(10),
                vk.database().getCities(userActor),
                vk.groups().getMembers(actor).groupId("RTK")
        ).execute();
        System.out.println(response23);

    }
}
