FROM eclipse-temurin:11.0.20_8-jre-alpine@sha256:68df9349f93a23d582d9a48c9050a9df637cd973f8fad3243ec8e8e2f4362fa3
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
