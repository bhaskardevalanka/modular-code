
#docker image for maven 3.9.5 and jkd 21
FROM 769126030485.dkr.ecr.us-east-1.amazonaws.com/learning/java_mvn:21 AS builder1
LABEL org.opencontainers.image.authors="Bhaskar Devalanka"

# Change directory to the project folder
WORKDIR /opt

COPY . .

#build the project
RUN ./apache-maven/bin/mvn clean package -DskipTests


#Final stage
FROM 769126030485.dkr.ecr.us-east-1.amazonaws.com/learning/java_mvn:21


RUN apt-get update -y && apt-get upgrade libc-bin -y && apt-get upgrade liblzma5 -y && apt-get install zlib1g -y



COPY --from=builder1 /opt/target/gateway-*-SNAPSHOT.jar  app.jar

# Create group and user with same UID/GID as host
RUN groupadd -g 1023 cvi && \
    useradd -u 1023 -g cvi -m -s /bin/bash cvi

RUN chown cvi:cvi /app/app.jar

USER cvi

 
ENTRYPOINT ["java","-Xmx1g","-Xms1g","-jar","/app/app.jar"]
