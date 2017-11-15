/*
*Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.am.integration.tests.restapi.testcases;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.am.integration.test.utils.base.APIMIntegrationBaseTest;
import org.wso2.am.integration.test.utils.http.HTTPSClientUtils;
import org.wso2.am.integration.tests.restapi.RESTAPITestConstants;
import org.wso2.am.integration.tests.restapi.utils.RESTAPITestUtil;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
public class APIMANAGER5903TagsWithUpperCase extends APIMIntegrationBaseTest {

    @Factory(dataProvider = "userModeDataProvider")
    public APIMANAGER5903TagsWithUpperCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][]{
                new Object[]{TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init(userMode);
    }

    @Test(groups = {"wso2.am"}, description = "REST API Implementation test : API handling test case")
    public void testAPIs() {

        String gatewayURL = getGatewayURLNhttp();
        String keyManagerURL = getKeyManagerURLHttp();

        //file name of the JSON data file related to : API handling test case
        String dataFileName = "APIMANAGER5903TagsWithUpperCase.txt";
        String dataFilePath = (new File(System.getProperty("user.dir"))).getParent() +
                RESTAPITestConstants.PATH_SUBSTRING + dataFileName;
        boolean testSuccessStatus = new RESTAPITestUtil().testRestAPI(dataFilePath, gatewayURL, keyManagerURL);
        assertTrue(testSuccessStatus);

        int retry = 300;
        int count = 0;
        HttpResponse response;
        String publisherRestApiBaseUrl = getStoreURLHttps() + "api/am/store/v0.11/";
        while (retry > 0) {
            try {

                response = HTTPSClientUtils.doGet(publisherRestApiBaseUrl + "apis?query=tag:upperCase", null);
                JSONObject json = new JSONObject(response.getData());
                count = json.getInt("count");
                if (count < 1) {
                    Thread.sleep(1000);
                } else {
                    break;
                }

            } catch (IOException e) {
            } catch (JSONException e) {
            } catch (InterruptedException e) {
            }
            retry--;
        }
        Assert.assertEquals(count, 1);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanUp();
    }

}
