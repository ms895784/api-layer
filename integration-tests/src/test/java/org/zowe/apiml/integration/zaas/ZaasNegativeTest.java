/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.integration.zaas;

import io.jsonwebtoken.Jwts;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.zowe.apiml.security.common.token.QueryResponse;
import org.zowe.apiml.ticket.TicketRequest;
import org.zowe.apiml.util.SecurityUtils;
import org.zowe.apiml.util.categories.ZaasTest;
import org.zowe.apiml.util.config.ConfigReader;
import org.zowe.apiml.util.config.SafIdtConfiguration;
import org.zowe.apiml.util.config.SslContext;
import org.zowe.apiml.util.config.SslContextConfigurer;
import org.zowe.apiml.util.config.TlsConfiguration;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zowe.apiml.integration.zaas.ZaasTestUtil.COOKIE;
import static org.zowe.apiml.integration.zaas.ZaasTestUtil.ZAAS_SAFIDT_URI;
import static org.zowe.apiml.integration.zaas.ZaasTestUtil.ZAAS_TICKET_URI;
import static org.zowe.apiml.integration.zaas.ZaasTestUtil.ZAAS_ZOSMF_URI;
import static org.zowe.apiml.integration.zaas.ZaasTestUtil.ZAAS_ZOWE_URI;
import static org.zowe.apiml.util.SecurityUtils.generateJwtWithRandomSignature;
import static org.zowe.apiml.util.SecurityUtils.getConfiguredSslConfig;
import static org.zowe.apiml.util.SecurityUtils.getDummyClientCertificate;

@ZaasTest
public class ZaasNegativeTest {

    private final static String APPLICATION_NAME = ConfigReader.environmentConfiguration().getDiscoverableClientConfiguration().getApplId();
    private final static SafIdtConfiguration SAFIDT_CONF = ConfigReader.environmentConfiguration().getSafIdtConfiguration();

    private final static String CLIENT_USER = ConfigReader.environmentConfiguration().getCredentials().getClientUser();

    private static final Set<URI> tokenEndpoints = new HashSet<URI>() {{
        add(ZAAS_ZOWE_URI);
        add(ZAAS_ZOSMF_URI);
        if (SAFIDT_CONF.isEnabled()) {
            add(ZAAS_SAFIDT_URI);
        }
    }};

    private static final Set<URI> endpoints = new HashSet<URI>() {{
        add(ZAAS_TICKET_URI);
        addAll(tokenEndpoints);
    }};

    private static final Set<String> tokens = new HashSet<String>() {{
        add(generateJwtWithRandomSignature(QueryResponse.Source.ZOSMF.value));
        add(generateJwtWithRandomSignature(QueryResponse.Source.ZOWE.value));
        add(generateJwtWithRandomSignature(QueryResponse.Source.ZOWE_PAT.value));
        add(generateJwtWithRandomSignature("https://localhost:10010"));
    }};

    static Stream<Arguments> provideZaasEndpointsWithAllTokens() {
        List<Arguments> argumentsList = new ArrayList<>();
        for (URI uri : endpoints) {
            RequestSpecification requestSpec = given();
            if (ZAAS_SAFIDT_URI.equals(uri) || ZAAS_TICKET_URI.equals(uri)) {
                requestSpec.contentType(ContentType.JSON).body(new TicketRequest(APPLICATION_NAME));
            }
            for (String token : tokens) {
                argumentsList.add(Arguments.of(uri, requestSpec, token));
            }
        }

        return argumentsList.stream();
    }

    static Stream<Arguments> provideZaasEndpoints() {
        List<Arguments> argumentsList = new ArrayList<>();
        for (URI uri : endpoints) {
            RequestSpecification requestSpec = given();
            if (ZAAS_SAFIDT_URI.equals(uri) || ZAAS_TICKET_URI.equals(uri)) {
                requestSpec.contentType(ContentType.JSON).body(new TicketRequest(APPLICATION_NAME));
            }
            argumentsList.add(Arguments.of(uri, requestSpec));
        }
        return argumentsList.stream();
    }

