FROM eclipse-temurin:17.0.12_7-jre-alpine@sha256:eb88837e44fe350da382950e7d124bd0231af7590a95191481e22109115a5106
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
