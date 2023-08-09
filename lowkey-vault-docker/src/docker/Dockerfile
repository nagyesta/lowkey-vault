FROM eclipse-temurin:11.0.20_8-jre-alpine@sha256:35fe43b09ac3d3201b5804aae868291a02d84910c43c72914c33d18a34d26346
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
