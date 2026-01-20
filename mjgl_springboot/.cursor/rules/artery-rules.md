---
alwaysApply: true
---

# Artery项目开发规则

## 技术栈规范

### 后端技术栈
- **Spring Boot**: 2.7.18
- **Spring WebMVC**: 5.3.31  
- **Thymeleaf**: 3.0.15
- **Shiro**: 1.13.0
- **MyBatis**: 3.5.10

### 前端技术栈
- **Vue.js**: 2.6.11
- **Thymeleaf**: 3.0.15
- **Artery组件库**: 基于Vue的组件库artery-component-vue

## 代码规范

### Java代码规范
- 使用Java 1.8
- 遵循Spring Boot最佳实践
- 使用MyBatis进行数据访问
- 使用Shiro进行安全认证

### 前端代码规范
- 使用Vue 2.6.11语法
- 遵循Artery组件库的使用规范

### 组件与示例获取规则（必须）
- 组件列表、属性、事件、示例，必须通过 MCP 服务 `artery-mcp-server` 实时获取，不得手写臆造。
- 允许调用的 MCP 接口：
  - list-components-names（获取全部组件名）
  - get-component（组件说明）
  - get-component-props（组件属性）
  - get-component-events（组件事件）
  - get-component-methods (组件方法)
  - get-component-examples（官方示例片段）
- 使用要求：
  - 页面中出现的所有组件名必须在 MCP 返回清单内；若不在，禁止使用。
  - 示例代码以 MCP 返回为准，可按业务改动绑定值与事件，但不得修改组件 API 形态。
  - 文档、代码评审需能溯源：标注组件名称与对应的 MCP 接口返回时间或版本。

## 项目结构规范

### 目录结构

```
example/                         # 示例目录
├── sql/                         # 一键生成
│   ├── MySQL/                   # 其他类型: ABase、ASE、DaMeng、MSSQL、MySQL、ORACLE
│   │   └── book-cms/            # 项目模块
│   │       ├── create/          # 数据库表结构和索引
│   │       │   ├── 02.CT_DEMO0718.sql        # 数据库表结构
│   │       │   └── 03.CI_DEMO0718.sql        # 数据库索引
│   │       └── insert/          # 数据库表初始数据
│   │           └── 2.I_T_Book_DEMO0718.sql   # 数据库表初始数据
├── src/                          # 源代码目录
│   ├── main/                     # 主要源码目录
│   │   ├── docker/               # Docker配置
│   │   │   └── Dockerfile        # 构建镜像的文件,包含构建镜像所需的指令和说明
│   │   ├── java/                 # Java源码包
│   │   │   └── com/thunisoft/    # Java源码包
│   │   │       ├── artery/       # Artery框架相关
│   │   │       │   └── customplugin/    # 自定义Artery控件
│   │   │       │       └── atyslider/   # 滑块控件
│   │   │       │           └── AtySlider.java    # 自定义控件的逻辑类
│   │   │       └── demo/example/ # 示例项目包
│   │   │           ├── controller/       # 控制器层
│   │   │           │   ├── IndexController.java
│   │   │           │   ├── IndexTplController.java
│   │   │           │   └── BookController.java
│   │   │           ├── mapper/           # 数据访问层
│   │   │           │   ├── provider/     # SQL提供者
│   │   │           │   │   └── BookSqlProvider.java
│   │   │           │   └── BookMapper.java
│   │   │           ├── model/            # 实体类
│   │   │           │   └── Book.java     # Pojo类文件
│   │   │           ├── service/          # 服务层
│   │   │           │   └── BookService.java
│   │   │           └── ExampleApplication.java
│   │   └── resources/            # 资源文件
│   │       ├── artery/           # Artery配置目录
│   │       │   ├── classcode/    # 分级代码配置
│   │       │   │   └── classcode.cfg.xml     # 分级代码配置文件
│   │       │   └── dic/          # 数据字典配置
│   │       │       └── group/    # 数据字典组
│   │       │           └── datadic.group.xml # 数据字典库定义配置
│   │       ├── static/           # 静态资源
│   │       │   ├── css/          # 样式文件
│   │       │   │   ├── book.css
│   │       │   │   ├── index-tpl.tpl.css
│   │       │   │   └── index.css
│   │       │   └── js/           # JavaScript文件
│   │       │       ├── book.js
│   │       │       ├── index-tpl.tpl.js
│   │       │       └── index.js
│   │       ├── templates/        # Thymeleaf模板
│   │       │   ├── book.html
│   │       │   ├── index-tpl.tpl.html
│   │       │   └── index.html
│   │       ├── application.yml   # 主配置文件
│   │       ├── application-dev.yml   # 开发环境配置
│   │       └── application-prod.yml  # 生产环境配置
│   └── test/                     # 测试源码目录
│       └── java/                 # 测试Java源码包
│           └── example/          # 测试代码
│               └── ExampleTest.java
├── .gitignore                    # 提交时可忽略哪些文件和目录
├── artery.xml                    # Artery特性文件
├── pom.xml                       # 包含了项目基本信息、项目依赖等
└── README.md                     # 项目说明(项目背景、参与人员、环境要求、使用手册链接、版本日志链接等)
```

