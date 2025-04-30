/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.am.integration.tests.api.lifecycle;

import com.google.gson.Gson;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.am.integration.clients.internal.api.dto.RevokedEventsDTO;
import org.wso2.am.integration.clients.internal.api.dto.RevokedJWTDTO;
import org.wso2.am.integration.clients.internal.api.dto.RevokedJWTListDTO;
import org.wso2.am.integration.clients.publisher.api.ApiException;
import org.wso2.am.integration.clients.publisher.api.ApiResponse;
import org.wso2.am.integration.clients.publisher.api.v1.dto.APIDTO;
import org.wso2.am.integration.clients.publisher.api.v1.dto.APIOperationsDTO;
import org.wso2.am.integration.clients.store.api.v1.dto.APIKeyDTO;
import org.wso2.am.integration.clients.store.api.v1.dto.ApplicationDTO;
import org.wso2.am.integration.clients.store.api.v1.dto.ApplicationKeyDTO;
import org.wso2.am.integration.clients.store.api.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.am.integration.clients.store.api.v1.dto.SubscriptionDTO;
import org.wso2.am.integration.test.utils.APIManagerIntegrationTestException;
import org.wso2.am.integration.test.utils.base.APIMIntegrationConstants;
import org.wso2.am.integration.test.utils.bean.APILifeCycleAction;
import org.wso2.am.integration.test.utils.bean.APIRequest;
import org.wso2.am.integration.test.utils.generic.APIMTestCaseUtils;
import org.wso2.am.integration.test.utils.http.HTTPSClientUtils;
import org.wso2.am.integration.test.utils.http.HttpRequestUtil;
import org.wso2.am.integration.test.utils.token.TokenUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * This class tests the behaviour of API when there is choice of selection between oauth2 and mutual ssl in API Manager.
 */
@SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
public class APISecurityTestCase extends APIManagerLifecycleBaseTest {

    private static final Log log = LogFactory.getLog(APISecurityTestCase.class);

    private final String mutualSSLOnlyAPIName = "mutualsslOnlyAPI";
    private final String mutualSSLWithOAuthAPI = "mutualSSLWithOAuthAPI";
    private final String mutualSSLandOauthMandatoryAPI = "mutualSSLandOAuthMandatoryAPI";
    private final String apiKeySecuredAPI = "apiKeySecuredAPI";
    private final String OauthDisabledAPI = "OauthDisabledAPI";
    private final String OauthEnabledAPI = "OauthEnabledAPI";
    private final String mutualSSLOnlyAPIContext = "mutualsslOnlyAPI";
    private final String mutualSSLWithOAuthAPIContext = "mutualSSLWithOAuthAPI";
    private final String mutualSSLandOAuthMandatoryAPIContext = "mutualSSLandOAuthMandatoryAPI";
    private final String OauthDisabledAPIContext = "OauthDisabledAPI";
    private final String OauthEnabledAPIContext = "OauthEnabledAPI";
    private final String apiKeySecuredAPIContext = "apiKeySecuredAPI";
    private final String basicAuthSecuredAPI = "BasicAuthSecuredAPI";
    private final String basicAuthSecuredAPIContext = "BasicAuthSecuredAPI";
    private final String API_END_POINT_METHOD = "/customers/123";
    private final String API_VERSION_1_0_0 = "1.0.0";
    private final String APPLICATION_NAME = "AccessibilityOfDeprecatedOldAPIAndPublishedCopyAPITestCase";
    private String accessToken;
    private final String API_END_POINT_POSTFIX_URL1 = "jaxrs_basic/services/customers/customerservice/";
    private final String API_END_POINT_POSTFIX_URL2 = "jaxrs_basic/services/customers/customerservice2/";
    private String apiEndPointUrl;
    private String applicationId;
    private String consumerKey;
    private String consumerSecret;
    private String apiId1, apiId2;
    private String apiId3, apiId4;
    private String apiId5;
    private String apiId6;
    private String apiId7;
    private SubscriptionDTO subscriptionDTO;
    private final String API_RESPONSE_DATA = "<id>123</id><name>John</name></Customer>";
    String users[] = {"apisecUser", "apisecUser2@wso2.com", "apisecUser2@abc.com"};
    String endUserPassword = "password@123";

