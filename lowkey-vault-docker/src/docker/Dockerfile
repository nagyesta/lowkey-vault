FROM eclipse-temurin:17.0.10_7-jre-alpine@sha256:d6d64277f4b88a821e604896f356ebdf1bda846f68d6d8d9c96919384e47d498
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
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