    static Stream<Arguments> provideZaasTokenEndpoints() {
        List<Arguments> argumentsList = new ArrayList<>();
        for (URI uri : tokenEndpoints) {
            RequestSpecification requestSpec = given();
            if (ZAAS_SAFIDT_URI.equals(uri)) {
                requestSpec.contentType(ContentType.JSON).body(new TicketRequest(APPLICATION_NAME));
            }
            argumentsList.add(Arguments.of(uri, requestSpec));
        }
        return argumentsList.stream();
    }

    @Nested
    class ReturnUnauthorized {

        @BeforeEach
        void setUpCertificateAndToken() {
            RestAssured.config = RestAssured.config().sslConfig(getConfiguredSslConfig());
        }

        @ParameterizedTest
        @MethodSource("org.zowe.apiml.integration.zaas.ZaasNegativeTest#provideZaasEndpoints")
        void givenNoToken(URI uri, RequestSpecification requestSpecification) {
            //@formatter:off
            requestSpecification
            .when()
                .post(uri)
            .then()
                .statusCode(SC_UNAUTHORIZED);
            //@formatter:on
        }

        @ParameterizedTest
        @MethodSource("org.zowe.apiml.integration.zaas.ZaasNegativeTest#provideZaasEndpointsWithAllTokens")
        void givenInvalidToken(URI uri, RequestSpecification requestSpecification, String token) {
            //@formatter:off
            requestSpecification
                .header("Authorization", "Bearer " + token)
            .when()
                .post(uri)
            .then()
                .statusCode(SC_UNAUTHORIZED);
            //@formatter:on
        }

        @ParameterizedTest
        @MethodSource("org.zowe.apiml.integration.zaas.ZaasNegativeTest#provideZaasEndpoints")
        void givenOKTATokenWithNoMapping(URI uri, RequestSpecification requestSpecification) {
            String oktaTokenNoMapping = SecurityUtils.validOktaAccessToken(false);
            //@formatter:off
            requestSpecification
                .header("Authorization", "Bearer " + oktaTokenNoMapping)
            .when()
                .post(uri)
            .then()
                .statusCode(SC_UNAUTHORIZED);
            //@formatter:on
        }
    }

    @Nested
    class GivenBadCertificate {

        @Value("${server.ssl.keyPassword}")
        char[] password;
        @Value("${server.ssl.keyStore}")
        String client_cert_keystore;
        @Value("${server.ssl.keyStore}")
        String keystore;

        @ParameterizedTest
        @MethodSource("org.zowe.apiml.integration.zaas.ZaasNegativeTest#provideZaasEndpoints")
        void givenNoCertificate_thenReturnUnauthorized(URI uri, RequestSpecification requestSpecification) {
            //@formatter:off
            requestSpecification
                .relaxedHTTPSValidation()
                .cookie(COOKIE, SecurityUtils.gatewayToken())
            .when()
                .post(uri)
            .then()
                .statusCode(SC_UNAUTHORIZED);
            //@formatter:on
        }

        @ParameterizedTest
        @MethodSource("org.zowe.apiml.integration.zaas.ZaasNegativeTest#provideZaasTokenEndpoints")
        void givenClientAndHeaderCertificates_thenReturnTokenFromClientCert(URI uri, RequestSpecification requestSpecification) throws Exception {
            TlsConfiguration tlsCfg = ConfigReader.environmentConfiguration().getTlsConfiguration();
            SslContextConfigurer sslContextConfigurer = new SslContextConfigurer(tlsCfg.getKeyStorePassword(), tlsCfg.getClientKeystore(), tlsCfg.getKeyStore());
            SslContext.prepareSslAuthentication(sslContextConfigurer);

            //@formatter:off
            String token = requestSpecification
                .config(SslContext.clientCertValid)
                .header("Client-Cert", getDummyClientCertificate())
            .when()
                .post(uri)
            .then()
                .statusCode(SC_OK)
            .extract()
                .jsonPath().getString("token");
            //@formatter:on

            String userId = Jwts.parserBuilder().build()
                .parseClaimsJwt(token.substring(0, token.lastIndexOf('.') + 1))
                .getBody()
                .getSubject();

            assertEquals(CLIENT_USER, userId);
        }
    }
}
