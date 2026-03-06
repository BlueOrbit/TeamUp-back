# Teamup

###### 2022年秋季软件工程第一小组项目后端

## 项目介绍
本项目是一个以大学生**课程组队**为核心的组队交流平台，是一款网页类在线社交平台。该项目的主要思路是为有课程组队需求且有一定实现难度的用户提供一个可以即时发布组队信息和加入队伍的在线快捷平台。

## start up

- 在mysql中用`src\doc\teamupsql.sql`创建后端数据库
- 通过环境变量配置数据库连接（推荐）：
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`
-  `mvn compile`获取依赖项
-  `mvn install`编译打包
-  `java -jar ./target/TeamUp-0.0.1-SNAPSHOT.jar`开始运行

## 鉴权说明

- 注册：`POST /users`
- 登录：`POST /login`，返回 `token`
- 除公开接口外，写操作需要请求头：
  - `Authorization: Bearer <token>`

## 前端（新增）

项目已新增 `frontend/` 目录（React + Vite）：

- 进入前端目录：`cd frontend`
- 安装依赖：`npm install`
- 本地运行：`npm run dev`
- 构建：`npm run build`

