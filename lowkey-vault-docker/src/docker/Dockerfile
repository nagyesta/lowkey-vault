FROM --platform=${BUILDPLATFORM} eclipse-temurin:17.0.14_7-jre-alpine@sha256:865cca6d0b31f284a5fc4e8cbd8f9375f470fd07cf6e909cea62f2790d4187fe
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
