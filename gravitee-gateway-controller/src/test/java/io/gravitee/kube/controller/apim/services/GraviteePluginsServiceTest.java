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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.Watcher;
import io.gravitee.definition.model.Policy;
import io.gravitee.kube.controller.apim.AbstractMockServerTest;
import io.gravitee.kube.controller.apim.crds.cache.PluginRevision;
import io.gravitee.kube.controller.apim.crds.resources.GraviteePluginReference;
import io.gravitee.kube.controller.apim.crds.resources.GraviteeServices;
import io.gravitee.kube.controller.apim.crds.resources.plugin.Plugin;
import io.gravitee.kube.controller.apim.services.impl.WatchActionContext;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.HttpURLConnection;

import static org.junit.Assert.*;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(SpringRunner.class)
public class GraviteePluginsServiceTest extends AbstractMockServerTest {

    @Autowired
    public GraviteePluginsService service;

    @Test
    public void shouldReplaceSecretInPlugin() throws Exception {
        populateSecret("default", "myapp", "/kubernetes/test-secret-opaque.yml");

        final WatchActionContext context = new WatchActionContext(mockCustomResource(), WatchActionContext.Event.ADDED);
        PluginRevision<Policy> policy = service.buildPolicy(context, readYamlAs("/slice/slice-plugin-secret.yaml", Plugin.class), null);

        assertNotNull("buildPolicy should return non null value", policy);
        assertTrue("Policy should be present", policy.isValid());
        assertFalse("Policy should be not marked as ref", policy.isRef());
        assertTrue("resolverParameter should be replace by password",
                policy.getPlugin().getConfiguration().contains("\"resolverParameter\":\"DA7OLkdACP\""));
    }

    @Test
    @Ignore
    public void shouldReplaceSecretInPluginRef() throws Exception {
        populateSecret("default", "myapp", "/kubernetes/test-secret-opaque.yml");
        populateCustomResource();

        final WatchActionContext context = new WatchActionContext(mockCustomResource(), WatchActionContext.Event.ADDED);
        final GraviteePluginReference pluginRef = new GraviteePluginReference();
        pluginRef.setName("jwt-poc");
        pluginRef.setNamespace("default");
        pluginRef.setResource("myapp-plugins");

        PluginRevision<Policy> policy = service.buildPolicy(context, null, pluginRef);

        assertNotNull("buildPolicy should return non null value", policy);
        assertTrue("Policy should be present", policy.isValid());
        assertTrue("Policy should be marked as red", policy.isRef());
        assertTrue("resolverParameter should be replace by password",
                policy.getPlugin().getConfiguration().contains("\"resolverParameter\":\"DA7OLkdACP\""));
    }

    // TODO Test buildPolicy by Ref missing Ref
    // TODO Test buildSecurity
    // TODO Test buildSecurity by Ref missing Ref

    private GraviteeServices mockCustomResource() {
        GraviteeServices resource = new GraviteeServices();
        final ObjectMeta metadata = new ObjectMeta();
        metadata.setNamespace("default");
        resource.setMetadata(metadata);
        return resource;
    }

    private void populateSecret(String ns, String name, String filename) {
        Secret toCreate = kubeClient.secrets().load(getClass().getResourceAsStream(filename)).get();
        kubeClient.secrets().inNamespace(ns).withName(name).create(toCreate);
    }


    private void populateCustomResource() throws Exception {
        kubeServer.expect().get()
                .withPath("/apis/gravitee.io/v1alpha1/namespaces/default/gravitee-plugins/myapp-plugins")
                .andReturn(HttpURLConnection.HTTP_OK, yamlToJson("/kubernetes/test-gravitee-plugin.yml"))
                .always();

    }


}
