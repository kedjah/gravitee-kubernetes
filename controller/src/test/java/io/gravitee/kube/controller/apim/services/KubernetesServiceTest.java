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
package io.gravitee.kube.controller.apim.services;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.gravitee.kube.controller.apim.exceptions.KubeAuthorizationException;
import io.gravitee.kube.controller.apim.exceptions.PipelineException;
import io.gravitee.kube.controller.apim.exceptions.SecretNotFoundException;
import io.gravitee.kube.controller.apim.services.impl.KubernetesServiceImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class KubernetesServiceTest {
    public static final String NAMESPACE = "default";

    @Rule
    public KubernetesServer k8sServer = new KubernetesServer(true, true);

    private NamespacedKubernetesClient client;

    private KubernetesService kubernetesService;

    @Before
    public void before() {
        this.client = k8sServer.getClient();
        this.kubernetesService = new KubernetesServiceImpl(client);
    }

    @Test
    public void shouldReturnOpaqueSecret() throws Exception {
        populateSecret(NAMESPACE, "myapp", "/kubernetes/test-secret-opaque.yml");

        String password = kubernetesService.resolveSecret(NAMESPACE, "myapp", "myapp-password");
        assertNotNull(password);
        assertEquals("DA7OLkdACP", password);
    }

    @Test(expected = SecretNotFoundException.class)
    public void shouldThrowError_UnknownSecretName() throws Exception {
        populateSecret(NAMESPACE, "myapp", "/kubernetes/test-secret-opaque.yml");
        kubernetesService.resolveSecret(NAMESPACE, "unknown", "myapp-password");
    }

    @Test(expected = SecretNotFoundException.class)
    public void shouldThrowError_UnknownSecretKey() throws Exception {
        populateSecret(NAMESPACE, "myapp", "/kubernetes/test-secret-opaque.yml");
        kubernetesService.resolveSecret(NAMESPACE, "myapp", "unknown-password");
    }

    private void populateSecret(String ns, String name, String filename) {
        Secret toCreate = client.secrets().load(getClass().getResourceAsStream(filename)).get();
        client.secrets().inNamespace(ns).withName(name).create(toCreate);
    }
}
