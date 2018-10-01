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
RUN wget https://dl.bintray.com/jbake/binary/jbake-2.6.1-bin.zip && \
  unzip jbake-2.6.1-bin.zip
  
# set environment variables
ENV PROJECT_NAME="test"
ENV PATH=$PATH:/jbake-2.6.1-bin/bin/
 
# clone project  
RUN git clone ${PROJECT_NAME}

# prepare build directory and mount volume
RUN mkdir /var/www/html/${PROJECT_NAME}
VOLUME /var/www/html/${PROJECT_NAME}

# add webhook listener
ADD dist/webhook-listener.jar webhook-listener.jar
EXPOSE 8080

# run webhook-listener
CMD java -jar -Dworkingdir=${PROJECT_NAME} -Dtarget=/var/www/html/${PROJECT_NAME} webhook-listener.jar