FROM eclipse-temurin:11.0.20_8-jre-alpine@sha256:6a7bdc6f82c73a2fa1c95c5fe7465f8dab63a8973d9eb2c9ad6157db75b9c309
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
