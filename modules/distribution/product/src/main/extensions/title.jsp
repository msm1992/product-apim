<%--
  ~ Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<!-- localize.jsp MUST already be included in the calling script -->
<%@ page import="java.io.FileReader" %>
<%@ page import="org.json.simple.parser.JSONParser"%>
<%@ page import="org.json.simple.JSONObject"%>

<%
    String tenant = request.getParameter("tenantDomain");

    String headerTitle = "API Manager";
    String logoAltText = "WSO2";
    String logoTitle = "WSO2";
    String footerText = "WSO2 API Manager";
    String footerLink = "https://wso2.com/";
    String footerLinkText = "Inc";
    String pageTitle = "WSO2 API Manager";
    String tenantLogoFile = "images/logo-inverse.svg";
    String tenantCSSFile = "";

    if (tenant != null) {
        String current = new File(".").getCanonicalPath();
        String tenantConfLocation = "/repository/deployment/server/jaggeryapps/tenantthemes/site/public/";
        String tenantThemeDirectoryName = tenant.replace(".", "_");
        String tenantThemeFile =  current + tenantConfLocation + "/" + tenantThemeDirectoryName + "/" + "customtheme.json";

        File directory = new File(current + tenantConfLocation + "/" + tenantThemeDirectoryName);
        if (directory != null && directory.exists() && directory.isDirectory()) {
            File themeFile = new File(tenantThemeFile);
            if (themeFile != null && themeFile.exists() && themeFile.isFile()) {
                FileReader fr = new FileReader(themeFile);
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(fr);
                JSONObject jsonObject = (JSONObject) obj;

                JSONObject headerThemeObj = (JSONObject)jsonObject.get("header");
                if (headerThemeObj != null) {
                    headerTitle = (String)(headerThemeObj.get("title")) != null ? (String)(headerThemeObj.get("title")) : "API Manager";
                }

                JSONObject logoConfig = (JSONObject)jsonObject.get("logo");
                if (logoConfig != null) {
                    logoAltText = (String)(logoConfig.get("alternateText")) != null ? (String)(logoConfig.get("alternateText")) : "WSO2";
                    logoTitle = (String)(logoConfig.get("title")) != null ? (String)(logoConfig.get("title")) : "WSO2";
                }

                JSONObject footerThemeObj = (JSONObject)jsonObject.get("footer");
                if (footerThemeObj != null) {
                    footerText = (String)(footerThemeObj.get("name"));
                    footerLink = (String)(footerThemeObj.get("link"));
                    footerLinkText = (String)(footerThemeObj.get("linkText"));
                }

                pageTitle = (String)jsonObject.get("title") != null ? (String)jsonObject.get("title") : "WSO2 API Manager";

                tenantCSSFile = "/tenantthemes/site/public/" + tenantThemeDirectoryName + "/customtheme.css";
                tenantLogoFile = "/tenantthemes/site/public/" + tenantThemeDirectoryName + "/logo.svg";
            }
        }
    }
    request.setAttribute("headerTitle", headerTitle);
    request.setAttribute("logoAltText", logoAltText);
    request.setAttribute("logoTitle", logoTitle);
    request.setAttribute("footerText", footerText);
    request.setAttribute("footerLink", footerLink);
    request.setAttribute("footerLinkText", footerLinkText);
    request.setAttribute("pageTitle", pageTitle);
    request.setAttribute("tenantCSSFile", tenantCSSFile);
    request.setAttribute("tenantLogoFile", tenantLogoFile);
%>

<!-- title -->

<title><%=request.getAttribute("pageTitle")%></title>