FROM eclipse-temurin:21-jre-alpine@sha256:865e2b43cf362d2fc278f96ccbdd9d6c602d9b5855bb9bb233887f1f51353282
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN addgroup -S lowkey && adduser -S lowkey -G lowkey
RUN chown -R lowkey:lowkey "/lowkey-vault.jar"
RUN chmod 555 "/lowkey-vault.jar"
RUN mkdir "/import"
RUN chown -R lowkey:lowkey "/import"
RUN chmod 755 "/import"
RUN mkdir "/config"
RUN chown -R lowkey:lowkey "/config"
RUN chmod 555 "/config"
USER lowkey
WORKDIR /
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
