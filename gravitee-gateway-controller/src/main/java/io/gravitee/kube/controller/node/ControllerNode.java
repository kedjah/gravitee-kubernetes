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
package io.gravitee.kube.controller.node;

import io.gravitee.common.component.LifecycleComponent;
import io.gravitee.kube.controller.ControllerConstants;
import io.gravitee.kube.controller.apim.ApimControllerComponent;
import io.gravitee.kube.controller.apim.managers.GraviteeGatewayManager;
import io.gravitee.kube.controller.apim.managers.GraviteePluginsManager;
import io.gravitee.kube.controller.apim.managers.GraviteeServicesManager;
import io.gravitee.kube.controller.vertx.VertxEmbeddedContainer;
import io.gravitee.node.container.AbstractNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ControllerNode extends AbstractNode {

    @Override
    public String name() {
        return ControllerConstants.SERVICE_NAME;
    }

    @Override
    public String application() {
        return ControllerConstants.APPLICATION;
    }

    @Override
    public Map<String, Object> metadata() {
        Map<String, Object> metadata = new HashMap<>();

        return metadata;
    }

    @Override
    public List<Class<? extends LifecycleComponent>> components() {
        final List<Class<? extends LifecycleComponent>> components = new ArrayList<>();

        components.add(VertxEmbeddedContainer.class);
        components.add(ApimControllerComponent.class);
        components.add(GraviteeServicesManager.class);
        components.add(GraviteePluginsManager.class);
        components.add(GraviteeGatewayManager.class);

        //components.addAll(super.components());

        return components;
    }
}
