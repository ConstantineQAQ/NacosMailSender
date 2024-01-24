# Nacos配置中心配置更改邮件通知
## 使用方法
1. 下载源码
2. 使用docker build -t xxx .构建镜像
3. 拉取redis镜像 : docker pull redis
4. 创建内部网络 : docker network create mynetwork
5. 启动redis : docker run -d --name redis-container --network mynetwork redis
6. 启动本项目 : docker run -d --name yyy --network mynetwork xxx