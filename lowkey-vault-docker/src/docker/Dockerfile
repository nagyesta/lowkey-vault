FROM eclipse-temurin:11.0.15_10-jre-alpine@sha256:dcd7942517ac38c259af1a9af0601ae138f173616c2f0c08cdc0294027e59c16
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
