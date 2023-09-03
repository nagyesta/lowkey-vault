FROM eclipse-temurin:17.0.8_7-jre-alpine@sha256:e90e0d654765ab3ae33f5c5155daafa4a907d0d738ce98c3be8f402a8edcee2b
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
