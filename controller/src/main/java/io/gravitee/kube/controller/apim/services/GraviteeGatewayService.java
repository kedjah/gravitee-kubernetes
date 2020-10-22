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

import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.gravitee.kube.controller.apim.crds.resources.DoneableGraviteeGateway;
import io.gravitee.kube.controller.apim.crds.resources.GraviteeGateway;
import io.gravitee.kube.controller.apim.crds.resources.GraviteeGatewayList;
import io.gravitee.kube.controller.apim.services.impl.WatchActionContext;
import io.gravitee.kube.controller.apim.services.listeners.GraviteeGatewayListener;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface GraviteeGatewayService {

    void registerListener(GraviteeGatewayListener listener);

    List<GraviteeGateway> listAllGateways();

    Flowable processAction(WatchActionContext<GraviteeGateway> context);

    WatchActionContext<GraviteeGateway> persistAsSuccess(WatchActionContext<GraviteeGateway> context);
    WatchActionContext<GraviteeGateway> persistAsError(WatchActionContext<GraviteeGateway> context, String message);

    MixedOperation<GraviteeGateway,
            GraviteeGatewayList,
            DoneableGraviteeGateway,
            Resource<GraviteeGateway, DoneableGraviteeGateway>> getCrdClient();
}