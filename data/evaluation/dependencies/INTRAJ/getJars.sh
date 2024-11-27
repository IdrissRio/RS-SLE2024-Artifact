#!/bin/bash

# List of JAR files to copy
JAR_FILES=(
  "~/.gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.graphstream/gs-core/2.0/3d7ff769c11283016c6b3cc02d29480d035ceb4b/gs-core-2.0.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.graphstream/gs-ui-swing/2.0/b62a97400c9307d7410c2e2e048aa300f3ea68bf/gs-ui-swing-2.0.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.graphstream/gs-algo/2.0/b939ed25a5e9d7b0d06bf1b6f6ae39a6138d707d/gs-algo-2.0.jar"
  "~/.gradle/caches/modules-2/files-2.1/com.sparkjava/spark-core/2.9.3/7c8800d6c442d940ac3f9cb8d5e09f9948faf6cd/spark-core-2.9.3.jar"
  "~/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.8.9/8a432c1d6825781e21a02db2e2c33c5fde2833b9/gson-2.8.9.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.graphstream/pherd/1.0/def146e11a24b48f88e8af5fa14ca383f8ee4dd2/pherd-1.0.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.graphstream/mbox2/1.0/c20049788c1bde824e17cd8e26c2f515a1eab352/mbox2-1.0.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-math/2.1/b3c4bdc2778ddccceb8da2acec3e37bfa41303e9/commons-math-2.1.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-math3/3.4.1/3ac44a8664228384bc68437264cf7c4cf112f579/commons-math3-3.4.1.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.jfree/jfreechart/1.0.14/fa67c798b0ae80b84f3854d69e341abacd3867c5/jfreechart-1.0.14.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api/1.7.25/da76ca59f6a57ee3102f8f9bd9cee742973efa8a/slf4j-api-1.7.25.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-webapp/9.4.31.v20200723/9e6716366f586307f253d1082cbae88f33c239cd/jetty-webapp-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty.websocket/websocket-server/9.4.31.v20200723/e45865d39dd749c4feac66b8f6c2dd7df914e121/websocket-server-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-servlet/9.4.31.v20200723/dd1718a61f30e28b1cbf5a1c02997082d887c054/jetty-servlet-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-security/9.4.31.v20200723/6e37b3590820c5d8a466acd65d5c30672b2367f6/jetty-security-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-server/9.4.31.v20200723/b9043b4a0c17ee543aba97e80ea3a34cd8cdb600/jetty-server-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty.websocket/websocket-servlet/9.4.31.v20200723/c8688b80f740354f0da7e185ff70a9061e904683/websocket-servlet-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.jfree/jcommon/1.0.17/7bcb68fde08258e59fe7bcc758c08af830fb2c1d/jcommon-1.0.17.jar"
  "~/.gradle/caches/modules-2/files-2.1/xml-apis/xml-apis/1.3.04/90b215f48fe42776c8c7f6e3509ec54e84fd65ef/xml-apis-1.3.04.jar"
  "~/.gradle/caches/modules-2/files-2.1/com.lowagie/itext/2.1.5/a25c4c425ebf812c65184e81d4c84d22e8641b35/itext-2.1.5.jar"
  "~/.gradle/caches/modules-2/files-2.1/javax.servlet/javax.servlet-api/3.1.0/3cd63d075497751784b2fa84be59432f4905bf7c/javax.servlet-api-3.1.0.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty.websocket/websocket-client/9.4.31.v20200723/786dc9e2a6f5cb1e68229b1652c15d21b488f52b/websocket-client-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-client/9.4.31.v20200723/a4d779d1dcd7fa948d1175ebf3e20a0b0caacdc/jetty-client-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-http/9.4.31.v20200723/6862f0e6fc7e9f8828416a7cae1477b233d92f8/jetty-http-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty.websocket/websocket-common/9.4.31.v20200723/aafea15d115377e05609a60ff10db0e9682a928e/websocket-common-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-io/9.4.31.v20200723/328e4562e0f30e01efea63efe4fc24b2b860d852/jetty-io-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-xml/9.4.31.v20200723/9cccd5b9d76324dac3f2ffc501adf10621250a78/jetty-xml-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty.websocket/websocket-api/9.4.31.v20200723/56eb7d4002268c9f5f4bb4266cf0cb247bda49e0/websocket-api-9.4.31.v20200723.jar"
  "~/.gradle/caches/modules-2/files-2.1/bouncycastle/bcmail-jdk14/138/14ff2dfec8578f5f6838c4d6a77a86789afe5382/bcmail-jdk14-138.jar"
  "~/.gradle/caches/modules-2/files-2.1/bouncycastle/bcprov-jdk14/138/de366c3243a586eb3c0e2bcde1ed9bb1bfb985ff/bcprov-jdk14-138.jar"
  "~/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty/jetty-util/9.4.31.v20200723/b9c346da72d8715bdfb0373a123374ec6ac9e544/jetty-util-9.4.31.v20200723.jar"

)

# Loop through each JAR file and copy to the current directory
for jar_file in "${JAR_FILES[@]}"; do
  cp $jar_file .
done

echo "All JAR files have been copied to the current directory."
