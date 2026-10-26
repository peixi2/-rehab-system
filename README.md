# 康复评估过程管理系统

面向医院康复科的评估-治疗全流程管理平台：覆盖**评估申请 → 量表执行 → 自动算分 → 评估记录 → 康复趋势分析**全过程，含患者自助门户。

## 功能模块

| 模块 | 说明 |
| --- | --- |
| 权限体系 | 5 类角色（管理员 / 科室管理员 / 医师 / 治疗师 / 患者），JWT 认证 + 方法级授权 + AOP 操作审计日志 |
| 量表引擎 | 题库化建模 8 种常用康复量表（73 条目 / 172 选项），单选/多选/数值/文本四类题型，后端权威自动算分 + 分级规则判定 |
| 评估任务工作流 | 医师申请 → 治疗师执行（在线填表）→ 自动生成评估记录；支持驳回、编辑、删除 |
| 记录管理 | 评估记录 / 治疗记录增删改查，患者身份强制数据过滤 |
| 康复趋势分析 | ECharts 可视化趋势图 |
| 患者门户 | 床位查询、本人评估/治疗记录只读查看 |
| 基础数据 | 病人 / 医生 / 床位（分配与释放）/ 治疗项目 / 账号管理 |

## 技术栈

- **后端**：Java 21 · Spring Boot 3.5 · Spring Security + JWT · Spring Data JPA · MySQL 8
- **前端**：Vue 3 · Vite · Element Plus · ECharts
- **部署**：Docker / Docker Compose（三容器：MySQL + 后端 + Nginx 前端）

## 快速启动

**仅需安装 Docker**，一条命令完成构建与启动（后端在容器内用 Maven 编译，无需本地 JDK/Maven）：

```bash
docker compose up --build -d
```

访问 http://localhost:8081 （前端 8081 / 后端 API 8082 / MySQL 3307）

> 首次构建需联网拉取依赖；国内网络若拉取镜像缓慢，可配置 Docker 镜像加速器（如 docker.1ms.run）。

首次启动自动建表并初始化默认账号（密码均为 `123456`）：

| 账号 | 角色 |
| --- | --- |
| admin | 系统管理员 |
| deptadmin | 科室管理员 |
| doctor | 康复医师 |
| therapist | 康复治疗师 |
| patient | 患者（自动绑定演示病人） |

量表题库种子：`scale_seed.sql`（8 量表 / 73 条目 / 172 选项）。**首次启动后需手动导入演示数据**（基础数据 + 量表题库），否则系统为空：

```bash
# 基础数据（医生 / 病人 / 床位 / 治疗项目 / 演示记录）
docker cp seed.sql rehab-mysql:/seed.sql
docker exec rehab-mysql sh -c "mysql -uroot -p123456 --default-character-set=utf8mb4 rehab_system < /seed.sql"

# 量表题库（含条目 / 选项 / 计分 / 分级规则）
docker cp scale_seed.sql rehab-mysql:/scale_seed.sql
docker exec rehab-mysql sh -c "mysql -uroot -p123456 --default-character-set=utf8mb4 rehab_system < /scale_seed.sql"
```

> 系统表结构变化后：重启 backend 自动更新表结构，再重新执行一次上述导入即可。

## Render 免费演示部署

仓库根目录的 `render.yaml` 会创建一个 Docker Web Service。生产镜像先构建 Vue，
再把静态文件打进 Spring Boot，因此前端、API 和 HTTPS 地址都由同一个 Render 服务提供。

应用优先使用独立的云 MySQL。首次连接空数据库时，会自动建表、导入 `seed.sql` 与
`scale_seed.sql`，并写入 `demo-v1` 标记；重启或重新部署不会再次清空和导入数据。Render
默认启用 `DB_FALLBACK_EMBEDDED`：云 MySQL 休眠或不可达时自动使用 H2 演示库，保证面试页面、
五个演示账号和种子数据仍可访问。外部数据库恢复后，下次实例启动会重新优先连接它。

在 Render 选择 **New > Blueprint** 并连接本仓库，然后填写：

| 环境变量 | 说明 |
| --- | --- |
| `DB_URL` | JDBC MySQL 地址，建议包含 `sslMode=REQUIRED&createDatabaseIfNotExist=true` |
| `DB_USERNAME` / `DB_PASSWORD` | 云 MySQL 账号与密码 |
| `DEMO_PASSWORD` | 五个演示账号共用的强密码，不要提交到 GitHub |
| `DB_FALLBACK_EMBEDDED` | 云 MySQL 不可用时启用内置演示库，Blueprint 默认为 `true` |

`JWT_SECRET` 由 Render 自动生成。Aiven MySQL 的示例 URL：

```text
jdbc:mysql://HOST:PORT/rehab_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&sslMode=REQUIRED&createDatabaseIfNotExist=true
```

部署后检查：

```text
https://rehab-system-demo.onrender.com/health
```

返回 `{"application":"ok","database":"ok"}` 表示应用与当前数据库均正常。使用内置演示库时，
故障期间的临时修改不会同步回 MySQL，容器替换后可能重置；免费服务闲置后仍需等待冷启动。

## 项目结构

```
├── src/                 # Spring Boot 后端
│   └── main/java/com/rehab/system
│       ├── controller/  # REST 接口（40+）
│       ├── service/     # 业务逻辑（量表算分、任务流）
│       ├── entity/      # JPA 实体（12 张表）
│       ├── security/    # JWT 认证
│       └── config/      # 安全配置、数据初始化
├── frontend/            # Vue 3 前端（Nginx 部署）
├── scale_seed.sql       # 量表题库种子数据
└── docker-compose.yml   # 三容器编排
```
