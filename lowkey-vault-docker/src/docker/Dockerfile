FROM eclipse-temurin:11.0.17_8-jre-alpine@sha256:8555d6b7e5e5f75e3ed033877a6778bf4f5531895ab4d22920c42e23d0e3ba87
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
