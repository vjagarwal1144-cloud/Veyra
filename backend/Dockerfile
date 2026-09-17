FROM eclipse-temurin:17-jre
WORKDIR /app
COPY build/libs/veyra-backend-*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
