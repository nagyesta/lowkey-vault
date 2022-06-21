FROM eclipse-temurin:11.0.15_10-jre-alpine@sha256:6d221e8b13f4840c61635741de7cd81994193a49c5664e5b12d9499d9de2ca26
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
