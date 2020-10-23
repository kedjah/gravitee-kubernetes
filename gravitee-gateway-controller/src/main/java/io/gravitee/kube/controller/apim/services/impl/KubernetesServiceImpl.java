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
package io.gravitee.kube.controller.apim.services.impl;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.gravitee.kube.controller.apim.exceptions.PipelineException;
import io.gravitee.kube.controller.apim.exceptions.SecretNotFoundException;
import io.gravitee.kube.controller.apim.services.KubernetesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class KubernetesServiceImpl implements KubernetesService  {
    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesServiceImpl.class);

    protected KubernetesClient client;

    public KubernetesServiceImpl(@Autowired KubernetesClient client) {
        this.client = client;
    }

    @Override
    public String resolveSecret(String namespace, String secretName, String secretKey) throws SecretNotFoundException {
        LOGGER.debug("resolving secret {}.{} in namespce {}", secretName, secretKey, namespace);

        Secret k8sSecret = client.secrets().inNamespace(namespace).withName(secretName).get();
        if (k8sSecret == null) {
            throw new SecretNotFoundException(String.format("Secret '%s' not found in '%s' namespace.", secretName, namespace));
        }

        if (k8sSecret.getData() == null || !k8sSecret.getData().containsKey(secretKey)) {
            throw new SecretNotFoundException(String.format("Key '%s' not found in secret '%s' (namespace=%s).", secretKey, secretName, namespace));
        }

        String b64Secret = k8sSecret.getData().get(secretKey);
        return new String(Base64.getDecoder().decode(b64Secret));
    }
}
