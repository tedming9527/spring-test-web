(() => {
  const page = document.body.dataset.scenario;
  const diagram = document.getElementById("diagram");
  const modeControl = document.getElementById("variable-mode");
  if (!diagram || !modeControl) return;

  const definitions = {
    pom: {
      title: "POM 中的变量替换",
      timing: "Maven 构建项目模型时解析属性。",
      sources: [
        { id: "cli", name: "命令行用户属性", value: "cli-value", defaultOn: false, note: "本次 Maven 命令传入，可覆盖同名 POM 属性。" },
        { id: "child", name: "当前 POM 属性", value: "child-value", defaultOn: true, note: "在当前项目的 properties 中定义，也可以显式引用环境变量。" },
        { id: "parent", name: "父 POM 属性", value: "parent-value", defaultOn: true, note: "可由父 POM 继承到当前项目。" },
        { id: "jvm", name: "Maven JVM 系统属性", value: "jvm-value", defaultOn: false, note: "通过 MAVEN_OPTS 设置给运行 Maven 的 JVM。" },
        { id: "os", name: "操作系统环境变量", value: "env-value", defaultOn: true, note: "仅 env.APP_LABEL 写法会查询这个环境变量。" }
      ]
    },
    filtering: {
      title: "Maven 构建时过滤文本资源",
      timing: "资源复制时替换；本示例假设已对该文本资源启用 filtering。",
      sources: [
        { id: "cli", name: "命令行用户属性", value: "cli-value", defaultOn: false, note: "仅影响本次 Maven 执行，可覆盖同名过滤值。" },
        { id: "jvm", name: "Maven JVM 系统属性", value: "jvm-value", defaultOn: false, note: "通过 MAVEN_OPTS 提供给运行 Maven 的 JVM。" },
        { id: "os", name: "操作系统环境变量", value: "env-value", defaultOn: true, note: "仅 env.APP_LABEL 写法会查询这个环境变量。" },
        { id: "pom", name: "生效 POM 属性", value: "pom-value", defaultOn: true, note: "已合并的当前项目、父 POM 和生效 profile 属性。" },
        { id: "filter", name: "filter 属性文件", value: "filter-value", defaultOn: true, note: "由 build/filters 指定，供启用过滤的任务读取。" }
      ]
    }
  };

  const config = definitions[page];
  if (!config) return;
  const nodes = new Map();

  function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function side(position, title, text, note) {
    const node = element("div", "fish-side " + position);
    node.append(element("span", "bone-title", title));
    if (text) node.append(element("code", "bone-code", text));
    if (note) node.append(element("div", "bone-note", note));
    return node;
  }

  function addIntroRow(mark, left, right) {
    const row = element("div", "fish-row");
    row.append(side("left", ...left));
    row.append(element("span", "fish-center", mark));
    row.append(side("right", ...right));
    diagram.append(row);
    return row;
  }

  addIntroRow("场", ["场景", "", config.title], ["处理时机", "", config.timing]);
  const usageRow = addIntroRow("式", ["使用位置", "", ""], ["查找对象", "", "前缀和属性名决定要查找的键。"]);
  const usage = element("code", "bone-code");
  usageRow.querySelector(".left").append(usage);
  const keyDescription = element("code", "bone-code");
  usageRow.querySelector(".right").append(keyDescription);

  config.sources.forEach((source, index) => {
    const row = element("div", "fish-row");
    const isLeft = index % 2 === 0;
    const choice = element("div", "fish-side " + (isLeft ? "left" : "right"));
    choice.classList.add("fish-choice");
    const label = element("label", "fish-check");
    const input = document.createElement("input");
    input.type = "checkbox";
    input.checked = source.defaultOn;
    input.setAttribute("aria-label", source.name + "已提供");
    label.append(input, element("span", "bone-title", source.name));
    const code = element("code", "bone-code");
    choice.append(label, document.createElement("br"), code, element("div", "bone-note", source.note));
    const detail = side(isLeft ? "right" : "left", "查找结果", "", "");
    detail.classList.add("fish-detail");
    const status = element("div", "status-text");
    const extra = element("div", "bone-note");
    detail.append(status, extra);
    row.append(choice, element("span", "fish-center", String(index + 1)), detail);
    diagram.append(row);
    nodes.set(source.id, { row, input, code, status, extra });
  });

  const end = element("div", "end-row");
  const resultInfo = element("div");
  const value = element("div", "end-value");
  resultInfo.append(element("div", "end-label", page === "pom" ? "本次属性解析结果" : "复制后资源中的值"), value);
  const resultSource = element("div", "end-source");
  end.append(resultInfo, resultSource);
  end.setAttribute("aria-live", "polite");
  diagram.after(end);

  function render() {
    const env = modeControl.value === "env";
    const key = env ? "env.APP_LABEL" : "app.label";
    usage.textContent = page === "pom" ? "<finalName>${" + key + "}</finalName>" : "app.label=@" + key + "@";
    keyDescription.textContent = "${" + key + "}";

    const format = {
      cli: "-D" + key + "=cli-value",
      child: "<" + key + ">child-value</" + key + ">",
      parent: "<" + key + ">parent-value</" + key + ">",
      pom: "<" + key + ">pom-value</" + key + ">",
      jvm: 'MAVEN_OPTS="-D' + key + '=jvm-value"',
      os: "APP_LABEL=env-value",
      filter: key + "=filter-value"
    };

    const available = source => source.id !== "os" || env;
    const chosen = config.sources.find(source => nodes.get(source.id).input.checked && available(source));
    const chosenIndex = chosen ? config.sources.indexOf(chosen) : -1;
    config.sources.forEach((source, index) => {
      const node = nodes.get(source.id);
      const applicable = available(source);
      node.input.disabled = !applicable;
      node.code.textContent = format[source.id];
      const selected = chosen && chosen.id === source.id;
      node.row.classList.toggle("hit", Boolean(selected));
      node.status.textContent = !applicable
        ? "当前写法不匹配"
        : selected
          ? "命中，停止查找"
          : chosenIndex >= 0 && index > chosenIndex
            ? "已在更高优先级命中"
            : "未提供，继续查找";
      if (source.id === "os") {
        node.extra.textContent = env ? "Maven 通过 env.APP_LABEL 读取。" : "APP_LABEL 不会自动作为 app.label 使用。";
      } else if (source.id === "child" || source.id === "pom") {
        node.extra.textContent = env
          ? page === "pom"
            ? "此 POM 插值示例中，同名 POM 属性优先于环境变量。"
            : "此资源过滤示例中，环境变量优先于同名 POM 属性。"
          : "可显式赋值为 ${env.APP_LABEL}，从环境变量取值。";
      } else if (source.id === "filter") {
        node.extra.textContent = "同名键从 filters 指定的文件读取。";
      } else {
        node.extra.textContent = source.note;
      }
    });
    value.textContent = chosen ? chosen.value : page === "pom" ? "未找到属性值" : "占位符未替换";
    resultSource.textContent = chosen ? "来源 " + chosen.name : "请检查属性名及来源";
  }

  modeControl.addEventListener("change", render);
  diagram.addEventListener("change", render);
  render();
})();
