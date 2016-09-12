/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.msf4j.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.msf4j.DefaultSessionManager;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.MicroserviceRegistry;
import org.wso2.msf4j.SessionManager;
import org.wso2.msf4j.SwaggerService;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * OSGi service component for MicroServicesServer.
 */
@Component(
        name = "org.wso2.msf4j.internal.MicroServicesServerSC",
        immediate = true,
        property = {
                "componentName=wso2-microservices-server"
        }
)
@SuppressWarnings("unused")
public class MicroservicesServerSC implements RequiredCapabilityListener {
    private static final Logger log = LoggerFactory.getLogger(MicroservicesServerSC.class);
    private final Map<String, MicroservicesRegistry> microservicesRegistries = new HashMap<>();

    @Activate
    protected void start(final BundleContext bundleContext) {
        DataHolder.getInstance().setMicroservicesRegistries(microservicesRegistries);
    }

    @Reference(
            name = "microservice",
            service = Microservice.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeService"
    )
    protected void addService(Microservice service, Map properties) {

        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).addService(service);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.addService(service));
        }
    }

    protected void removeService(Microservice service, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).removeService(service);
        }
    }

    @Reference(
            name = "swaggerservice",
            service = SwaggerService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeSwaggerService"
    )
    protected void addSwaggerService(SwaggerService service, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).addService(service);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.addService(service));
        }
    }

    protected void removeSwaggerService(SwaggerService service, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).removeService(service);
        }
    }

    @Reference(
            name = "carbon-transport",
            service = CarbonTransport.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonTransport"
    )
    protected void addCarbonTransport(CarbonTransport carbonTransport) {
        MicroservicesRegistry microservicesRegistry = new MicroservicesRegistry();
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put(MSF4JConstants.CHANNEL_ID, carbonTransport.getId());
        microservicesRegistries.put(carbonTransport.getId(), microservicesRegistry);
        DataHolder.getInstance().getBundleContext()
                  .registerService(MicroserviceRegistry.class, microservicesRegistry, properties);
    }

    protected void removeCarbonTransport(CarbonTransport carbonTransport) {
        microservicesRegistries.remove(carbonTransport.getId());
    }

    @Reference(
            name = "interceptor",
            service = Interceptor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInterceptor"
    )
    protected void addInterceptor(Interceptor interceptor, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).addInterceptor(interceptor);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.addInterceptor(interceptor));
        }
    }

    protected void removeInterceptor(Interceptor interceptor, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).removeInterceptor(interceptor);
        }
    }

    @Reference(
            name = "exception-mapper",
            service = ExceptionMapper.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeExceptionMapper"
    )
    protected void addExceptionMapper(ExceptionMapper exceptionMapper, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).addExceptionMapper(exceptionMapper);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.addExceptionMapper(exceptionMapper));
        }
    }

    protected void removeExceptionMapper(ExceptionMapper exceptionMapper, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).removeExceptionMapper(exceptionMapper);
        }
    }

    @Reference(
            name = "session-manager",
            service = SessionManager.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeSessionManager"
    )
    protected void addSessionManager(SessionManager sessionManager, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        sessionManager.init();
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).setSessionManager(sessionManager);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.setSessionManager(sessionManager));
        }
    }

    protected void removeSessionManager(SessionManager sessionManager, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            sessionManager.stop();
            DefaultSessionManager defaultSessionManager = new DefaultSessionManager();
            defaultSessionManager.init();
            microservicesRegistries.get(channelId.toString()).setSessionManager(defaultSessionManager);
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        DataHolder.getInstance().getBundleContext().registerService(MicroservicesServerSC.class, this, null);
        log.info("All microservices are available");
    }
}