### Artery页面文件结构
每个页面应包含：
- JAVA控制器文件(src/main/java/**/controller目录)
- HTML模板文件 (templates目录)
- CSS样式文件 (static/css目录)
- JavaScript逻辑文件 (static/js目录)

## Artery开发规范

### 前端代码规范
- 尽可能地使用Artery组件，可使用artery-mcp-server获取使用方式，不可随意编辑
- 尽可能地使用Artery已经提供的JS方法和CSS样式

## 参考资源

- [Artery 官方文档](https://artery.thunisoft.com)
- [Spring Boot参考指南](https://spring.io/projects/spring-boot)
- [Vue.js官方文档](https://vuejs.org/v2/guide/)
- [Thymeleaf官方文档](https://www.thymeleaf.org/)

---

### Artery组件白名单与使用约束（基于 MCP：artery-mcp-server）
- 尽可能使用以下组件（MCP 官方清单）：
  - 基础展示：aty-text, aty-title, aty-image, aty-icon, aty-link, aty-badge, aty-tag
  - 布局导航：aty-region-layout, aty-float-layout, aty-row, aty-breadcrumb, aty-menu, aty-tabs, aty-steps, aty-dropdown
  - 表单输入：aty-form, aty-input, aty-textarea, aty-select, aty-select-tree, aty-date-picker, aty-time-picker, aty-input-number, aty-input-money, aty-checkbox/aty-checkbox-group, aty-radio/aty-radio-group, aty-switch, aty-slider, aty-upload, aty-auto-complete, aty-transfer
  - 数据展示：aty-table, aty-progress, aty-timeline, aty-tree, aty-pagination
  - 反馈与动效：aty-modal, aty-alert, aty-poptip, aty-tooltip, aty-spin, aty-back-top, aty-divider, aty-affix
  - 其他：aty-iframe, aty-report-preview, aty-report-toolbar, aty-scroll, aty-scrollbar, aty-show-more, aty-tpl-slot, template
- 约束：
  - 所有组件名必须以 aty- 前缀；插槽使用 `aty-tpl-slot`。
  - 表单必须包裹在 `aty-form` 内，通过 `$refs` 获取与校验。
  - 页面骨架优先使用 `aty-region-layout`。
  - 弹窗统一使用 `aty-modal` 或 `Artery.open`。
  - 全部组件（MCP 官方清单）：
  `aty-tpl-slot`, `aty-button`, `aty-icon`, `aty-iframe`, `aty-image`, `aty-link`, `aty-panel`, `aty-row`, `aty-text`, `aty-title`, `aty-captcha-slide`, `aty-captcha-string`, `aty-badge`, `aty-card`, `aty-progress`, `aty-table`, `aty-tag`, `aty-timeline`, `aty-auto-complete`, `aty-cascader`, `aty-checkbox-group`, `aty-checkbox`, `aty-date-picker`, `aty-form`, `aty-input-money`, `aty-input-number`, `aty-input`, `aty-organ-tree`, `aty-radio-group`, `aty-radio`, `aty-rate`, `aty-select-tree`, `aty-select`, `aty-slider`, `aty-switch`, `aty-textarea`, `aty-time-picker`, `aty-transfer`, `aty-upload`, `aty-button-group`, `aty-float-layout`, `aty-region-layout`, `aty-breadcrumb`, `aty-dropdown`, `aty-fix-nav`, `aty-menu`, `aty-steps`, `aty-tabs`, `aty-tree`, `aty-alert`, `aty-poptip`, `aty-tooltip`, `aty-markdown`, `aty-scrollbar`, `aty-scroll`, `aty-show-more`, `aty-pagination`, `aty-modal`, `aty-report-preview`, `aty-report-toolbar`, `template`, `aty-affix`, `aty-back-top`, `aty-color-picker`, `aty-divider`, `aty-spin`.

### Artery 前端 JS 方法白名单（基于 MCP：artery-mcp-server）
- 窗口/弹窗：`Artery.open(config)`, `Artery.close()`
- 全局提示：`Artery.message.{info|success|warning|error|loading}(config)`
- 通知：`Artery.notice.{info|success|warning|error|loading}(config)`
- 加载/进度：`Artery.spin.{show|hide}()`, `Artery.loadingBar.{start|finish|error|update|config|destroy}()`
- 表单（基于 `$refs.formRef`）：`submit({url})`, `validate(cb)`, `getData(noValidate, cb)`, `resetFields()`
- 工具：`Artery.debounce`, `Artery.deepCopy`, `Artery.deepmerge`, `Artery.merge`, `Artery.type`, `Artery.isOneOf`
- DOM/样式：`Artery.findComponentUpward`, `Artery.findBrothersComponents`, `Artery.getStyle`, `Artery.setStyle`, `Artery.on`, `Artery.off`, `Artery.getScrollBarSize`, `Artery.getOffset`, `Artery.getElementRect`, `Artery.stopEvent`
- 其他：`Artery.parseUrl`, `Artery.dateFormat`, `Artery.setScrollBarPosition`, `Artery.loadCode(codeType)`
- 统一约束：仅允许调用白名单内方法，如需新增需在本清单补充并注明来源。

### CSS 工具类白名单（基于 MCP：artery-mcp-server）
- 布局/显示：`aty-fl`, `aty-fr`, `aty-inline`, `aty-block`, `aty-inline-block`, `aty-hide`, `aty-show`, `aty-hidden`, `aty-visible`, `aty-clearfix`
- 溢出/定位：`aty-ovh`, `aty-ovhy`, `aty-ovhx`, `aty-posr`, `aty-posa`, `aty-posf`
- 文本/对齐：`aty-text-ellipsis`, `aty-wwb`, `aty-wsn`, `aty-usn`, `aty-tal`, `aty-tac`, `aty-tar`, `aty-vam|aty-vat|aty-vab`
- 交互/边框：`aty-curp|aty-curd|aty-curm|aty-curh`, `aty-border-default`
- 约束：优先使用上述功能类，不得自造与其冲突的公共工具类名。
 - 全部功能类（MCP 官方清单）：`aty-fl`, `aty-fr`, `aty-clearfix`, `aty-inline`, `aty-block`, `aty-inline-block`, `aty-hide`, `aty-show`, `aty-hidden`, `aty-visible`, `aty-opacity0`, `aty-opacity1`, `aty-ovh`, `aty-ovhy`, `aty-ovhx`, `aty-posr`, `aty-posa`, `aty-posf`, `aty-fwn`, `aty-fwb`, `aty-ti2`, `aty-text-hide`, `aty-text-ellipsis`, `aty-wwb`, `aty-wsn`, `aty-usn`, `aty-tal`, `aty-tac`, `aty-tar`, `aty-vam`, `aty-vat`, `aty-vab`, `aty-curd`, `aty-curp`, `aty-curm`, `aty-curh`, `aty-border-default`。

### Artery注解
#### @Column、@NotColumn
#### @TypeHandler
#### @Corp, @Dept, @User
#### @CodeTyp
单值代码的类型取自工具get-codeTypes-from-db
#### @MultiCodeType
#### @ClassCodeType
#### @TranslateToString
#### @DeepTranslate
#### @DataTranslate
#### @ExceptionTips

#### 翻译机制
- 翻译的使用文档取自工具how-to-translate
- 翻译后的数据会以`[属性名]TranslateText`的格式返回给前端
- @Corp, @Dept, @User,@CodeType,@MultiCodeType,@ClassCodeType,@TranslateToString可将Id翻译为名称，目标：实体类的field
- 在需要显示中文名称的字段上使用相应的翻译注解
- 对于复杂嵌套对象，使用@DeepTranslate确保所有层级都能正确翻译，目标：实体类的class
- 在需要返回翻译后数据的接口上使用@DataTranslate，目标：Controller的class, method
- 前端组件会自动解析此属性并显示为翻译后的值，但aty-text等组件必须使用`[属性名]TranslateText`才能显示翻译后的值


### 权限表达式规范
- 权限字取自工具get-rightKeys-from-db，不能随意编纂
- 权限的使用文档取自工具how-to-use-right
- 在组件上通过 `aty:right` 使用权限表达式，支持 `()`、`|/or`、`&/and`
- 示例：`aty:right="(right.a & right.b) | right.c"`
- 命名建议：以业务域分组，如 `book.add`、`book.edit`、`book.view`

### 图标ICON
- Artery提供的图标取自于工具list-icons，不可随意编纂

### 禁止项
- 禁止自定义与官方冲突的 组件名、CSS 工具类等。
- 禁止直接操作 DOM 改写表单或弹窗行为；
