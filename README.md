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

- **后端**：Spring Boot 3.5 · Spring Security + JWT · Spring Data JPA · MySQL 8
- **前端**：Vue 3 · Vite · Element Plus · ECharts
- **部署**：Docker / Docker Compose（三容器：MySQL + 后端 + Nginx 前端）

## 快速启动

```bash
# 1. 构建后端 jar
mvn -f pom.xml package -DskipTests

# 2. 一键启动三容器
docker compose up --build -d
```

访问 http://localhost:8081 （前端 8081 / 后端 API 8082 / MySQL 3307）

首次启动自动建表并初始化默认账号（密码均为 `123456`）：

| 账号 | 角色 |
| --- | --- |
| admin | 系统管理员 |
| deptadmin | 科室管理员 |
| doctor | 康复医师 |
| therapist | 康复治疗师 |
| patient | 患者（自动绑定演示病人） |

量表题库种子：`scale_seed.sql`（8 量表 / 73 条目 / 172 选项），系统表结构变化后执行：

```bash
docker cp scale_seed.sql rehab-mysql:/scale_seed.sql
docker exec -i rehab-mysql mysql -uroot -p123456 rehab_system < /scale_seed.sql
```

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
