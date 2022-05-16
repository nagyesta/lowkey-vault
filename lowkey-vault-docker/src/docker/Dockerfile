FROM eclipse-temurin:11.0.15_10-jre-alpine@sha256:37c5e98456f7a9a36d4e385a2d068963b82c1c8e2d4943610d93d13c5f65324a
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
