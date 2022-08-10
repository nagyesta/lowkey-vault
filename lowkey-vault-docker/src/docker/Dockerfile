FROM eclipse-temurin:11.0.15_10-jre-alpine@sha256:cd5d4a9b71d59707fae0f5643b504aaa16768b67f414c12eda7bca8d5819821d
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
