/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.am.integration.tests.other;

import java.net.URL;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.am.integration.test.utils.base.APIMIntegrationBaseTest;
import org.wso2.am.integration.test.utils.bean.APIRequest;
import org.wso2.am.integration.test.utils.clients.APIPublisherRestClient;
import org.wso2.am.integration.test.utils.clients.APIStoreRestClient;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

public class APIMANAGER5777EndpointMessageContentMissingTestCase extends APIMIntegrationBaseTest{
	
    private final Log log = LogFactory.getLog(APIMANAGER5777EndpointMessageContentMissingTestCase.class);
    private APIPublisherRestClient apiPublisher;
    private APIStoreRestClient apiStore;
    private String apiName = "TestEndpointConfAPI";
    private String apiContext = "testEndpointConfAPI";
    @Factory(dataProvider = "userModeDataProvider")
    public APIMANAGER5777EndpointMessageContentMissingTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init(userMode);
        String publisherURLHttp = getPublisherURLHttp();
        String storeURLHttp = getStoreURLHttp();

        apiStore = new APIStoreRestClient(storeURLHttp);
        apiPublisher = new APIPublisherRestClient(publisherURLHttp);

        apiPublisher.login(user.getUserName(), user.getPassword());
        apiStore.login(user.getUserName(), user.getPassword());

    }
    @Test(groups = {"wso2.am"}, description = "Sample API Publishing")
    public void testEndpointMessageContentDefinition() throws Exception {
		String backendEndPoint = getBackendEndServiceEndPointHttp("jaxrs_basic/services/customers/customerservice");
		APIRequest apiRequest = new APIRequest(apiName, apiContext, new URL(backendEndPoint));
		HttpResponse serviceResponse = apiPublisher.addAPI(apiRequest);
		verifyResponse(serviceResponse);

		HttpResponse implPageRespone = apiPublisher.getAPIImplementPage(apiName, "admin", "1.0.0");
		Assert.assertEquals(implPageRespone.getResponseCode(), HttpStatus.SC_OK, "Response code is not as expected");

		// check for <legend>Message Content</legend> section
		Assert.assertTrue(implPageRespone.getData().contains("<legend>Message Content</legend>"),
				"Message Content is missing in endpoint configuration");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanUp();
    }

    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][]{
                new Object[]{TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

}
