# Teamup

###### 2022年秋季软件工程第一小组项目后端

## 项目介绍
本项目是一个以大学生**课程组队**为核心的组队交流平台，是一款网页类在线社交平台。该项目的主要思路是为有课程组队需求且有一定实现难度的用户提供一个可以即时发布组队信息和加入队伍的在线快捷平台。

## start up

- 在mysql中用`src\doc\teamupsql.sql`创建后端数据库
- 修改`\src\main\resources\application.yml`中的账号密码等以连接数据库
-  `mvn compile`获取依赖项
-  `mvn install`编译打包
-  `java -jar ./target/TeamUp-0.0.1-SNAPSHOT.jar`开始运行

