FROM --platform=${BUILDPLATFORM} eclipse-temurin:25.0.5.1_2-jre-alpine-3.22@sha256:7ec950978e14058a5a67b4ab3a8e2ed2b736bedd218d6a14d7868f56de11ec2b
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8080 8443
COPY lowkey-vault.jar /lowkey-vault.jar
RUN \
  chmod 555 "/lowkey-vault.jar" && \
  mkdir "/import" && \
  chmod 755 "/import" && \
  mkdir "/config" && \
  chmod 555 "/config"
WORKDIR /
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
