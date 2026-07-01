# EchoStudy 运行说明

## 1. 环境要求

- Windows
- JDK 17
- Maven
- Node.js 与 npm
- SQL Server 与 SSMS

不要安装全局依赖或使用 conda；本项目只使用 Maven 与 npm 的项目本地依赖。

## 2. 数据库说明

- 类型：SQL Server
- 服务器：`localhost,1433`
- 数据库：`echo_study`
- 用户名：`echo_user`
- 密码：`EchoStudy@123456`

后端配置位于 `backend/src/main/resources/application.yml`。

## 3. 执行 schema.sql

在 SSMS 中连接 SQL Server，选择数据库 `echo_study`，打开并执行：

```text
backend/src/main/resources/db/schema.sql
```

脚本会创建核心表、默认账号、2 个自习室、每室 4 行 × 6 列座位、基础时间节点。

## 4. 后端启动

```powershell
cd D:\EchoStudy\backend
mvn spring-boot:run
```

后端默认端口：`http://localhost:8080`

Knife4j / OpenAPI 页面：

```text
http://localhost:8080/doc.html
```

## 5. 前端启动

首次运行安装本地依赖：

```powershell
cd D:\EchoStudy\frontend
npm install
```

启动开发服务：

```powershell
npm run dev
```

前端默认地址：`http://localhost:5173`

## 6. 默认账号密码

- 管理员：admin / 123456
- 学生：student / 123456

如果没有先执行 `schema.sql`，后端启动时的 `DataInitializer` 也会在空表中兜底创建默认账号、自习室、座位和时间节点。

## 7. 常见问题

- 登录失败：确认数据库脚本已执行，或后端启动日志中没有数据库连接错误。
- 前端 401：退出重新登录，确认浏览器 localStorage 中 token 未过期。
- 签到失败：默认演示坐标为 `31.2304160, 121.4737010`，自习室允许半径为 80 米。
- 预约冲突：同一座位或同一用户在同一天有效预约状态下重叠时间会被拒绝。
- AI 任务未执行：定时任务每 1 分钟扫描一次，请等待下一轮日志输出。
