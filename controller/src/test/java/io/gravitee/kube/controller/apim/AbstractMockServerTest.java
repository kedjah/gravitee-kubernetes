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
package io.gravitee.kube.controller.apim;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.gravitee.kube.controller.ControllerTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@ContextConfiguration(classes = {ControllerTestConfiguration.class})
public class AbstractMockServerTest {
    protected ObjectMapper jsonMapper = new ObjectMapper();
    protected Yaml yaml = new Yaml();

    @Autowired
    protected KubernetesMockServer kubeServer;

    @Autowired
    protected KubernetesClient kubeClient;

    @Before
    public final void before() {
        try {
            kubeServer.init();
        } catch (IllegalStateException e) {
        }
    }

    @After
    public final void after() {
        kubeServer.destroy();
    }

    protected <T> T readYamlAs(String file, Class<T> type) {
        final InputStream resourceAsStream = getClass().getResourceAsStream(file);
        return yaml.loadAs(resourceAsStream, type);
    }

    protected String yamlToJson(String file) throws Exception {
        final InputStream resourceAsStream = getClass().getResourceAsStream(file);
        return jsonMapper.writeValueAsString(yaml.loadAs(resourceAsStream, Map.class));
    }
}
