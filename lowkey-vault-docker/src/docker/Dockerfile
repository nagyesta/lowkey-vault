FROM --platform=${BUILDPLATFORM} eclipse-temurin:25.0.5_8-jre-alpine-3.22@sha256:5d3da18e5f3d0387a531888efd722b5d58e2967495fad17c7b6fba86abca03a3
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