    @DataProvider
    public static Object[][] userModeDataProvider() {

        return new Object[][]{new Object[]{TestUserMode.SUPER_TENANT_ADMIN},
                new Object[]{TestUserMode.TENANT_ADMIN}};
    }

    private void createUser() throws RemoteException,
            RemoteUserStoreManagerServiceUserStoreExceptionException, UserStoreException {

        for (String user : users) {
            remoteUserStoreManagerServiceClient.addUser(user, endUserPassword, new String[]{}, new ClaimValue[]{},
                    "default", false);
        }

    }


    @Factory(dataProvider = "userModeDataProvider")
    public APISecurityTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws APIManagerIntegrationTestException, IOException, ApiException,
            org.wso2.am.integration.clients.store.api.ApiException, XPathExpressionException, AutomationUtilException,
            InterruptedException, JSONException, RemoteUserStoreManagerServiceUserStoreExceptionException,
            UserStoreException {
        super.init(userMode);
        createUser();
        String apiSandboxEndPointUrl = backEndServerUrl.getWebAppURLHttp() + API_END_POINT_POSTFIX_URL2;
        apiEndPointUrl = backEndServerUrl.getWebAppURLHttp() + API_END_POINT_POSTFIX_URL1;

        APIRequest apiRequest1 = new APIRequest(mutualSSLOnlyAPIName, mutualSSLOnlyAPIContext,
                new URL(apiEndPointUrl), new URL(apiSandboxEndPointUrl));
        apiRequest1.setVersion(API_VERSION_1_0_0);
        apiRequest1.setTiersCollection(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest1.setTier(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest1.setTags(API_TAGS);
        apiRequest1.setVisibility(APIDTO.VisibilityEnum.PUBLIC.getValue());
        apiRequest1.setProvider(user.getUserName());
        APIOperationsDTO apiOperationsDTO1 = new APIOperationsDTO();
        apiOperationsDTO1.setVerb("GET");
        apiOperationsDTO1.setTarget("/customers/{id}");
        apiOperationsDTO1.setAuthType("Application & Application User");
        apiOperationsDTO1.setThrottlingPolicy("Unlimited");

        List<APIOperationsDTO> operationsDTOS = new ArrayList<>();
        operationsDTOS.add(apiOperationsDTO1);
        apiRequest1.setOperationsDTOS(operationsDTOS);

        List<String> securitySchemes = new ArrayList<>();
        securitySchemes.add("mutualssl");
        securitySchemes.add("mutualssl_mandatory");
        apiRequest1.setSecurityScheme(securitySchemes);
        apiRequest1.setDefault_version("false");
        apiRequest1.setHttps_checked("https");
        apiRequest1.setHttp_checked(null);
        apiRequest1.setDefault_version_checked("false");
        HttpResponse response1 = restAPIPublisher.addAPI(apiRequest1);
        apiId1 = response1.getData();

        String certOneSandbox = getAMResourceLocation() + File.separator + "lifecycletest" + File.separator + "mutualssl"
                + File.separator + "abcde.crt";

        String certOneProduction = getAMResourceLocation() + File.separator + "lifecycletest" + File.separator + "mutualssl"
                + File.separator + "example.crt";

        restAPIPublisher.uploadCertificate(new File(certOneSandbox), "example_sand", apiId1,
                APIMIntegrationConstants.API_TIER.UNLIMITED, APIMIntegrationConstants.KEY_TYPE.SANDBOX);
        restAPIPublisher.uploadCertificate(new File(certOneProduction), "example_prod", apiId1,
                APIMIntegrationConstants.API_TIER.UNLIMITED, APIMIntegrationConstants.KEY_TYPE.PRODUCTION);

        APIRequest apiRequest2 = new APIRequest(mutualSSLWithOAuthAPI, mutualSSLWithOAuthAPIContext,
                new URL(apiEndPointUrl));
        apiRequest2.setVersion(API_VERSION_1_0_0);
        apiRequest2.setProvider(user.getUserName());
        apiRequest2.setTiersCollection(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest2.setTier(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest2.setTags(API_TAGS);
        apiRequest2.setVisibility(APIDTO.VisibilityEnum.PUBLIC.getValue());
        apiRequest2.setOperationsDTOS(operationsDTOS);
        apiRequest2.setDefault_version("false");
        apiRequest2.setHttps_checked("https");
        apiRequest2.setHttp_checked(null);
        apiRequest2.setDefault_version_checked("false");
        List<String> securitySchemes2 = new ArrayList<>();
        securitySchemes2.add("mutualssl");
        securitySchemes2.add("oauth2");
        securitySchemes2.add("api_key");
        securitySchemes2.add("oauth_basic_auth_api_key_mandatory");
        apiRequest2.setSecurityScheme(securitySchemes2);

        HttpResponse response2 = restAPIPublisher.addAPI(apiRequest2);
        apiId2 = response2.getData();

        String certTwo = getAMResourceLocation() + File.separator + "lifecycletest" + File.separator + "mutualssl"
                + File.separator + "example.crt";
        restAPIPublisher.uploadCertificate(new File(certTwo), "abcde", apiId2,
                APIMIntegrationConstants.API_TIER.UNLIMITED, APIMIntegrationConstants.KEY_TYPE.SANDBOX);


        APIRequest apiRequest3 = new APIRequest(mutualSSLandOauthMandatoryAPI, mutualSSLandOAuthMandatoryAPIContext,
                new URL(apiEndPointUrl));
        apiRequest3.setVersion(API_VERSION_1_0_0);
        apiRequest3.setTiersCollection(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest3.setTier(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest3.setTags(API_TAGS);
        apiRequest3.setVisibility(APIDTO.VisibilityEnum.PUBLIC.getValue());
        apiRequest3.setOperationsDTOS(operationsDTOS);
        apiRequest3.setProvider(user.getUserName());

        List<String> securitySchemes3 = new ArrayList<>();
        securitySchemes3.add("mutualssl");
        securitySchemes3.add("oauth2");
        securitySchemes3.add("api_key");
        securitySchemes3.add("mutualssl_mandatory");
        securitySchemes3.add("oauth_basic_auth_api_key_mandatory");
        apiRequest3.setSecurityScheme(securitySchemes3);
        apiRequest3.setDefault_version("false");
        apiRequest3.setHttps_checked("https");
        apiRequest3.setHttp_checked(null);
        apiRequest3.setDefault_version_checked("false");
        HttpResponse response3 = restAPIPublisher.addAPI(apiRequest3);
        apiId3 = response3.getData();
        String certThree = getAMResourceLocation() + File.separator + "lifecycletest" + File.separator + "mutualssl"
                + File.separator + "example.crt";
        restAPIPublisher.uploadCertificate(new File(certThree), "abcdef", apiId3,
                        APIMIntegrationConstants.API_TIER.UNLIMITED, APIMIntegrationConstants.KEY_TYPE.SANDBOX);
        // Create Revision and Deploy to Gateway

        // Add an API Secured with APIKey only
        APIRequest apiRequest4 = new APIRequest(apiKeySecuredAPI, apiKeySecuredAPIContext, new URL(apiEndPointUrl));
        apiRequest4.setVersion(API_VERSION_1_0_0);
        apiRequest4.setTiersCollection(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest4.setTier(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest4.setTags(API_TAGS);
        apiRequest4.setVisibility(APIDTO.VisibilityEnum.PUBLIC.getValue());
        apiRequest4.setOperationsDTOS(operationsDTOS);
        apiRequest4.setDefault_version("false");
        apiRequest4.setHttps_checked("https");
        apiRequest4.setHttp_checked(null);
        apiRequest4.setProvider(user.getUserName());
        apiRequest4.setDefault_version_checked("false");
        List<String> securitySchemes4 = new ArrayList<>();
        securitySchemes4.add("api_key");
        securitySchemes4.add("oauth_basic_auth_api_key_mandatory");
        apiRequest4.setSecurityScheme(securitySchemes4);
        apiRequest4.setSandbox(apiEndPointUrl);

        HttpResponse response4 = restAPIPublisher.addAPI(apiRequest4);
        apiId4 = response4.getData();

        APIRequest apiRequest5 = new APIRequest(basicAuthSecuredAPI, basicAuthSecuredAPIContext,
                new URL(apiEndPointUrl));
        apiRequest5.setVersion(API_VERSION_1_0_0);
        apiRequest5.setTiersCollection(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest5.setTier(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest5.setTags(API_TAGS);
        apiRequest5.setVisibility(APIDTO.VisibilityEnum.PUBLIC.getValue());
        apiRequest5.setOperationsDTOS(operationsDTOS);
        apiRequest5.setProvider(user.getUserName());

        List<String> securitySchemes5 = new ArrayList<>();
        securitySchemes5.add("basic_auth");
        securitySchemes5.add("oauth_basic_auth_api_key_mandatory");
        apiRequest5.setSecurityScheme(securitySchemes5);
        apiRequest5.setDefault_version("false");
        apiRequest5.setHttps_checked("https");
        apiRequest5.setHttp_checked(null);
        HttpResponse response5 = restAPIPublisher.addAPI(apiRequest5);
        apiId5 = response5.getData();
        createAPIRevisionAndDeployUsingRest(apiId5, restAPIPublisher);
        restAPIPublisher.changeAPILifeCycleStatusToPublish(apiId5, false);
        waitForAPIDeploymentSync(apiRequest5.getProvider(), apiRequest5.getName(), apiRequest5.getVersion(),
                APIMIntegrationConstants.IS_API_EXISTS);

        APIRequest apiRequest6 = new APIRequest(OauthDisabledAPI, OauthDisabledAPIContext,
                new URL(apiEndPointUrl));

        APIOperationsDTO apiOperationsDTO2 = new APIOperationsDTO();
        apiOperationsDTO2.setVerb("GET");
        apiOperationsDTO2.setTarget("/customers/{id}");
        apiOperationsDTO2.setAuthType("None");
        apiOperationsDTO2.setThrottlingPolicy("Unlimited");
        APIOperationsDTO apiOperationsDTO3 = new APIOperationsDTO();
        apiOperationsDTO3.setVerb("POST");
        apiOperationsDTO3.setTarget("/customers/{id}");
        apiOperationsDTO3.setAuthType("None");
        apiOperationsDTO3.setThrottlingPolicy("Unlimited");
        List<APIOperationsDTO> operationsDTOS2 = new ArrayList<>();
        operationsDTOS2.add(apiOperationsDTO2);
        operationsDTOS2.add(apiOperationsDTO3);

        apiRequest6.setVersion(API_VERSION_1_0_0);
        apiRequest6.setTiersCollection(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest6.setTier(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest6.setTags(API_TAGS);
        apiRequest6.setVisibility(APIDTO.VisibilityEnum.PUBLIC.getValue());
        apiRequest6.setOperationsDTOS(operationsDTOS2);
        apiRequest6.setProvider(user.getUserName());
        List<String> securitySchemes6 = new ArrayList<>();
        securitySchemes6.add("oauth2");
        apiRequest6.setSecurityScheme(securitySchemes6);
        apiRequest6.setDefault_version("false");
        apiRequest6.setHttps_checked("https");
        apiRequest6.setHttp_checked(null);
        apiRequest6.setDefault_version_checked("false");

        HttpResponse response6 = restAPIPublisher.addAPI(apiRequest6);
        apiId6 = response6.getData();

        createAPIRevisionAndDeployUsingRest(apiId6, restAPIPublisher);
        restAPIPublisher.changeAPILifeCycleStatusToPublish(apiId6, false);
        waitForAPIDeploymentSync(apiRequest6.getProvider(), apiRequest6.getName(), apiRequest6.getVersion(),
                APIMIntegrationConstants.IS_API_EXISTS);

        APIRequest apiRequest7 = new APIRequest(OauthEnabledAPI, OauthEnabledAPIContext,
                new URL(apiEndPointUrl));

        apiRequest7.setVersion(API_VERSION_1_0_0);
        apiRequest7.setTiersCollection(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest7.setTier(APIMIntegrationConstants.API_TIER.UNLIMITED);
        apiRequest7.setTags(API_TAGS);
        apiRequest7.setVisibility(APIDTO.VisibilityEnum.PUBLIC.getValue());
        apiRequest7.setProvider(user.getUserName());

        apiRequest7.setOperationsDTOS(operationsDTOS);
        apiRequest7.setSecurityScheme(securitySchemes4);
        apiRequest7.setDefault_version("false");
        apiRequest7.setHttps_checked("https");
        apiRequest7.setHttp_checked(null);
        apiRequest7.setDefault_version_checked("false");

        HttpResponse response7 = restAPIPublisher.addAPI(apiRequest7);
        apiId7 = response7.getData();
    }

    @Test(description = "This test case tests the behaviour of internal Key token on Created API with authentication " +
            "types")
    public void testCreateAndDeployRevisionWithInternalKeyTesting() throws JSONException, ApiException,
            XPathExpressionException, APIManagerIntegrationTestException, IOException,
            org.wso2.am.integration.clients.store.api.ApiException, InterruptedException {
        createAPIRevisionAndDeployUsingRest(apiId1, restAPIPublisher);
        APIDTO api1 = restAPIPublisher.getAPIByID(apiId1);
        waitForAPIDeploymentSync(api1.getProvider(), api1.getName(), api1.getVersion(),
                APIMIntegrationConstants.IS_API_EXISTS);
        ApiResponse<org.wso2.am.integration.clients.publisher.api.v1.dto.APIKeyDTO> keyDTOApiResponse1 =
                restAPIPublisher.generateInternalApiKey(apiId1);
        Assert.assertEquals(keyDTOApiResponse1.getStatusCode(), 200);
        HttpResponse httpResponse1 = invokeApiWithInternalKey(mutualSSLOnlyAPIContext, API_VERSION_1_0_0,
                API_END_POINT_METHOD, keyDTOApiResponse1.getData().getApikey());
        Assert.assertEquals(httpResponse1.getResponseCode(), 200);
        restAPIPublisher.changeAPILifeCycleStatus(apiId1, APILifeCycleAction.PUBLISH.getAction());
        createAPIRevisionAndDeployUsingRest(apiId2, restAPIPublisher);
        APIDTO api2 = restAPIPublisher.getAPIByID(apiId2);
        waitForAPIDeploymentSync(api2.getProvider(), api2.getName(), api2.getVersion(),
                APIMIntegrationConstants.IS_API_EXISTS);
        ApiResponse<org.wso2.am.integration.clients.publisher.api.v1.dto.APIKeyDTO> keyDTOApiResponse2 =
                restAPIPublisher.generateInternalApiKey(apiId2);
        HttpResponse httpResponse2 = invokeApiWithInternalKey(mutualSSLWithOAuthAPIContext, API_VERSION_1_0_0,
                API_END_POINT_METHOD, keyDTOApiResponse2.getData().getApikey());
        Assert.assertEquals(httpResponse2.getResponseCode(), 200);
        HttpResponse httpResponse3 = invokeApiWithInternalKey(mutualSSLWithOAuthAPIContext, API_VERSION_1_0_0,
                API_END_POINT_METHOD, keyDTOApiResponse1.getData().getApikey());
        Assert.assertEquals(httpResponse3.getResponseCode(), 403);
        // verify internal key authentication after publish
        restAPIPublisher.changeAPILifeCycleStatus(apiId2, APILifeCycleAction.PUBLISH.getAction());
        httpResponse2 = invokeApiWithInternalKey(mutualSSLWithOAuthAPIContext, API_VERSION_1_0_0,
                API_END_POINT_METHOD, keyDTOApiResponse2.getData().getApikey());
        Assert.assertEquals(httpResponse2.getResponseCode(), 200);

        createAPIRevisionAndDeployUsingRest(apiId3, restAPIPublisher);
        APIDTO api3 = restAPIPublisher.getAPIByID(apiId3);
        waitForAPIDeploymentSync(api3.getProvider(), api3.getName(), api3.getVersion(),
                APIMIntegrationConstants.IS_API_EXISTS);

        ApiResponse<org.wso2.am.integration.clients.publisher.api.v1.dto.APIKeyDTO> keyDTOApiResponse3 =
                restAPIPublisher.generateInternalApiKey(apiId3);
        HttpResponse httpResponse4 = invokeApiWithInternalKey(mutualSSLandOAuthMandatoryAPIContext, API_VERSION_1_0_0,
                API_END_POINT_METHOD, keyDTOApiResponse3.getData().getApikey());
        Assert.assertEquals(httpResponse4.getResponseCode(), 200);
        restAPIPublisher.changeAPILifeCycleStatus(apiId3, APILifeCycleAction.PUBLISH.getAction());

        createAPIRevisionAndDeployUsingRest(apiId4, restAPIPublisher);
        APIDTO api4 = restAPIPublisher.getAPIByID(apiId4);
        waitForAPIDeploymentSync(api4.getProvider(), api4.getName(), api4.getVersion(),
                APIMIntegrationConstants.IS_API_EXISTS);
        ApiResponse<org.wso2.am.integration.clients.publisher.api.v1.dto.APIKeyDTO> keyDTOApiResponse4 =
                restAPIPublisher.generateInternalApiKey(apiId3);
        HttpResponse httpResponse5 = invokeApiWithInternalKey(mutualSSLandOAuthMandatoryAPIContext, API_VERSION_1_0_0,
                API_END_POINT_METHOD, keyDTOApiResponse4.getData().getApikey());
        Assert.assertEquals(httpResponse5.getResponseCode(), 200);
        restAPIPublisher.changeAPILifeCycleStatus(apiId4, APILifeCycleAction.PUBLISH.getAction());
        HttpResponse applicationResponse = restAPIStore.createApplication(APPLICATION_NAME,
                "Test Application", APIMIntegrationConstants.APPLICATION_TIER.UNLIMITED,
                ApplicationDTO.TokenTypeEnum.JWT);

        applicationId = applicationResponse.getData();
        restAPIStore.subscribeToAPI(apiId3, applicationId, APIMIntegrationConstants.APPLICATION_TIER.UNLIMITED);
        restAPIStore.subscribeToAPI(apiId2, applicationId, APIMIntegrationConstants.APPLICATION_TIER.UNLIMITED);
        subscriptionDTO = restAPIStore.subscribeToAPI(apiId4, applicationId, APIMIntegrationConstants.APPLICATION_TIER.UNLIMITED);
        assertNotNull(subscriptionDTO, "API Subscription Failed");
        ArrayList grantTypes = new ArrayList();
        grantTypes.add(APIMIntegrationConstants.GRANT_TYPE.PASSWORD);
        grantTypes.add(APIMIntegrationConstants.GRANT_TYPE.CLIENT_CREDENTIAL);

        ApplicationKeyDTO applicationKeyDTO = restAPIStore.generateKeys(applicationId, "36000", "",
                ApplicationKeyGenerateRequestDTO.KeyTypeEnum.PRODUCTION, null, grantTypes);
        //get access token
        accessToken = applicationKeyDTO.getToken().getAccessToken();
        consumerKey = applicationKeyDTO.getConsumerKey();
        consumerSecret = applicationKeyDTO.getConsumerSecret();
        HttpResponse httpResponseAfterPublish = invokeApiWithInternalKey(mutualSSLOnlyAPIContext, API_VERSION_1_0_0,
                API_END_POINT_METHOD, keyDTOApiResponse1.getData().getApikey());
        Assert.assertEquals(httpResponseAfterPublish.getResponseCode(), 200);

        // wait until certificates loaded
        Thread.sleep(120000);
    }

    private HttpResponse invokeApiWithInternalKey(String context, String version, String resource,
                                                  String internalKey) throws XPathExpressionException, IOException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "application/json");
        requestHeaders.put("Internal-Key", internalKey);
        return HttpRequestUtil.doGet(getAPIInvocationURLHttps(context, version) + resource, requestHeaders);
    }

    @Test(description = "This test case tests the behaviour of APIs that are protected with mutual SSL and OAuth2 "
            + "when the client certificate is not presented but OAuth2 token is presented.", dependsOnMethods =
            {"testCreateAndDeployRevisionWithInternalKeyTesting"})
    public void testCreateAndPublishAPIWithOAuth2() throws XPathExpressionException, IOException, JSONException {
        // Create requestHeaders
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "application/json");
        requestHeaders.put("Authorization", "Bearer " + accessToken);

        HttpResponse apiResponse = HttpRequestUtil
                .doGet(getAPIInvocationURLHttps(mutualSSLOnlyAPIContext, API_VERSION_1_0_0) + API_END_POINT_METHOD,
                        requestHeaders);
        JSONObject response = new JSONObject(apiResponse.getData());
        //fix test failure due to error code changes introduced in product-apim pull #7106
        assertEquals(response.getString("code"), "900901",
                "API invocation succeeded with the access token without need for mutual ssl");
        apiResponse = HttpRequestUtil
                .doGet(getAPIInvocationURLHttps(mutualSSLWithOAuthAPIContext, API_VERSION_1_0_0) + API_END_POINT_METHOD,
                        requestHeaders);
        assertEquals(apiResponse.getResponseCode(), HTTP_RESPONSE_CODE_OK,
                "API invocation failed for a test case with valid access token when the API is protected with "
                        + "both mutual sso and oauth2");
    }

    private Map<String, String> createRequestHeadersForAPIKey(String apiKey, String ip, String referer) {

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "text/xml");
        requestHeaders.put("apikey", apiKey);
        if (ip != null) {
            requestHeaders.put("X-Forwarded-For", ip);
        }
        if (referer != null) {
            requestHeaders.put("Referer", referer);
        }
        return requestHeaders;
    }

    @Test(description = "Testing the invocation with BasicAuth", dependsOnMethods =
            {"testCreateAndPublishAPIWithOAuth2"})
    public void testInvokeBasicAuth() throws Exception {
        String user1 = users[0];
//        Map<String, String> requestHeaders1 = new HashMap<>();
//        requestHeaders1.put("Authorization",
//                "Basic " + Base64.encodeBase64String(user1.concat("@").concat(this.user.getUserDomain()).concat(":")
//                        .concat("randomPassword1").getBytes()));
//        HttpResponse response = HttpRequestUtil.doGet(getAPIInvocationURLHttps(basicAuthSecuredAPIContext,
//                API_VERSION_1_0_0) + API_END_POINT_METHOD, requestHeaders1);
//        log.info("Received Response : " + response.getResponseCode() + " " + response.getResponseMessage() + " " + response.getResponseMessage());
//        Assert.assertEquals(response.getResponseCode(), 401);
        HttpResponse response;
        for (String user : users) {
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization",
                    "Basic " + Base64.encodeBase64String(user.concat("@").concat(this.user.getUserDomain()).concat(
                            ":").concat(endUserPassword).getBytes()));
            response = HttpRequestUtil.doGet(getAPIInvocationURLHttps(basicAuthSecuredAPIContext,
                    API_VERSION_1_0_0) + API_END_POINT_METHOD, requestHeaders);
            Assert.assertEquals(response.getResponseCode(), 200);
        }
        Map<String, String> requestHeaders2 = new HashMap<>();
        requestHeaders2.put("Authorization",
                "Basic " + Base64.encodeBase64String(user1.concat("@").concat(this.user.getUserDomain()).concat(":")
                        .concat("randomPassword1").getBytes()));
        response = HttpRequestUtil.doGet(getAPIInvocationURLHttps(basicAuthSecuredAPIContext,
                API_VERSION_1_0_0) + API_END_POINT_METHOD, requestHeaders2);
        Assert.assertEquals(response.getResponseCode(), 401);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpArtifacts() throws Exception {
        restAPIStore.deleteApplication(applicationId);
        restAPIPublisher.deleteAPI(apiId1);
        restAPIPublisher.deleteAPI(apiId2);
        restAPIPublisher.deleteAPI(apiId3);
        restAPIPublisher.deleteAPI(apiId4);
        restAPIPublisher.deleteAPI(apiId5);
        restAPIPublisher.deleteAPI(apiId6);
        restAPIPublisher.deleteAPI(apiId7);
        removeUsers();
    }

    public String generateBase64EncodedCertificate() throws IOException {

        String certOne = getAMResourceLocation() + File.separator + "lifecycletest" + File.separator + "mutualssl"
                + File.separator + "example.crt";
        String base64EncodedString = IOUtils.toString(new FileInputStream(certOne));
        base64EncodedString = Base64.encodeBase64URLSafeString(base64EncodedString.getBytes());
        return base64EncodedString;
    }

    private void removeUsers() throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {
        for (String user : users) {
            remoteUserStoreManagerServiceClient.removeUser(user);
        }
    }

    private void verifyRevokedTokenAvailable(String alias)
            throws org.wso2.am.integration.clients.internal.ApiException, InterruptedException {
        int retryCount = 0;
        RevokedJWTDTO selectedRevokedJWTDTO = null;
        do {
            RevokedEventsDTO revokedEventsDTO = restAPIInternal.retrieveRevokedList();
            List<RevokedJWTDTO> revokedJWTList = revokedEventsDTO.getRevokedJWTList();
            for (RevokedJWTDTO revokedJWTDTO : revokedJWTList) {
                if (alias.equals(revokedJWTDTO.getJwtSignature())) {
                    selectedRevokedJWTDTO = revokedJWTDTO;
                    break;
                }
            }
            if (selectedRevokedJWTDTO != null) {
                break;
            }
            retryCount++;
            Thread.sleep(5000);
        } while (retryCount < 20);
        Assert.assertNotNull(selectedRevokedJWTDTO, "Revoked Token didn't store in database");
    }

    private List<Object> validateResourceSecurity(String swaggerContent) throws APIManagementException {
        OpenAPIParser parser = new OpenAPIParser();
        SwaggerParseResult swaggerParseResult = parser.readContents(swaggerContent, null, null);
        OpenAPI openAPI = swaggerParseResult.getOpenAPI();
        Paths paths = openAPI.getPaths();
        List<Object> authType = new ArrayList<>();
        for (String pathKey : paths.keySet()) {
            Map<PathItem.HttpMethod, Operation> operationsMap = paths.get(pathKey).readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : operationsMap.entrySet()) {
                Operation operation = entry.getValue();
                Map<String, Object> extensions = operation.getExtensions();
                Assert.assertNotNull(extensions.get("x-auth-type"));
                authType.add(extensions.get("x-auth-type"));
            }
        }
        return authType;
    }

}
