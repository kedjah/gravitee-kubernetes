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

import io.gravitee.gateway.handlers.api.definition.Api;
import io.gravitee.kube.controller.apim.crds.cache.PluginRevision;
import io.gravitee.kube.controller.apim.crds.resources.GraviteeServices;
import io.gravitee.kube.controller.apim.crds.resources.service.GraviteeService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ServiceWatchActionContext extends WatchActionContext<GraviteeServices> {
    private final GraviteeService subResource;
    private final String serviceName;

    private Api api; // TODO use the right reactable type

    /**
     * use to retrieve easily if the API must be redeploy in case of plugin resource updates
     */
    private List<PluginRevision> pluginRevisions = new ArrayList<>();

    public ServiceWatchActionContext(WatchActionContext<GraviteeServices> origin, GraviteeService subResource, String name) {
        super(origin.getResource(), origin.getEvent());
        this.subResource = subResource;
        this.serviceName = name;
    }

    public String getServiceName() {
        return serviceName;
    }

    public GraviteeService getSubResource() {
        return subResource;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public void addPluginRevision(PluginRevision revision) {
        this.pluginRevisions.add(revision);
    }

    // -- utils methods to regroup in GSUtils class ?
    public String buildApiId() {
        return serviceName + "." + getResource().getMetadata().getName() + "." + getResource().getMetadata().getNamespace();
    }
}
