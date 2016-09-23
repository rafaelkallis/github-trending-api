FROM maven:alpine
COPY . /
RUN mvn -D skipTests=true install
VOLUME /tmp
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usr/scr/app/target/github-trending-api-0.0.1-SNAPSHOT.jar"]
EXPOSE 8080
