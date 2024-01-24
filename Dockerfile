FROM maven:3-openjdk-17-slim

LABEL maintainer="wangyaning<357208746@qq.com>"

ENV TZ=Asia/Shanghai
ENV WORKPATH mailSender

WORKDIR /${WORKPATH}

COPY . .

RUN mvn clean package -Dmaven.test.skip=true

ENTRYPOINT ["java", "-jar", "/mailSender/target/mailSender-0.0.1-SNAPSHOT.jar"]