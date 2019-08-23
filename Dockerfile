# Set the base image to Ubuntu
FROM ubuntu:18.04

# Install OpenJDK 8
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

ARG GO_PIPELINE_COUNTER=$GO_PIPELINE_COUNTER
ARG DOCKER_TAG_PREFIX=$DOCKER_TAG_PREFIX
ENV SENTRY_RELEASE=$DOCKER_TAG_PREFIX-$GO_PIPELINE_COUNTER-gocd

RUN mkdir /data
WORKDIR /data
ADD target/projects-api-v2.jar projects.jar

CMD java -jar projects.jar