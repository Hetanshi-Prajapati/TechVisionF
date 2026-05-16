FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .

RUN chmod +x signup/mvnw

RUN cd signup && ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "signup/target/*.jar"]