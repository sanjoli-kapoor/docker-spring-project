FROM eclipse-temurin:17
#ENV REDIS_USER=XXX MONGO_PA SS=XXX
RUN mkdir -p /home/app
COPY ./People.csv /home/app
ADD target/docker-spring.jar docker-spring.jar
#                            (this jar with above name will be
#                    added to root directory of the container)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "docker-spring.jar"]
