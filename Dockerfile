FROM amazoncorretto:21
WORKDIR /payments
COPY build/libs/*.jar payments.jar
ENTRYPOINT ["java", "-jar", "payments.jar"]