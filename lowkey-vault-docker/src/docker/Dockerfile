FROM mcr.microsoft.com/java/jre-headless:11-zulu-alpine
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["java", "-jar", "/lowkey-vault.jar"]
