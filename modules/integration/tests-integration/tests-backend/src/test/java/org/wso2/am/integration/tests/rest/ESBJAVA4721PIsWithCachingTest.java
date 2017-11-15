/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
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
package org.wso2.am.integration.tests.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.am.integration.test.utils.base.APIMIntegrationBaseTest;
import org.wso2.am.integration.test.utils.base.APIMIntegrationConstants;
import org.wso2.am.integration.test.utils.bean.APILifeCycleState;
import org.wso2.am.integration.test.utils.bean.APILifeCycleStateRequest;
import org.wso2.am.integration.test.utils.bean.APIRequest;
import org.wso2.am.integration.test.utils.bean.APIThrottlingTier;
import org.wso2.am.integration.test.utils.bean.APPKeyRequestGenerator;
import org.wso2.am.integration.test.utils.bean.SubscriptionRequest;
import org.wso2.am.integration.test.utils.clients.APIPublisherRestClient;
import org.wso2.am.integration.test.utils.clients.APIStoreRestClient;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This test enabling PIs in synapse.properties when JsonStream builder and formatter is used
 */
public class ESBJAVA4721PIsWithCachingTest extends APIMIntegrationBaseTest {

    private final Log log = LogFactory.getLog(JsonResponseWithSingleElementWithBracketTestCase.class);
    private APIStoreRestClient apiStore;
    private String apiContext = "tempAPI1";
    private String appName = "sample-application1";
    private Map<String, String> requestHeaders = new HashMap<String, String>();

    private ServerConfigurationManager serverConfigurationManager;

    @Factory(dataProvider = "userModeDataProvider")
    public ESBJAVA4721PIsWithCachingTest(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init(userMode);
        String publisherURLHttp = getPublisherURLHttp();
        String storeURLHttp = getStoreURLHttp();

        apiStore = new APIStoreRestClient(storeURLHttp);
        APIPublisherRestClient apiPublisher = new APIPublisherRestClient(publisherURLHttp);

        apiPublisher.login(user.getUserName(), user.getPassword());
        apiStore.login(user.getUserName(), user.getPassword());

        String gatewaySessionCookie = createSession(gatewayContextMgt);

        //Load the back-end API
        if (TestUserMode.SUPER_TENANT_ADMIN == userMode) {
            loadSynapseConfigurationFromClasspath(
                    "artifacts" + File.separator + "AM" + File.separator + "synapseconfigs" + File.separator + "rest"
                            + File.separator + "single_element_json_return_api.xml", gatewayContextMgt,
                    gatewaySessionCookie);
        }

        //create API
        String backendEndPoint = getGatewayURLNhttp();
        String apiName = "tempAPI1";
        APIRequest apiRequest = new APIRequest(apiName, apiContext, new URL(backendEndPoint));
        //enable response caching
        apiRequest.setResponseCachingEnabled("enabled");
        apiPublisher.addAPI(apiRequest);

        log.info("API " + apiName + " is added");

        //publish API
        APILifeCycleStateRequest updateRequest = new APILifeCycleStateRequest(apiName, user.getUserName(),
                APILifeCycleState.PUBLISHED);
        apiPublisher.changeAPILifeCycleStatus(updateRequest);

        log.info("API " + apiName + " is published");

        //create an Application
        apiStore.addApplication(appName, APIThrottlingTier.UNLIMITED.getState(), "", "this-is-test");

        log.info("Application " + apiName + " is created");

        //subscribe application to API
        String provider = user.getUserName();
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(apiName, provider);
        subscriptionRequest.setApplicationName(appName);
        subscriptionRequest.setTier("Gold");
        apiStore.subscribe(subscriptionRequest);

        log.info("subscribed " + appName + " to API " + apiName);

        //generate keys
        APPKeyRequestGenerator generateAppKeyRequest = new APPKeyRequestGenerator(appName);
        String responseString = apiStore.generateApplicationKey(generateAppKeyRequest).getData();
        log.info(responseString);
        JSONObject response = new JSONObject(responseString);
        String accessToken = response.getJSONObject("data").getJSONObject("key").get("accessToken").toString();
        Assert.assertNotNull(accessToken, "Access Token not found " + responseString);

        requestHeaders.put("Authorization", "Bearer " + accessToken);

        log.info("Generated keys for API " + apiName);

    }

    @Test(groups = { "wso2.am" },
          description = "test if PIs enabled and working fine")
    public void testAPIInvocation() throws Exception {
        requestHeaders.put("Accept", "application/json");

        //first request - will not hit the cache
        HttpRequestUtil.doGet(getAPIInvocationURLHttp(apiContext + "/1.0.0/backend"), requestHeaders);

        //second request - will  hit the cache. Still should serve correct data
        HttpResponse serviceResponse = HttpRequestUtil
                .doGet(getAPIInvocationURLHttp(apiContext + "/1.0.0/backend"), requestHeaders);

        log.info("Response " + serviceResponse.getData());

        assertEquals(serviceResponse.getResponseCode(), Response.Status.OK.getStatusCode(),
                "Response code mismatched when api invocation");
        //backend returning payload has element "services":[ "water" ]
        assertTrue(serviceResponse.getData().contains("[ \"water\" ]"), "Response data mismatched when api invocation");

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        apiStore.removeApplication(appName);
        super.cleanUp();
    }

    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][] {
                new Object[] { TestUserMode.SUPER_TENANT_ADMIN },
                };
    }

}