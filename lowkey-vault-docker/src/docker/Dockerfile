FROM eclipse-temurin:11.0.18_10-jre-alpine@sha256:d19c17f59e768549cd3d26f577be73b5e26e652dd66210d91a6738a355aa1dfe
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
