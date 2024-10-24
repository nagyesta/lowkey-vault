FROM eclipse-temurin:17.0.13_11-jre-alpine@sha256:148f1c965d9314fc1207b7719f0eec35a374ad2ccfb8200268b5ac17a25e05fd
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8080 8443
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
