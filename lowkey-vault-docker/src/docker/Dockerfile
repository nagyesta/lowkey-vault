FROM eclipse-temurin:11.0.17_8-jre-alpine@sha256:c99492a6cf649269c2e39f468fa64dacca233e362059ce1deb218c56e51969a0
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
