# One Dockerfile for every Spring Boot service. It packages a jar already built on the host
# (mvn package reuses the host's ~/.m2, which is much faster than downloading inside Docker).
# Build them all with k8s/build-images.ps1, or one by hand from the repository root:
#   cd inventory-service; mvn -q package -DskipTests; cd ..
#   docker build --build-arg SERVICE=inventory-service -t eshop/inventory-service:0.1 .

FROM eclipse-temurin:17-jre
ARG SERVICE
RUN useradd --system --uid 1001 app && mkdir -p /app/logs && chown -R app /app
WORKDIR /app
COPY ${SERVICE}/target/${SERVICE}-*.jar app.jar
USER app
# Heap is sized from the container's memory limit, not the node's RAM.
# LOG_DIR moves logback's file appender off its ../logs default, which is not writable here.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=64m" \
    LOG_DIR=/app/logs
ENTRYPOINT ["java", "-jar", "app.jar"]
