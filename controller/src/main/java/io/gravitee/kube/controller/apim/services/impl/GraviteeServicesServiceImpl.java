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

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import io.gravitee.definition.model.*;
import io.gravitee.definition.model.endpoint.HttpEndpoint;
import io.gravitee.gateway.handlers.api.definition.Api;
import io.gravitee.kube.controller.apim.crds.cache.PluginRevision;
import io.gravitee.kube.controller.apim.crds.resources.*;
import io.gravitee.kube.controller.apim.crds.resources.plugin.Plugin;
import io.gravitee.kube.controller.apim.crds.resources.service.BackendConfiguration;
import io.gravitee.kube.controller.apim.crds.resources.service.BackendService;
import io.gravitee.kube.controller.apim.crds.resources.service.ServiceEndpoint;
import io.gravitee.kube.controller.apim.crds.resources.service.ServicePath;
import io.gravitee.kube.controller.apim.services.*;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class GraviteeServicesServiceImpl
        extends AbstractServiceImpl<GraviteeServices, GraviteeServicesList, DoneableGraviteeServices>
        implements GraviteeServicesService, InitializingBean {
    private static Logger LOGGER = LoggerFactory.getLogger(GraviteeServicesServiceImpl.class);

    @Autowired
    private GraviteePluginsService pluginsService;

    @Autowired
    private GraviteeGatewayService gatewayService;

    private void initializeGraviteeServicesClient(KubernetesClient client) {
        LOGGER.debug("Creating CRD Client for 'gravitee-services'");

        CustomResourceDefinitionContext context = new CustomResourceDefinitionContext.Builder()
                .withGroup("gravitee.io")
                .withVersion("v1alpha1")
                .withScope("Namespaced")
                .withName("gravitee-services.gravitee.io")
                .withPlural("gravitee-services")
                .withKind("GraviteeServices")
                .build();

        this.crdClient = client.customResources(context,
                GraviteeServices.class,
                GraviteeServicesList.class,
                DoneableGraviteeServices.class);

        KubernetesDeserializer.registerCustomKind("gravitee.io/v1alpha1", "GraviteeServices", GraviteeServices.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeGraviteeServicesClient(client);
    }

    public List<GraviteeServices> listAllServices() {
        GraviteeServicesList list = crdClient.list();
        return list.getItems();
    }

    @Override
    public Flowable processAction(WatchActionContext<GraviteeServices> context) {
        Flowable pipeline = null;
        switch (context.getEvent()) {
            case ADDED:
                pipeline = Flowable.fromStream(split(context))
                        .map(this::lookupGatewayRef)
                        .map(this::prepareApi)
                        .map(this::buildApiPaths)
                        .map(this::buildApiProxy)
                        .map(this::applySecurityPlugin)
                        .map(this::addService)
                        .map(this::preserveApiData);
                break;
            case MODIFIED:
                pipeline = Flowable.fromStream(split(context))
                        .map(this::lookupGatewayRef)
                        .map(this::prepareApi)
                        .map(this::buildApiPaths)
                        .map(this::buildApiProxy)
                        .map(this::applySecurityPlugin)
                        .map(this::updateService)
                        .map(this::preserveApiData);
                break;
            case REFERENCE_UPDATED:
                pipeline = Flowable.fromStream(split(context))
                        .map(this::lookupGatewayRef)
                        .map(this::prepareApi)
                        .map(this::buildApiPaths)
                        .map(this::buildApiProxy)
                        .map(this::applySecurityPlugin)
                        .map(this::updateService)
                        .map(this::preserveApiData);
                break;
            case DELETED:
                pipeline = Flowable.fromStream(split(context))
                        .map(this::prepareApi)
                        .map(this::deleteService)
                        .map(this::cleanApiData);
                break;
            default:
                pipeline = Flowable.just(context);
        }
        return pipeline;
    }

    private Stream<ServiceWatchActionContext> split(WatchActionContext<GraviteeServices> action) {
        if (action.getResource() != null && action.getResource().getSpec().getServices() != null) {
            return action.getResource().getSpec()
                    .getServices().entrySet().stream()
                    .map(entry -> new ServiceWatchActionContext(action, entry.getValue(), entry.getKey()));
        } else {
            return Stream.empty();
        }
    }

    public ServiceWatchActionContext preserveApiData(ServiceWatchActionContext context) {
        // TODO add to cache
        return context;
    }

    public ServiceWatchActionContext cleanApiData(ServiceWatchActionContext context) {
        // TODO remove from cache
        return context;
    }

    public ServiceWatchActionContext addService(ServiceWatchActionContext context) {
        if (context.getApi().isEnabled()) {
            LOGGER.info("Deploy Api '{}'", context.getApi().getId());
            // apiManager.deploy(api);
        } else {
            LOGGER.debug("Ignore disabled Api '{}'", context.getApi().getId());
        }
        return context;
    }

    public ServiceWatchActionContext updateService(ServiceWatchActionContext context) {
        boolean wasPresentAndEnabled = false;
        if (!context.getApi().isEnabled()) {
            if (wasPresentAndEnabled) {
                LOGGER.info("Undeploy Api '{}'", context.getApi().getId());
                // apiManager.undeploy(api);
            } else {
                LOGGER.debug("Ignore disabled Api '{}'", context.getApi().getId());
            }
        } else {
            LOGGER.info("Deploy Api '{}'", context.getApi().getId());
            // apiManager.deploy(api);
        }
        return context;
    }

    public ServiceWatchActionContext deleteService(ServiceWatchActionContext context) {
        LOGGER.info("Undeploy api '{}'", context.getApi().getId());
        //apiManager.undeploy(apiId);
        return context;
    }

    public ServiceWatchActionContext lookupGatewayRef(ServiceWatchActionContext context) {
        // TODO
        return context;
    }

    private ServiceWatchActionContext prepareApi(ServiceWatchActionContext context) {
        Api api = new Api();
        api.setName(context.getServiceName());
        api.setId(context.buildApiId());
        api.setEnabled(context.getSubResource().isEnabled() && context.getResource().getSpec().isEnabled());
        api.setPlanRequired(false); // TODO maybe useless for the right reactable type
        context.setApi(api);
        return context;
    }

    private ServiceWatchActionContext buildApiPaths(ServiceWatchActionContext context) {
        Api api = context.getApi();

        List<ServicePath> svcPaths = context.getSubResource().getPaths();
        Map<String, Path> apiPaths = svcPaths.stream().map(svcPath -> {
            Path path = new Path();
            path.setPath(svcPath.getPrefix());

            path.setRules(svcPath.getRules().stream().map(r -> {
                Rule rule = new Rule();
                rule.setMethods(r.getMethods());
                final Plugin policy = r.getPolicy();

                if (policy != null && policy.getType() == null) {
                    // policy declared in path rule use the policy type per default
                    policy.setType("policy");
                }

                PluginRevision<Policy> optPolicy = pluginsService.buildPolicy(context, policy, r.getPolicyRef());

                if (optPolicy.isRef()) {
                    // policy comes from reference, keep its version in memory
                    context.addPluginRevision(optPolicy);
                }

                if (optPolicy.isValid()) {
                    rule.setPolicy(optPolicy.getPlugin());
                    return rule;
                } else {
                    LOGGER.error("Policy Plugin not found for Path {} in API {}", svcPath, api.getId());
                    return null;
                }

            }).filter(Objects::nonNull).collect(Collectors.toList()));
            return path;
        }).collect(Collectors.toMap(Path::getPath, Function.identity()));

        api.setPaths(apiPaths);
        return context;
    }

    private ServiceWatchActionContext buildApiProxy(ServiceWatchActionContext context) {
        Api api = context.getApi();
        Proxy proxy = new Proxy();
        proxy.setVirtualHosts(context.getSubResource().getVhosts()
                .stream()
                .filter(v -> {
                    LOGGER.info("VirtualHost({},{}) = {}", v.getHost(), v.getPath(), v.isEnabled());
                    return v.isEnabled();
                })
                .map(v -> {
                    LOGGER.info("VirtualHost({},{})", v.getHost(), v.getPath());
                    return new VirtualHost(v.getHost(), v.getPath());
                }).collect(Collectors.toList()));
        proxy.setGroups(buildEndpoints(context.getSubResource().getEndpoints()));
        proxy.setCors(context.getSubResource().getCors());
        // TODO preserve host
        // TODO stripContextPath
        proxy.setPreserveHost(true);
        api.setProxy(proxy);
        return context;
    }

    private Set<EndpointGroup> buildEndpoints(Map<String, ServiceEndpoint> endpoints) {
        return endpoints.entrySet().stream().map(entry -> {
            // TODO manage all types (gRPC, HTTP...)
            BackendConfiguration backEndConfig = entry.getValue().getConfiguration();
            EndpointGroup endpointGroup = new EndpointGroup();
            endpointGroup.setName(entry.getKey());
            // TODO manage secrets
            boolean useHttps = endpointGroup.getHttpClientSslOptions() != null;
            Set<Endpoint> targetEndpoints = new HashSet<>();
            for(BackendService backendSvcRef : entry.getValue().getBackendServices()) {
                String target = (useHttps ? "https://" : "http://") + backendSvcRef.getName() + ":" + backendSvcRef.getPort();
                HttpEndpoint httpEndpoint = new HttpEndpoint(backendSvcRef.getName(), target);
                httpEndpoint.setName(backendSvcRef.getName());
                targetEndpoints.add(httpEndpoint);
            }
            // TODO call BackendPolicy to retrieve http configuration or set default one.
            endpointGroup.setEndpoints(targetEndpoints);
            return endpointGroup;
        }).collect(Collectors.toSet());
    }

    private ServiceWatchActionContext applySecurityPlugin(ServiceWatchActionContext context) {
        Api api = context.getApi();
        if (context.getSubResource().getSecurity() != null) {
            PluginRevision<Policy> securityPolicy = pluginsService.buildSecurityPolicy(context, context.getSubResource().getSecurity());
            if (securityPolicy.isValid()) {
                final Policy plugin = securityPolicy.getPlugin();
                LOGGER.info("Api '{}' secured by '{}' policy", plugin.getName());
                if ("key_less".equalsIgnoreCase(plugin.getName())) {
                    api.setSecurity("key_less");
                } else if ("jwt".equalsIgnoreCase(plugin.getName())) {
                    api.setSecurity("JWT");
                    api.setSecurityDefinition(plugin.getConfiguration());
                } // TODO other
            }
        } else {
            // TODO if gw not null && gw security not null apply otherwise error;
        }
        return context;
    }

}
