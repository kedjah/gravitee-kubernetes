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
package io.gravitee.kube.controller.vertx;

import io.gravitee.kube.controller.apim.handler.ApimControllerHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ControllerVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(ControllerVerticle.class);

    @Autowired
    @Qualifier("controllerHttpServer")
    private HttpServer httpServer;

    @Autowired
    private VertxHttpServerConfiguration httpServerConfiguration;

    @Autowired
    private Vertx vertx;
/*
    @Value("${http.requestTimeout:0}")
    private long requestTimeout;

    @Value("${http.websocket.enabled:false}")
    private boolean websocketEnabled;

    @Value("${handlers.request.format:uuid}")
    private String requestFormat;*/

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        httpServer.requestHandler(new ApimControllerHandler());

        httpServer.listen(res -> {
            if (res.succeeded()) {
                logger.info("HTTP listener ready to accept requests on port {}",
                        httpServerConfiguration.getPort());
                startFuture.complete();
            } else {
                logger.error("Unable to start HTTP Server", res.cause());
                startFuture.fail(res.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping HTTP Server...");
        httpServer.close(voidAsyncResult -> logger.info("HTTP Server has been correctly stopped"));
    }
}