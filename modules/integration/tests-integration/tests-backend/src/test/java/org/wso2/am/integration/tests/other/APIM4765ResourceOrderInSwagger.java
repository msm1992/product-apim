/*
*Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.am.integration.tests.other;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.*;
import org.wso2.am.integration.test.utils.base.APIMIntegrationBaseTest;
import org.wso2.am.integration.test.utils.bean.APILifeCycleState;
import org.wso2.am.integration.test.utils.bean.APILifeCycleStateRequest;
import org.wso2.am.integration.test.utils.bean.APIRequest;
import org.wso2.am.integration.test.utils.clients.APIPublisherRestClient;
import org.wso2.am.integration.test.utils.clients.APIStoreRestClient;
import org.wso2.am.integration.tests.api.lifecycle.APIManagerLifecycleBaseTest;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * This is the test case for https://wso2.org/jira/browse/APIMANAGER-4765
 */
@SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE }) public class APIM4765ResourceOrderInSwagger
        extends APIManagerLifecycleBaseTest {

    private APIPublisherRestClient apiPublisher;
    private APIStoreRestClient apiStore;
    private String providerName;


    private static final Log log = LogFactory.getLog(APIM4765ResourceOrderInSwagger.class);

    @Factory(dataProvider = "userModeDataProvider") public APIM4765ResourceOrderInSwagger(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @DataProvider public static Object[][] userModeDataProvider() {
        return new Object[][] { new Object[] { TestUserMode.SUPER_TENANT_ADMIN }, };
    }

    @BeforeClass(alwaysRun = true) public void setEnvironment() throws Exception {
        super.init(userMode);

        apiPublisher = new APIPublisherRestClient(getPublisherURLHttp());
        apiStore = new APIStoreRestClient(getStoreURLHttp());

        apiPublisher.login(user.getUserName(), user.getPassword());
        apiStore.login(user.getUserName(), user.getPassword());
        providerName = publisherContext.getContextTenant().getContextUser().getUserName();

    }

    @Test(groups = { "webapp" }, description = "Test resource order in the swagger") public void swaggerResourceOrderTest()
            throws Exception {

        String swagger = "{\"paths\":{\"/*\":{\"get\":{\"x-auth-type\":\"Application \",\"x-throttling-tier\":\"Plus\","
                + "\"responses\":{\"200\":{}}}},\"/post\":{\"get\":{\"x-auth-type\":\"Application \","
                + "\"x-throttling-tier\":\"Plus\",\"responses\":{\"200\":{}}}},\"/list\":{\"get\":{\"x-auth-type\":"
                + "\"Application \",\"x-throttling-tier\":\"Plus\",\"responses\":{\"200\":{}}}}},\"swagger\":\"2.0\","
                + "\"x-wso2-security\":{\"apim\":{\"x-wso2-scopes\":[]}},\"info\":{\"licence\":{},\"title\":"
                + "\"TokenTestAPI\",\"description\":\"This is test API create by API manager integration test\","
                + "\"contact\":{\"email\":null,\"name\":null},\"version\":\"1.0.0\"}}";

        String resourceOrder = "{\"paths\":{\"/*\":{\"get\":{\"x-auth-type\":\"Application \",\"x-throttling-tier\""
                + ":\"Plus\",\"responses\":{\"200\":{}}}},\"/post\":{\"get\":{\"x-auth-type\":\"Application \","
                + "\"x-throttling-tier\":\"Plus\",\"responses\":{\"200\":{}}}},\"/list\":{\"get\":"
                + "{\"x-auth-type\":\"Application \",\"x-throttling-tier\":\"Plus\",\"responses\":{\"200\":{}}}}}";

        apiPublisher.updateResourceOfAPI(providerName, API_NAME, API_VERSION_1_0_0, swagger);
        //get swagger doc.
        String swaggerURL = getStoreURLHttps() + "store/api-docs/admin/APITest/1.0.0";

        Map<String, String> requestHeadersSandBox = new HashMap<String, String>();
        HttpResponse swaggerFileReadFromRegistry = HttpRequestUtil.doGet(swaggerURL, requestHeadersSandBox);

        String swaggerTextFromRegisrtry = swaggerFileReadFromRegistry.getData();

        //resourceOrder should  be equal to the given resource order.
        boolean isResourceOrderEqaul = swaggerTextFromRegisrtry.contains(resourceOrder);

        assertTrue(isResourceOrderEqaul, "Resource order is not equal to the given order.");

    }
}
