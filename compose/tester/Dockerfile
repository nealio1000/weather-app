FROM eclipse-temurin:17.0.1_12-jdk

ARG SBT_VERSION=1.7.3

RUN mkdir /banno-interview

COPY . /banno-interview

# Install sbt
RUN \
  mkdir /working/ && \
  cd /working/ && \
  curl -L -o sbt-$SBT_VERSION.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get -y update && \
  apt-get -y install sbt && \
  cd && \
  rm -r /working/ && \
  sbt sbtVersion

WORKDIR /banno-interview
