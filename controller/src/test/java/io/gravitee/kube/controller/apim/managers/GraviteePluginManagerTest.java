/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.kube.controller.apim.managers;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class GraviteePluginManagerTest {
    @Rule
    public KubernetesServer crudServer = new KubernetesServer(true, false);

    private CustomResourceDefinitionContext customResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
            .withGroup("gravitee.io")
            .withName("gravitee-plugins.gravitee.io")
            .withPlural("gravitee-plugins")
            .withScope("Namespaced")
            .withVersion("v1alpha1")
            .build();
    @Test
    public void test() throws Exception {
        final String body = "{\n" +
                "  \"apiVersion\": \"gravitee.io/v1alpha1\",\n" +
                "  \"kind\": \"GraviteePlugins\",\n" +
                "  \"metadata\": {\n" +
                "    \"name\": \"internal-gw-plugins\"\n" +
                "  },\n" +
                "  \"spec\": {\n" +
                "    \"plugins\": {\n" +
                "      \"key-less-poc\": {\n" +
                "        \"type\": \"security\",\n" +
                "        \"identifier\": \"key_less\"\n" +
                "      },\n" +
                "      \"jwt-poc\": {\n" +
                "        \"type\": \"security\",\n" +
                "        \"identifier\": \"jwt\",\n" +
                "        \"configuration\": {\n" +
                "          \"signature\": \"HMAC_HS256\",\n" +
                "          \"publicKeyResolver\": \"GIVEN_KEY\",\n" +
                "          \"useSystemProxy\": false,\n" +
                "          \"extractClaims\": false,\n" +
                "          \"propagateAuthHeader\": true,\n" +
                "          \"resolverParameter\": \"ae0368e97a7574a05995c0bf535fa395776f2e941715870cf5dfab3be6868364\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"oauth2\": {\n" +
                "        \"type\": \"security\",\n" +
                "        \"identifier\": \"oauth2\",\n" +
                "        \"configuration\": {\n" +
                "          \"oauthResource\": \"default.internal-gw-plugins.oauth2-resource\",\n" +
                "          \"extractPayload\": false,\n" +
                "          \"checkRequiredScopes\": false,\n" +
                "          \"requiredScopes\": [\n" +
                "            \"email\",\n" +
                "            \"profile\"\n" +
                "          ],\n" +
                "          \"modeStrict\": true,\n" +
                "          \"propagateAuthHeader\": true\n" +
                "        }\n" +
                "      },\n" +
                "      \"oauth2-resource\": {\n" +
                "        \"type\": \"resource\",\n" +
                "        \"identifier\": \"oauth2\",\n" +
                "        \"configuration\": {\n" +
                "          \"authorizationServerUrl\": \"e\",\n" +
                "          \"introspectionEndpoint\": \"e\",\n" +
                "          \"clientId\": \"e\",\n" +
                "          \"clientSecret\": \"e\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"quota-policy\": {\n" +
                "        \"type\": \"policy\",\n" +
                "        \"identifier\": \"quota\",\n" +
                "        \"configuration\": {\n" +
                "          \"async\": false,\n" +
                "          \"addHeaders\": true,\n" +
                "          \"quota\": {\n" +
                "            \"periodTime\": 2,\n" +
                "            \"periodTimeUnit\": \"HOURS\",\n" +
                "            \"limit\": 1000\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"rate-limit\": {\n" +
                "        \"type\": \"policy\",\n" +
                "        \"identifier\": \"rate-limit\",\n" +
                "        \"configuration\": {\n" +
                "          \"async\": false,\n" +
                "          \"addHeaders\": true,\n" +
                "          \"rate\": {\n" +
                "            \"periodTime\": 1,\n" +
                "            \"periodTimeUnit\": \"SECONDS\",\n" +
                "            \"limit\": 10\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        crudServer.expect().get().withPath("/apis/gravitee.io/v1alpha1/gravitee-plugins/internal-gw-plugins").andReturn(HttpURLConnection.HTTP_CREATED, body).once();

        Map<String, Object> objs = crudServer.getClient().customResource(customResourceDefinitionContext).get("internal-gw-plugins");
        System.out.println(objs);

    }

}
