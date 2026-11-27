# 知识卡：用四个问题读懂 POM

- 领域：Java / Maven
- 日期：2026-11-27
- 关联课程：项目构建与 Maven 复习

## 一句话核心

读 `pom.xml` 时，依次看项目身份、依赖、构建发布与配置来源，再进入具体字段。

## 四个问题

```text
pom.xml
├── 我是谁？
│   ├── groupId / artifactId / version  项目坐标
│   └── packaging                       产物类型
├── 我用什么？
│   ├── dependencies                    直接依赖声明
│   └── dependencyManagement            依赖版本等管理规则
├── 我怎么构建和发布？
│   ├── build/plugins                   构建插件及任务配置
│   ├── build/resources                 资源复制与过滤
│   └── distributionManagement          Maven 产物发布仓库
└── 配置从哪里来、何时生效？
    ├── parent                          继承父 POM 的配置
    ├── properties                      定义可引用的属性
    ├── repositories / pluginRepositories 查找依赖和插件
    └── profiles                        按条件启用一组配置
```

这四个问题是阅读顺序。同一个字段可能回答多个问题，例如 `parent` 既是配置来源，也可能提供版本基线。

## 项目实例

- 当前项目坐标为 `org.example:spring-test-web:0.0.1-SNAPSHOT`，默认打包类型为 `jar`。
- 项目直接声明 `mysql-connector-j`，scope 是 `runtime`；Spring Boot 的依赖管理提供其版本。
- 项目使用 Spring Boot Maven 插件，资源文件按 Maven 约定放在 `src/main/resources`。
- 项目通过 `parent` 继承 `spring-boot-starter-parent` 的配置。`settings.xml` 则可在项目之外配置仓库镜像、认证和代理。

## 检验尺

拿到一份 POM，能否回答项目坐标、一个依赖的来源与版本、一项构建配置、配置是否由父 POM 或 profile 提供？

## 易错点

- `dependencyManagement` 管理规则，单独声明不会引入依赖。
- `help:effective-pom -Dverbose` 用于查看合并后的 POM 和配置来源；`dependency:tree -Dverbose` 用于查看解析出的依赖路径与被省略的节点。
- 依赖树不等于产物内容。确认 JAR 中的文件应检查构建产物。

## 出处

- 项目配置：[`pom.xml`](../../pom.xml)
- [Maven POM 官方参考](https://maven.apache.org/pom.html)
