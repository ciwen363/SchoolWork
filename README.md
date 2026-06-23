# SchoolWork

黑马点评移动端课程项目，包含 H5 前端页面、Spring Boot 后端服务、MySQL 数据库脚本、Redis 缓存与秒杀相关功能，以及可选的智能客服扩展模块。

## 项目结构

- `hm-dianping`：黑马点评主后端服务。
- `nginx-1.18.0/html/hmdp`：移动端 H5 前端静态页面。
- `nginx-1.18.0/conf/nginx.conf`：本地前端代理配置。
- `consultant`：智能客服扩展模块。
- `hmdp.sql`、`hmdpchat.sql`：数据库初始化脚本。
- `享评.jmx`：接口压测脚本。

## 技术栈

- 后端：Spring Boot、MyBatis-Plus、MySQL、Redis、Redisson
- 前端：Vue、Axios、Element UI、Nginx
- 扩展：LangChain4j、DashScope

## 运行方式

1. 创建 MySQL 数据库并导入 `hmdp.sql`，如需运行智能客服模块再导入 `hmdpchat.sql`。
2. 修改后端配置文件中的 MySQL、Redis 连接信息。
3. 启动主后端：

   ```bash
   cd hm-dianping
   mvn spring-boot:run
   ```

4. 使用 Nginx 或其他静态服务托管 `nginx-1.18.0/html/hmdp`，访问前端页面。
5. 智能客服模块为可选功能，运行前需要配置 Java 17 环境和 `DASHSCOPE_API_KEY`。

## 注意事项

- 不提交真实 token、密码、个人报告或本地 IDE 配置。
- `target/`、`out/`、日志、压缩包和本地运行文件已通过 `.gitignore` 排除。
- 首次运行前请根据本机 MySQL、Redis 环境调整配置。
