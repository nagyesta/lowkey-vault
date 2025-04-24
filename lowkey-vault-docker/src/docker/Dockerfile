FROM --platform=${BUILDPLATFORM} eclipse-temurin:17.0.15_6-jre-alpine@sha256:b10e4fda9d71b3819a91fbb0dbb28512edbb37a45f6af2a301c780223bb42fb8
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8080 8443
COPY lowkey-vault.jar /lowkey-vault.jar
RUN \
  addgroup -S lowkey && adduser -S lowkey -G lowkey && \
  chown -R lowkey:lowkey "/lowkey-vault.jar" && \
  chmod 555 "/lowkey-vault.jar" && \
  mkdir "/import" && \
  chown -R lowkey:lowkey "/import" && \
  chmod 755 "/import" && \
  mkdir "/config" && \
  chown -R lowkey:lowkey "/config" && \
  chmod 555 "/config"
USER lowkey
WORKDIR /
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
