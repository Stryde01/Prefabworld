## Minecraft 26.3 · NeoForge 26.3.0.92

从 Forge 1.20.1 移植到 NeoForge 26.3，需要 Java 25。

### 新增

- **游戏内管理界面**：主界面「多人游戏」下方可打开预制世界列表，把存档一键加入预制，启用或删除预制
- **命令**：`/prefab list|use|add|remove`，支持专用服务器，参数可 Tab 补全，支持带空格的中文名
- **配置项**：
  - `showManageButton` — 管理入口开关，默认关闭
  - `copyPlayerData` — 是否复制玩家背包、坐标、末影箱
  - `copyPlayerProgress` — 是否复制统计与进度
  - `copyIcon` — 是否复制世界图标
  - `defaultGameMode` — 强制新世界游戏模式
  - `allowCommands` — 强制新世界是否允许命令
- **游戏内配置界面**：在「模组」菜单里直接修改配置

### 变更

- 管理入口默认隐藏，普通玩家看不到，专为整合包和地图作者设计
- 预制目录仍为 `prefab_saves`；旧的 `preset_world` 会自动识别为启用项
- 不再把第一个预制文件夹重命名为 `preset_world`，改为在 `.active` 文件中记录启用项
- 复制时机改到世界加载之前，避免与运行中的服务器争抢存档文件
- 复制时跳过 `session.lock`
- 默认不复制玩家的背包和进度，让玩家从干净的开局开始

### 修复

- 复制存档时目标目录已存在导致子目录内容复制失败
- `logoFile` 已弃用的警告，改用 `iconFile` / `bannerFile`

### 兼容性

- Minecraft 26.3
- NeoForge 26.3.0.92 或更高
- Java 25
