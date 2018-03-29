/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.am.integration.tests.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.am.integration.test.utils.base.APIMIntegrationBaseTest;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.File;

public class HttpDeleteWithoutBodyTestCase extends APIMIntegrationBaseTest {

    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][] {
                new Object[] {TestUserMode.SUPER_TENANT_ADMIN},
                new Object[] {TestUserMode.TENANT_ADMIN},
        };
    }

    @Factory(dataProvider = "userModeDataProvider")
    public HttpDeleteWithoutBodyTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init(userMode);
        String gatewaySessionCookie = createSession(gatewayContextMgt);
        loadSynapseConfigurationFromClasspath("artifacts" + File.separator + "AM"
                + File.separator + "synapseconfigs" + File.separator + "rest"
                + File.separator + "http-delete-without-body-synapse.xml", gatewayContextMgt, gatewaySessionCookie);
    }

    @Test(groups = "wso2.am", description = "Test if DELETE without a payload is working")
    public void testDeleteWithoutPayload() {

        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpDelete httpDelete = new HttpDelete(getGatewayURLNhttps() + "delete/without/body/delete");
        HttpResponse response;

        try {
            response = httpclient.execute(httpDelete);
            Assert.assertNotNull(response, "Received null response for DELETE request without a payload");
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                    "Did not receive an http 200 for DELETE request without a payload");
        } catch (Exception e) {
            Assert.fail("HTTP DELETE without a payload is NOT working: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanUp();
    }

}
