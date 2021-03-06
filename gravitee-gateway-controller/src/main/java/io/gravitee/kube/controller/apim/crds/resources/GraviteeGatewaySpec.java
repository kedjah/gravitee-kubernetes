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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class GraviteeGatewaySpec {
    private Map<String, String> selectors = new HashMap<>();
    private List<GraviteePluginReference> policies = new ArrayList<>();
    private List<GraviteePluginReference> resources = new ArrayList<>();
    private GraviteePluginReference security;
    private Map<String, Object> defaultBackendConfigurations = new HashMap<>(); // TODO

    public GraviteeGatewaySpec() {
    }

    public List<GraviteePluginReference> getPolicies() {
        return policies;
    }

    public void setPolicies(List<GraviteePluginReference> policies) {
        this.policies = policies;
    }

    public List<GraviteePluginReference> getResources() {
        return resources;
    }

    public void setResources(List<GraviteePluginReference> resources) {
        this.resources = resources;
    }

    public GraviteePluginReference getSecurity() {
        return security;
    }

    public void setSecurity(GraviteePluginReference security) {
        this.security = security;
    }

    public Map<String, String> getSelectors() {
        return selectors;
    }

    public void setSelectors(Map<String, String> selectors) {
        this.selectors = selectors;
    }

    public Map<String, Object> getDefaultBackendConfigurations() {
        return defaultBackendConfigurations;
    }

    public void setDefaultBackendConfigurations(Map<String, Object> defaultBackendConfigurations) {
        this.defaultBackendConfigurations = defaultBackendConfigurations;
    }

    @Override
    public String toString() {
        return "GraviteeGatewaySpec{" +
                "policies=" + policies +
                ", resources=" + resources +
                ", security=" + security +
                '}';
    }
}
