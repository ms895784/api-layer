/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.cloudgatewayservice.config;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.transport.jersey.EurekaJerseyClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.Ssl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ComponentScan(basePackages = "org.zowe.apiml.cloudgatewayservice")
class ConnectionsConfigTest {

    @Autowired
    private ConnectionsConfig connectionsConfig;
    @Autowired
    private RoutingConfig routingConfig;

    @Nested
    class WhenCreateEurekaJerseyClientBuilder {
        @Test
        void thenIsNotNull() {
            assertThat(connectionsConfig).isNotNull();
            assertThat(connectionsConfig.getEurekaJerseyClient()).isNotNull();
        }
    }

    @Nested
    class WhenInitializeEurekaClient {
        @Mock
        private ApplicationInfoManager manager;

        @Mock
        private EurekaClientConfig config;

        @Mock
        private EurekaJerseyClient eurekaJerseyClient;

        @Mock
        private HealthCheckHandler healthCheckHandler;

        @Test
        void thenCreateIt() {
            assertThat(connectionsConfig.primaryEurekaClient(manager, config, eurekaJerseyClient, healthCheckHandler)).isNotNull();
        }
    }

    @Nested
    class KeyringFormatAndPasswordUpdate {

        ApplicationContext context;

        @BeforeEach
        void setup() {
            context = mock(ApplicationContext.class);
            ServerProperties properties = new ServerProperties();
            properties.setSsl(new Ssl());
            when(context.getBean(ServerProperties.class)).thenReturn(properties);
        }

        @Test
        void whenKeyringHasWrongFormatAndMissingPasswords_thenFixIt() {
            ConnectionsConfig connectionsConfig = new ConnectionsConfig(null);
            ReflectionTestUtils.setField(connectionsConfig, "keyStorePath", "safkeyring:///userId/ringId1");
            ReflectionTestUtils.setField(connectionsConfig, "trustStorePath", "safkeyring:////userId/ringId2");
            ReflectionTestUtils.setField(connectionsConfig, "context", context);
            connectionsConfig.updateConfigParameters();

            assertThat(ReflectionTestUtils.getField(connectionsConfig, "keyStorePath")).isEqualTo("safkeyring://userId/ringId1");
            assertThat(ReflectionTestUtils.getField(connectionsConfig, "trustStorePath")).isEqualTo("safkeyring://userId/ringId2");
            assertThat((char[]) ReflectionTestUtils.getField(connectionsConfig, "keyStorePassword")).isEqualTo("password".toCharArray());
            assertThat((char[]) ReflectionTestUtils.getField(connectionsConfig, "trustStorePassword")).isEqualTo("password".toCharArray());
        }

        @Test
        void whenKeystore_thenDoNothing() {
            ConnectionsConfig connectionsConfig = new ConnectionsConfig(null);
            ReflectionTestUtils.setField(connectionsConfig, "keyStorePath", "/path1");
            ReflectionTestUtils.setField(connectionsConfig, "trustStorePath", "/path2");
            ReflectionTestUtils.setField(connectionsConfig, "context", context);
            connectionsConfig.updateConfigParameters();

            assertThat(ReflectionTestUtils.getField(connectionsConfig, "keyStorePath")).isEqualTo("/path1");
            assertThat(ReflectionTestUtils.getField(connectionsConfig, "trustStorePath")).isEqualTo("/path2");
            assertThat(ReflectionTestUtils.getField(connectionsConfig, "keyStorePassword")).isNull();
            assertThat(ReflectionTestUtils.getField(connectionsConfig, "trustStorePassword")).isNull();
        }
    }
}

