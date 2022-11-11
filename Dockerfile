FROM eclipse-temurin:17.0.1_12-jdk

RUN mkdir /banno-interview

COPY ./bin/start-script.sh /banno-interview/start-script.sh
COPY target/scala-2.13/banno-interview-0.0.1.jar /reportify/banno-interview.jar

EXPOSE 8081
WORKDIR /reportify

RUN /usr/sbin/useradd interview && \
    /bin/chown -R interview:interview /banno-interview && \
    chmod +x /banno-interview/start-script.sh

USER reportify

ENTRYPOINT ["/banno-interview/start-script.sh"]