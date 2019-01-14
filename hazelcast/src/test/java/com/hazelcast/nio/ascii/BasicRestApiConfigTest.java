/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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
package com.hazelcast.nio.ascii;

import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigurationException;
import com.hazelcast.config.RestApiConfig;
import com.hazelcast.config.RestEndpointGroup;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.hazelcast.config.RestEndpointGroup.DATA;
import static com.hazelcast.config.RestEndpointGroup.HEALTH_CHECK;
import static com.hazelcast.config.RestEndpointGroup.MEMCACHE;
import static com.hazelcast.nio.ascii.AbstractRestApiConfigTestBase.TEST_URLS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests enabling text protocols by {@link RestApiConfig} and legacy system properties.
 */
@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class BasicRestApiConfigTest extends AbstractRestApiConfigTestBase {

    private static final TestUrl TEST_URL_MEMCACHE = new TestUrl(MEMCACHE, "version", "foo", "VERSION Hazelcast");
    private static final TestUrl TEST_URL_HEALTH_CHECK = new TestUrl(HEALTH_CHECK, GET, "/hazelcast/health/node-state", "ACTIVE");
    private static final TestUrl TEST_URL_DATA = new TestUrl(DATA, GET, "/hazelcast/rest/maps/test/testKey", "testValue");

    /**
     * <pre>
     * Given: -
     * When: empty RestApiConfig object is created
     * Then: it's disabled and the only enabled REST endpoint group is the CLUSTER_READ
     * </pre>
     */
    @Test
    public void testRestApiDefaults() throws Exception {
        RestApiConfig restApiConfig = new RestApiConfig();
        assertFalse("REST should be disabled by default", restApiConfig.isEnabled());
        for (RestEndpointGroup endpointGroup : RestEndpointGroup.values()) {
            if (endpointGroup == RestEndpointGroup.CLUSTER_READ) {
                assertTrue("Cluster Read REST endpoint group should be enabled by default",
                        restApiConfig.isGroupEnabled(endpointGroup));
            } else {
                assertFalse(
                        "Only Cluster Read REST endpoint group should be enabled by default. Found enabled: " + endpointGroup,
                        restApiConfig.isGroupEnabled(endpointGroup));
            }
        }
    }

    /**
     * <pre>
     * Given: -
     * When: empty RestApiConfig object is created
     * Then: access to all REST endpoints is denied
     * </pre>
     */
    @Test
    public void testRestApiCallWithDefaults() throws Exception {
        Config config = new Config();
        config.setRestApiConfig(new RestApiConfig());
        HazelcastInstance hz = factory.newHazelcastInstance(config);
        for (TestUrl testUrl : TEST_URLS) {
            assertNoTextProtocolResponse(hz, testUrl);
        }
    }

    /**
     * <pre>
     * Given: RestApiConfig is explicitly enabled
     * When: REST endpoint is accessed
     * Then: it is permitted/denied based on its default groups values
     * </pre>
     */
    @Test
    public void testEnabledRestApiCallWithGroupDefaults() throws Exception {
        Config config = new Config();
        config.setRestApiConfig(new RestApiConfig().setEnabled(true));
        HazelcastInstance hz = factory.newHazelcastInstance(config);
        for (TestUrl testUrl : TEST_URLS) {
            if (testUrl.restEndpointGroup == RestEndpointGroup.CLUSTER_READ) {
                assertTextProtocolResponse(hz, testUrl);
            } else {
                assertNoTextProtocolResponse(hz, testUrl);
            }
        }
    }

    /**
     * <pre>
     * Given: RestApiConfig is explicitly enabled and all groups are explicitly enabled
     * When: REST endpoint is accessed
     * Then: access is permitted
     * </pre>
     */
    @Test
    public void testRestApiCallEnabledGroupsEnabled() throws Exception {
        Config config = new Config();
        config.setRestApiConfig(new RestApiConfig().setEnabled(true).enableAllGroups());
        HazelcastInstance hz = factory.newHazelcastInstance(config);
        for (TestUrl testUrl : TEST_URLS) {
            assertTextProtocolResponse(hz, testUrl);
        }
    }

    /**
     * <pre>
     * Given: RestApiConfig is explicitly disabled and all groups are explicitly enabled
     * When: REST endpoint is accessed
     * Then: access is denied
     * </pre>
     */
    @Test
    public void testRestApiCallDisabledGroupsEnabled() throws Exception {
        Config config = new Config();
        config.setRestApiConfig(new RestApiConfig().setEnabled(false).enableAllGroups());
        HazelcastInstance hz = factory.newHazelcastInstance(config);
        for (TestUrl testUrl : TEST_URLS) {
            assertNoTextProtocolResponse(hz, testUrl);
        }
    }

    @Test
    public void testRestConfigWithRestProperty() throws Exception {
        restEnabled.setOrClearProperty("true");
        createMemberWithRestConfigAndAssertConfigException();
    }

    @Test
    public void testRestConfigWithHealthCheckProperty() throws Exception {
        healthCheckEnabled.setOrClearProperty("true");
        createMemberWithRestConfigAndAssertConfigException();
    }

    @Test
    public void testRestConfigWithMemcacheProperty() throws Exception {
        memcacheEnabled.setOrClearProperty("true");
        createMemberWithRestConfigAndAssertConfigException();
    }

    @Test
    public void testRestConfigWithRestPropertyDisabled() throws Exception {
        restEnabled.setOrClearProperty("false");
        createMemberWithRestConfigAndAssertConfigException();
    }

    @Test
    public void testRestConfigWithHealthCheckPropertyDisabled() throws Exception {
        healthCheckEnabled.setOrClearProperty("false");
        createMemberWithRestConfigAndAssertConfigException();
    }

    @Test
    public void testRestConfigWithMemcachePropertyDisabled() throws Exception {
        memcacheEnabled.setOrClearProperty("false");
        createMemberWithRestConfigAndAssertConfigException();
    }

    @Test
    public void testMemcachePropertyEnabled() throws Exception {
        memcacheEnabled.setOrClearProperty("true");
        HazelcastInstance hz = factory.newHazelcastInstance(null);
        assertTextProtocolResponse(hz, TEST_URL_MEMCACHE);
        assertNoTextProtocolResponse(hz, TEST_URL_DATA);
        assertNoTextProtocolResponse(hz, TEST_URL_HEALTH_CHECK);
    }

    @Test
    public void testMemcachePropertyDisabled() throws Exception {
        memcacheEnabled.setOrClearProperty("false");
        HazelcastInstance hz = factory.newHazelcastInstance(null);
        assertNoTextProtocolResponse(hz, TEST_URL_MEMCACHE);
        assertNoTextProtocolResponse(hz, TEST_URL_DATA);
        assertNoTextProtocolResponse(hz, TEST_URL_HEALTH_CHECK);
    }

    @Test
    public void testHealthCheckPropertyEnabled() throws Exception {
        healthCheckEnabled.setOrClearProperty("true");
        HazelcastInstance hz = factory.newHazelcastInstance(null);
        assertTextProtocolResponse(hz, TEST_URL_HEALTH_CHECK);
        assertNoTextProtocolResponse(hz, TEST_URL_MEMCACHE);
        assertNoTextProtocolResponse(hz, TEST_URL_DATA);
    }

    @Test
    public void testHealthCheckPropertyDisabled() throws Exception {
        healthCheckEnabled.setOrClearProperty("false");
        HazelcastInstance hz = factory.newHazelcastInstance(null);
        assertNoTextProtocolResponse(hz, TEST_URL_MEMCACHE);
        assertNoTextProtocolResponse(hz, TEST_URL_DATA);
        assertNoTextProtocolResponse(hz, TEST_URL_HEALTH_CHECK);
    }

    @Test
    public void testRestPropertyEnabled() throws Exception {
        restEnabled.setOrClearProperty("true");
        HazelcastInstance hz = factory.newHazelcastInstance(null);
        hz.getMap("test").put("testKey", "testValue");
        assertTextProtocolResponse(hz, TEST_URL_DATA);
        assertTextProtocolResponse(hz, TEST_URL_HEALTH_CHECK);
        assertNoTextProtocolResponse(hz, TEST_URL_MEMCACHE);
    }

    @Test
    public void testRestPropertyDisabled() throws Exception {
        restEnabled.setOrClearProperty("false");
        HazelcastInstance hz = factory.newHazelcastInstance(null);
        hz.getMap("test").put("testKey", "testValue");
        assertNoTextProtocolResponse(hz, TEST_URL_MEMCACHE);
        assertNoTextProtocolResponse(hz, TEST_URL_DATA);
        assertNoTextProtocolResponse(hz, TEST_URL_HEALTH_CHECK);
    }

    @Test
    public void testAllRestPropertiesEnabled() throws Exception {
        restEnabled.setOrClearProperty("true");
        healthCheckEnabled.setOrClearProperty("true");
        memcacheEnabled.setOrClearProperty("true");
        HazelcastInstance hz = factory.newHazelcastInstance(null);
        hz.getMap("test").put("testKey", "testValue");
        assertTextProtocolResponse(hz, TEST_URL_DATA);
        assertTextProtocolResponse(hz, TEST_URL_HEALTH_CHECK);
        assertTextProtocolResponse(hz, TEST_URL_MEMCACHE);
    }

    private void createMemberWithRestConfigAndAssertConfigException() {
        Config config = new Config();
        config.setRestApiConfig(new RestApiConfig());
        expectedException.expect(ConfigurationException.class);
        factory.newHazelcastInstance(config);
    }
}
