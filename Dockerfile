# base image
FROM openjdk:8-jre-alpine

# maintainer details
MAINTAINER Georg Leber "info@georg-leber.de"

# change working directory
WORKDIR /

# update packages and install git, wget, unzip and openssh
RUN apk --update add git wget unzip openssh && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*

# install jbake
RUN \
  wget https://dl.bintray.com/jbake/binary/jbake-2.6.1-bin.zip && \
  unzip jbake-2.6.1-bin.zip

# set environment variables
ENV PATH=$PATH:/jbake-2.6.1-bin/bin/

# init git repository
RUN \
  mkdir desire-to-travel && \
  cd desire-to-travel && \
  git init && \
  git remote add origin https://github.com/GeorgHenkel/desire-to-travel.git && \
  cd ..

# prepare image
ADD dist/webhook-listener.jar webhook-listener.jar
VOLUME /var/www/html/desire-to-travel
EXPOSE 8080

# run webhook-listener
CMD java -jar -Dworkingdir=desire-to-travel -Dtarget=/var/www/html/desire-to-travel webhook-listener.jar
