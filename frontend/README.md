# TeamUp Frontend

本目录是 TeamUp 的前端工程，基于 **React + TypeScript + Vite**。

## 功能

- 用户注册、登录、退出
- 队伍广场：查看队伍列表、关键字搜索、查看详情
- 队伍详情：发表评论、提交入队申请、队长审批申请
- 创建队伍
- 个人中心：查看个人信息、我的申请、我的评论

## 启动方式

1. 安装依赖

```bash
npm install
```

2. 配置后端地址

```bash
cp .env.example .env
```

默认后端地址为 `http://localhost:8080`。

3. 本地开发运行

```bash
npm run dev
```

4. 生产构建

```bash
npm run build
```
