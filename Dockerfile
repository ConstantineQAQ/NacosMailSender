FROM maven:3-openjdk-17-slim

# 安装Redis
RUN apt-get update && \
    apt-get install -y redis-server && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

LABEL maintainer="wangyaning<357208746@qq.com>"

ENV TZ=Asia/Shanghai
ENV WORKPATH mailSender

WORKDIR /${WORKPATH}

COPY . .

RUN mvn clean package -Dmaven.test.skip=true

EXPOSE 6379

ENTRYPOINT ["java", "-jar", "/mailSender/target/mailSender-0.0.1-SNAPSHOT.jar"]