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

import com.netflix.discovery.EurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zowe.apiml.services.BasicInfoService;
import org.zowe.apiml.eurekaservice.client.util.EurekaMetadataParser;

@Configuration
public class RegistryConfig {

    @Bean
    public EurekaMetadataParser eurekaMetadataParser() {
        return new EurekaMetadataParser();
    }

    @Bean
    public BasicInfoService basicInfoService(EurekaClient eurekaClient, EurekaMetadataParser eurekaMetadataParser) {
        return new BasicInfoService(eurekaClient, eurekaMetadataParser);
    }
}
