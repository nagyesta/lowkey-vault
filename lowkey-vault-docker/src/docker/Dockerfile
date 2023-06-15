FROM eclipse-temurin:11.0.19_7-jre-alpine@sha256:1a6531a7239bea3739d89d8833b03c7732f51e08ecbb78bbc40ef98287c1212d
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
