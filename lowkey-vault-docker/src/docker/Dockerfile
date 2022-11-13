FROM eclipse-temurin:11.0.17_8-jre-alpine@sha256:64674afdc275bed87f352eb94f9469842313dd2902afe9d7b0fb9508fc75cc37
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
