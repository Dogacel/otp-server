FROM eclipse-temurin:21-jre

COPY ./app/build/libs/app-all.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
