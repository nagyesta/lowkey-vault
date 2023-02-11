FROM eclipse-temurin:11.0.18_10-jre-alpine@sha256:4edcb9a82ae75460ff836a63d7bf2d601635233130952083f5bc79bf888ea8ad
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
