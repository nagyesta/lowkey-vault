FROM eclipse-temurin:11.0.18_10-jre-alpine@sha256:2d8e2f5e35b70ffada5894eb3e949d27f21197eb3f23dbb7125b6fa06cce3b32
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
