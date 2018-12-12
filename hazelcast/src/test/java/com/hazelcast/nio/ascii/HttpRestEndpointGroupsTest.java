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

import static com.hazelcast.config.RestEndpointGroup.CLUSTER_READ;
import static com.hazelcast.config.RestEndpointGroup.CLUSTER_WRITE;
import static com.hazelcast.config.RestEndpointGroup.DATA;
import static com.hazelcast.config.RestEndpointGroup.HEALTH_CHECK;
import static com.hazelcast.config.RestEndpointGroup.HOT_RESTART;
import static com.hazelcast.config.RestEndpointGroup.MEMCACHE;
import static com.hazelcast.config.RestEndpointGroup.WAN;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import com.hazelcast.config.RestEndpointGroup;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.cluster.Versions;
import com.hazelcast.test.HazelcastParallelParametersRunnerFactory;
import com.hazelcast.test.annotation.QuickTest;

/**
 * Tests if HTTP REST URLs are protected by the correct REST endpoint groups.
 */
@RunWith(Parameterized.class)
@UseParametersRunnerFactory(HazelcastParallelParametersRunnerFactory.class)
@Category(QuickTest.class)
public class HttpRestEndpointGroupsTest extends AbstractProtocolsFilterTestBase {

    private static final TestUrl[] TEST_URLS = new TestUrl[] {
            new TestUrl(CLUSTER_WRITE, POST, "/hazelcast/rest/mancenter/changeurl ", "HTTP/1.1 500"),
            new TestUrl(CLUSTER_WRITE, POST, "/hazelcast/rest/mancenter/security/permissions", "forbidden"),
            new TestUrl(CLUSTER_READ, GET, "/hazelcast/rest/cluster", "Members {size:1, ver:1} ["),
            new TestUrl(CLUSTER_READ, POST, "/hazelcast/rest/management/cluster/state", "fail"),
            new TestUrl(CLUSTER_WRITE, POST, "/hazelcast/rest/management/cluster/changeState", "HTTP/1.1 500"),
            new TestUrl(CLUSTER_WRITE, POST, "/hazelcast/rest/management/cluster/version", "HTTP/1.1 500"),
            new TestUrl(CLUSTER_READ, GET, "/hazelcast/rest/management/cluster/version", Versions.CURRENT_CLUSTER_VERSION.toString()),
            new TestUrl(CLUSTER_WRITE, POST, "/hazelcast/rest/management/cluster/clusterShutdown", "fail"),
            new TestUrl(CLUSTER_WRITE, POST, "/hazelcast/rest/management/cluster/memberShutdown", "fail"),
            new TestUrl(CLUSTER_READ, POST, "/hazelcast/rest/management/cluster/nodes", "fail"),
            new TestUrl(HOT_RESTART, POST, "/hazelcast/rest/management/cluster/forceStart", "fail"),
            new TestUrl(HOT_RESTART, POST, "/hazelcast/rest/management/cluster/partialStart", "fail"),
            new TestUrl(HOT_RESTART, POST, "/hazelcast/rest/management/cluster/hotBackup", "fail"),
            new TestUrl(HOT_RESTART, POST, "/hazelcast/rest/management/cluster/hotBackupInterrupt", "fail"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/wan/sync/map", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/wan/sync/allmaps", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/wan/clearWanQueues", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/wan/addWanConfig", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/wan/pausePublisher", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/wan/stopPublisher", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/wan/resumePublisher", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/wan/consistencyCheck/map", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/wan/sync/map", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/wan/sync/allmaps", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/mancenter/clearWanQueues", "HTTP/1.1 500"),
            new TestUrl(WAN, POST, "/hazelcast/rest/wan/addWanConfig", "HTTP/1.1 500"),
            new TestUrl(HEALTH_CHECK, GET, "/hazelcast/health/node-state", "ACTIVE"),
            new TestUrl(HEALTH_CHECK, GET, "/hazelcast/health/cluster-state", "ACTIVE"),
            new TestUrl(HEALTH_CHECK, GET, "/hazelcast/health/cluster-safe", "HTTP/1.1 200"),
            new TestUrl(HEALTH_CHECK, GET, "/hazelcast/health/migration-queue-size", "HTTP/1.1 200"),
            new TestUrl(HEALTH_CHECK, GET, "/hazelcast/health/cluster-size", "HTTP/1.1 200"),
            new TestUrl(DATA, POST, "/hazelcast/rest/maps/", "HTTP/1.1 400"),
            new TestUrl(DATA, GET, "/hazelcast/rest/maps/", "HTTP/1.1 400"),
            new TestUrl(DATA, DELETE, "/hazelcast/rest/maps/", "HTTP/1.1 200"),
            new TestUrl(DATA, POST, "/hazelcast/rest/queues/", "HTTP/1.1 400"),
            new TestUrl(DATA, GET, "/hazelcast/rest/queues/", "HTTP/1.1 400"),
            new TestUrl(DATA, DELETE, "/hazelcast/rest/queues/", "HTTP/1.1 400"),
            new TestUrl(CLUSTER_WRITE, POST, "/hazelcast/1", "HTTP/1.1 404"),
            new TestUrl(CLUSTER_WRITE, GET, "/hazelcast/1", "HTTP/1.1 404"),
            new TestUrl(CLUSTER_WRITE, DELETE, "/hazelcast/1", "HTTP/1.1 404"),
            // Following URL doesn't represent an HTTP request, but a memcached text protocol command
            new TestUrl(MEMCACHE, "version", "foo", "VERSION Hazelcast"),
    };

    @Parameter
    public RestEndpointGroup restEndpointGroup;

    @Parameters(name = "restEndpointGroup:{0}")
    public static RestEndpointGroup[] parameters() {
        return RestEndpointGroup.values();
    }

    @Test
    public void testGroupEnabled() throws Exception {
        HazelcastInstance hz = factory.newHazelcastInstance(createConfigWithEnabledGroups(restEndpointGroup));
        for (TestUrl testUrl : TEST_URLS) {
            if (restEndpointGroup == testUrl.restEndpointGroup) {
                assertTextProtocolResponse(hz, testUrl);
            }
        }
    }

    @Test
    public void testGroupDisabled() throws Exception {
        HazelcastInstance hz = factory.newHazelcastInstance(createConfigWithDisabledGroups(restEndpointGroup));
        for (TestUrl testUrl : TEST_URLS) {
            if (restEndpointGroup == testUrl.restEndpointGroup) {
                assertNoTextProtocolResponse(hz, testUrl);
            }
        }
    }

    @Test
    public void testOthersWhenGroupEnabled() throws Exception {
        HazelcastInstance hz = factory.newHazelcastInstance(createConfigWithEnabledGroups(restEndpointGroup));
        for (TestUrl testUrl : TEST_URLS) {
            if (restEndpointGroup != testUrl.restEndpointGroup) {
                assertNoTextProtocolResponse(hz, testUrl);
            }
        }
    }

    @Test
    public void testOthersWhenGroupDisabled() throws Exception {
        HazelcastInstance hz = factory.newHazelcastInstance(createConfigWithDisabledGroups(restEndpointGroup));
        for (TestUrl testUrl : TEST_URLS) {
            if (restEndpointGroup != testUrl.restEndpointGroup) {
                assertTextProtocolResponse(hz, testUrl);
            }
        }
    }
}
