/*
 *     Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.wso2.am.integration.tests.other;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.am.integration.test.utils.base.APIMIntegrationBaseTest;
import org.wso2.am.integration.test.utils.base.APIMIntegrationConstants;
import org.wso2.am.integration.test.utils.bean.APILifeCycleState;
import org.wso2.am.integration.test.utils.bean.APILifeCycleStateRequest;
import org.wso2.am.integration.test.utils.bean.APIRequest;
import org.wso2.am.integration.test.utils.bean.APPKeyRequestGenerator;
import org.wso2.am.integration.test.utils.bean.SubscriptionRequest;
import org.wso2.am.integration.test.utils.clients.APIPublisherRestClient;
import org.wso2.am.integration.test.utils.clients.APIStoreRestClient;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.testng.annotations.*;

import java.io.File;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.core.Response;

import static org.testng.Assert.assertEquals;

/**
 * Test case to test DELETE with payload support
 */
public class APIMANAGER5858HttpDELETEWithPayloadSupportTestCase extends APIMIntegrationBaseTest {
    private APIPublisherRestClient apiPublisher;
    private APIStoreRestClient apiStore;

    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][] { new Object[] { TestUserMode.SUPER_TENANT_ADMIN } };
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        String gatewaySessionCookie = createSession(gatewayContextMgt);

        //Load the back-end dummy API
        loadSynapseConfigurationFromClasspath(
                "artifacts" + File.separator + "AM" + File.separator + "synapseconfigs" + File.separator + "rest"
                        + File.separator + "dummy_api_APIMANAGER5858.xml", gatewayContextMgt, gatewaySessionCookie);
    }

    @Test(groups = "wso2.am", description = "Check functionality of HTTP PATCH support for APIM")
    public void testHttpPatchSupport() throws Exception {

        String APIContext = "HttpDeleteWithPayloadContext";
        String APIVersion = "1.0.0";
        String requestMessage = "{\"requestMsg\":\"This is request Message\"}";
        String expectedResponse = "{\"responseMsg\":\"This is request Message\"}";

        String apiInvocationUrl = getAPIInvocationURLHttp(APIContext, APIVersion);

        HttpClient client = HttpClientBuilder.create().build();
        HttpDeleteWithEntity request = new HttpDeleteWithEntity(apiInvocationUrl);
        request.setHeader("Accept", "application/json");
        StringEntity payload = new StringEntity(requestMessage, "UTF-8");
        payload.setContentType("application/json");
        request.setEntity(payload);

        HttpResponse httpResponse = client.execute(request);
        HttpEntity responseEntity = httpResponse.getEntity();
        String response = EntityUtils.toString(responseEntity);

        //Assertion
        assertEquals(httpResponse.getStatusLine().getStatusCode(), Response.Status.OK.getStatusCode(),
                "The response code is not 200 OK");
        assertEquals(response, expectedResponse, "The response message does not match expected");

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanUp();
    }

    /**
     * Since org.apache.http.client.methods.HttpDelete does not support entity, we have to extend HttpPost capabilities
     * to act as Http DELETE
     */
    private class HttpDeleteWithEntity extends HttpPost {
        public static final String METHOD_NAME = "DELETE";

        public HttpDeleteWithEntity() {
        }

        public HttpDeleteWithEntity(URI uri) {
            super(uri);
        }

        public HttpDeleteWithEntity(String uri) {
            super(uri);
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }

}
