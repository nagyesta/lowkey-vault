FROM eclipse-temurin:11.0.19_7-jre-alpine@sha256:b805591946d12bb44f478b376e84fcc85eabe3b93125f3f321d0501bdc7302f5
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
