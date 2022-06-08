FROM eclipse-temurin:11.0.15_10-jre-alpine@sha256:e75a0b1ce5035e68c93e085eb6bcee0c1617c62c2090b98ad6ddbdc94d0bdc85
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
