FROM eclipse-temurin:17.0.8_7-jre-alpine@sha256:956b8bccdb62e0681010a4127e8dba8b9d3f3247dd4004e11e4bdcec71bb7cb8
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
