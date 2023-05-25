FROM eclipse-temurin:11.0.19_7-jre-alpine@sha256:64bf563dbb61a98a0bcc8b7eb56019771f56ab09fa493e09f20a6705325c175d
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
