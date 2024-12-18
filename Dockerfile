FROM eclipse-temurin:21-alpine as jre-build

RUN $JAVA_HOME/bin/jlink \
 --add-modules java.base,java.desktop,java.sql,java.naming,java.management,java.net.http,jdk.jdwp.agent,jdk.crypto.ec,jdk.unsupported \
 --strip-java-debug-attributes \
 --no-man-pages \
 --no-header-files \
 --compress=2 \
 --output /javaruntime

FROM alpine
COPY --from=jre-build /javaruntime $JAVA_HOME
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
ENV PORT=8888
ENV APP="/opt/applications/basic-server.jar"
ENV PROJECT_NUMBER="320300059816"
ENV PROJECT_REGION="us-central1"

RUN mkdir /opt/applications
COPY applications/basic-server/build/libs/basic-server.jar /opt/applications/
COPY applications/data-analyzer-server/build/libs/data-analyzer-server.jar /opt/applications/
COPY applications/data-collector-server/build/libs/data-collector-server.jar /opt/applications/

CMD java -jar ${APP}
