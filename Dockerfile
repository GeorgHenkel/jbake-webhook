# base image
FROM openjdk:8

# maintainer details
MAINTAINER Georg Leber "info@georg-leber.de"

# change working directory
WORKDIR /

# update packages and install git
RUN  \
  export DEBIAN_FRONTEND=noninteractive && \
  sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list && \
  apt-get update && \
  apt-get -y upgrade && \
  apt-get install -y wget unzip git

# install jbake
RUN \
  wget https://dl.bintray.com/jbake/binary/jbake-2.6.1-bin.zip && \
  unzip jbake-2.6.1-bin.zip

# set environment variables
ENV PATH=$PATH:/jbake-2.6.1-bin/bin/

# create directories
RUN \
  mkdir -p /var/www/html/desire-to-travel && \
  chown -R www-data:www-data /var/www/html/desire-to-travel && \
  mkdir desire-to-travel && \
  chown -R www-data:www-data desire-to-travel

# change user
USER www-data

# clone git repository
RUN git clone https://github.com/GeorgHenkel/desire-to-travel.git

# prepare image
ADD dist/webhook-listener.jar webhook-listener.jar
VOLUME /var/www/html/desire-to-travel
EXPOSE 8080

# run webhook-listener
CMD java -jar -Dworkingdir=desire-to-travel -Dtarget=/var/www/html/desire-to-travel webhook-listener.jar
