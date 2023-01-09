FROM eclipse-temurin:11.0.17_8-jre-alpine@sha256:6c80ae55c67466b3d1efd91feec3be6cc46c6256f59bbeaade74c3246e23bc98
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
