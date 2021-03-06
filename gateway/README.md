# WSO2 API Gateway

This is WSO2 API Gateway powered by Ballerina. 


## Building from the source

If you want to build APIM Gateway from the source code:

1. Install Java 8(http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
1. Install Apache Maven 3.x.x(https://maven.apache.org/download.cgi#)
1. Get a clone or download the source from this repository (https://github.com/wso2/product-apimgt).
1. Run the Maven command ``mvn clean install`` from the ``product-apimgt/gateway`` directory.
1. Extract the WSO2 APIM Gateway distribution created at `product-apimgt/gateway/target/wso2apim-gateway-<version>-SNAPSHOT.zip` to your local directory.

## Starting the server

1. Go to `product-apimgt/gateway/target/wso2apim-gateway-<version>-SNAPSHOT` directory
1. Execute the below command.
``./ballerina run service org/wso2/carbon/apimgt/gateway``

