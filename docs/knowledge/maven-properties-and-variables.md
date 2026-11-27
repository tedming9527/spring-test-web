# 知识卡：Maven 属性从哪里来

- 领域：Java / Maven
- 日期：2026-11-27
- 关联课程：POM、资源过滤与变量专题
- 适用范围：项目当前的 Maven 3.9.12

## 一句话核心

先看谁提供属性、在哪个阶段读取，再判断占位符会得到什么值。

## POM 属性来源

POM 中的 `${...}` 可以引用多种来源：

| 来源 | 示例 | 值从哪里来 |
|---|---|---|
| 项目模型 | `${project.version}`、`${project.basedir}` | 当前项目坐标或路径等信息 |
| POM 属性 | `${mysql.version}` | 当前 POM、父 POM 或已激活 profile 的 `properties` |
| 命令行用户属性 | `${app.name}` | 本次 Maven 命令传入的 `-Dapp.name=demo` |
| Maven 内置信息 | `${maven.version}`、`${maven.build.timestamp}` | 当前 Maven 版本、构建开始时间等 |
| Java 系统属性 | `${java.home}`、`${user.home}` | 运行 Maven 的 JVM |
| 操作系统环境变量 | `${env.DB_HOST}` | Maven 进程读取到的环境变量 |
| Maven 设置 | `${settings.localRepository}` | 生效的 Maven 设置；部分值在插件参数配置时解析 |

`maven.home`、`maven.build.version`、`maven.repo.local` 等也是 Maven 提供的信息。具体属性名可以由项目、插件或运行环境扩展。

## 项目实例：MySQL 驱动版本

当前项目直接声明 MySQL 驱动，未写版本。Spring Boot 父 POM 的依赖管理规则引用 `${mysql.version}`；当前本地 Spring Boot 4.1.0 POM 中，该属性定义为 `9.7.0`。Maven 插值后，effective POM 显示 `9.7.0`。

```text
当前项目直接声明驱动
    → Spring Boot 版本管理引用 ${mysql.version}
    → 查找生效属性值
    → 得到本次解析使用的版本
```

`./mvnw help:effective-pom -Dverbose` 可看最终配置及元素来源。版本行的注释可能指向原 POM 中的属性引用行，需要再看原文和属性定义，才能追出值的完整来源。

## 命令行属性与同名

`-D` 是 Maven 的命令行选项，用于传入本次执行的用户属性。例如 `-Dmysql.version=9.6.0` 可临时提供同名属性值，适用于本项目继承的版本管理配置。

插件也可以读取用户属性。`dependency:tree -Dincludes=com.mysql:mysql-connector-j` 中的 `includes` 是依赖树任务使用的筛选参数。命令行用户属性的名字可能与 POM 中引用的属性同名，项目自定义属性宜使用明确的前缀，例如 `spring-test-web.build-label`。

## 资源过滤与运行时配置

- `build/filters` 可以指定属性文件，作为 Maven 资源过滤的值来源。`filtering` 启用后，Maven 在构建时复制文本资源并替换占位符；源文件保持原样。不要对图片、证书或 JAR 等二进制资源做文本过滤。
- 本项目继承 Spring Boot 父 POM，默认的 Maven 资源过滤分隔符使用 `@属性名@`，例如 `@project.version@`。Spring 配置常用 `${属性名}`；具体行为仍以生效插件配置为准。
- 写入产物的构建时值在该次构建中确定。Spring 在应用启动时读取的外部配置是另一处读取时机。

## 检验尺

看到一个占位符时，先确认它位于 POM、被过滤的资源还是 Spring 运行时配置；再确认读取者、属性来源、读取时机和实际生效值。

## 易错点

- `-D` 是传属性的选项，`-Dincludes` 的意义由读取它的插件任务决定。
- 操作系统环境变量 `${env.HOME}` 和 JVM 系统属性 `${user.home}` 是不同来源。
- `<filters>` 文件用于资源过滤，不能当作 POM 所有位置的通用属性来源。
- Profile 激活发生在普通 POM 插值之前，激活条件不能假定能读取所有 POM 属性。

## 出处

- 项目配置：[`pom.xml`](../../pom.xml)
- [Maven 3.9.12 模型插值](https://maven.apache.org/ref/3.9.12/maven-model-builder/#model-interpolation)
- [Maven 资源过滤](https://maven.apache.org/guides/mini/guide-filtering-resources.html)
- [Spring Boot 配置与构建时属性展开](https://docs.spring.io/spring-boot/how-to/properties-and-configuration.html)
