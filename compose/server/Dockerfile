FROM eclipse-temurin:17.0.1_12-jdk

RUN mkdir /banno-interview

COPY ./bin/start-script.sh /banno-interview/start-script.sh
COPY ./target/scala-2.13/banno-interview-assembly-0.0.1.jar /banno-interview/banno-interview.jar

EXPOSE 8081
WORKDIR /banno-interview

RUN /usr/sbin/useradd interview && \
    /bin/chown -R interview:interview /banno-interview && \
    chmod +x /banno-interview/start-script.sh

USER interview

ENTRYPOINT ["/banno-interview/start-script.sh"]