# 剧本杀预约管理系统

基于 SpringBoot + Vue3 的剧本杀预约管理系统，支持玩家、主持人、管理员三种角色。

## 技术栈

### 后端
- Spring Boot 2.7.14
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- JWT 认证
- Druid 连接池

### 前端
- Vue 3.3.4
- Vite 5.0.8
- Element Plus 2.4.4
- Vue Router 4.2.5
- Pinia 2.1.7
- Axios 1.6.2

## 项目结构

```
ScriptKill/
├── database/           # 数据库脚本
│   └── script_kill.sql
├── backend/           # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/com/scriptkill/
│   │       │   ├── common/      # 通用类
│   │       │   ├── config/      # 配置类
│   │       │   ├── controller/  # 控制器
│   │       │   ├── dto/         # 数据传输对象
│   │       │   ├── entity/      # 实体类
│   │       │   ├── mapper/      # Mapper接口
│   │       │   └── service/     # 服务层
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
└── frontend/          # 前端项目
    ├── src/
    │   ├── api/       # API接口
    │   ├── layouts/   # 布局组件
    │   ├── router/    # 路由配置
    │   ├── store/     # 状态管理
    │   ├── utils/     # 工具类
    │   ├── views/     # 页面组件
    │   └── style/     # 样式文件
    ├── package.json
    └── vite.config.js
```

## 功能模块

### 1. 用户管理模块
- 玩家注册/登录
- 主持人/管理员登录
- 个人信息管理
- 密码修改
- 头像上传
- 管理员用户管理

### 2. 剧本管理模块
- 剧本增删改查
- 剧本上架/下架
- 剧本筛选（类型、难度、人数、价格）
- 剧本详情查看

### 3. 场次管理模块
- 场次创建与管理
- 场次预约
- 库存管理
- 场次状态管理

### 4. 预约功能模块
- 场次预约
- 订单管理
- 支付功能（微信/支付宝模拟）
- 订单状态跟踪

### 5. 拼场功能模块
- 发起拼场
- 加入拼场
- AI智能匹配推荐
- 拼场状态管理

### 6. 评价系统模块
- 剧本评价
- 主持人评价
- 评价管理
- 评分统计

### 7. 数据统计模块
- 玩家数据统计
- 主持人数据统计
- 管理员数据大屏
- 可视化图表

## 快速开始

### 1. 数据库配置

1. 创建 MySQL 数据库
2. 执行 `database/script_kill.sql` 脚本
3. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息

### 2. 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8088` 启动

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

## 默认账号

- 管理员：`admin` / `admin123` (MD5: 0192023a7bbd73250516f069df18b500)
- 主持人：`host001` / `host123` (MD5: 5f4dcc3b5aa765d61d8327deb882cf99)
- 其他账号密码是123456

## 注意事项

1. 数据库密码默认使用 MD5 加密
2. 支付功能为模拟实现

## 开发说明

### 后端开发
- 使用 MyBatis Plus 进行数据库操作
- 使用 JWT 进行身份认证
- 统一使用 Result 类返回结果
- 使用拦截器进行权限控制

### 前端开发
- 使用 Vue 3 Composition API
- 使用 Pinia 进行状态管理
- 使用 Element Plus 组件库
- 使用 Axios 进行 HTTP 请求
