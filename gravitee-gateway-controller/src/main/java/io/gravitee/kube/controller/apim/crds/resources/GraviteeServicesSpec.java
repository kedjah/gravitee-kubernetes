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
package io.gravitee.kube.controller.apim.crds.resources;


import io.gravitee.kube.controller.apim.crds.resources.service.GraviteeService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class GraviteeServicesSpec {

    private boolean enabled = true;

    private GraviteeGatewayReference gateway;

    private Map<String, GraviteeService> services = new HashMap<>();

    public GraviteeServicesSpec() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public GraviteeGatewayReference getGateway() {
        return gateway;
    }

    public void setGateway(GraviteeGatewayReference gateway) {
        this.gateway = gateway;
    }

    public Map<String, GraviteeService> getServices() {
        return services;
    }

    public void setServices(Map<String, GraviteeService> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "GraviteeServicesSpec{" +
                "enabled=" + enabled +
                ", gateway=" + gateway +
                ", services=" + services +
                '}';
    }
}
