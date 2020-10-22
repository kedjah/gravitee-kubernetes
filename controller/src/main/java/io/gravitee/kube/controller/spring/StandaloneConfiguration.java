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
package io.gravitee.kube.controller.spring;

import io.gravitee.common.event.EventManager;
import io.gravitee.common.event.impl.EventManagerImpl;
import io.gravitee.gateway.handlers.api.spring.ApiHandlerConfiguration;
import io.gravitee.gateway.report.spring.ReporterConfiguration;
import io.gravitee.kube.controller.node.ControllerNode;
import io.gravitee.kube.controller.vertx.VertxControllerConfiguration;
import io.gravitee.node.container.NodeFactory;
import io.gravitee.node.vertx.spring.VertxConfiguration;
import io.gravitee.plugin.alert.spring.AlertPluginConfiguration;
import io.gravitee.plugin.core.spring.PluginConfiguration;
import io.gravitee.plugin.policy.spring.PolicyPluginConfiguration;
import io.gravitee.plugin.resource.spring.ResourcePluginConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
@Import({
        VertxConfiguration.class,
        VertxControllerConfiguration.class,
        Fabric8ClientConfiguration.class,


        PluginConfiguration.class,
        PolicyPluginConfiguration.class,
        ResourcePluginConfiguration.class,
        ReporterConfiguration.class,
        ApiHandlerConfiguration.class,
        //DictionaryConfiguration.class,
        AlertPluginConfiguration.class,
        //ServiceDiscoveryPluginConfiguration.class
})
@ComponentScan("io.gravitee.kube.controller.apim")
public class StandaloneConfiguration {

    @Bean
    public EventManager eventManager() {
        return new EventManagerImpl();
    }

    @Bean
    public NodeFactory node() {
        return new NodeFactory(ControllerNode.class);
    }
}
