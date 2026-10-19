# PrefabWorld

为 Minecraft **26.3 (NeoForge)** 编写的模组：把一个世界存档设为"预制地图"，之后新建的每一个世界都会以它为基础生成。

适合**整合包作者**和**地图作者**使用——玩家正常游玩时不会看到任何额外界面。

## 功能

- **预制世界**：把任意存档加入预制列表，新建世界时自动复制其地形、建筑、结构等内容
- **游戏内管理界面**：在主界面启用后，可以添加、启用、删除预制世界
- **命令支持**：专用服务器可用 `/prefab` 命令完成同样的操作
- **可选复制内容**：玩家数据、统计与进度、世界图标都可以独立开关
- **世界规则覆盖**：可强制新建世界的游戏模式和是否允许命令
- **默认隐藏**：管理入口默认关闭，玩家看不到

## 安装

1. 安装 Minecraft 26.3 和 NeoForge 26.3.0.92 或更高版本
2. 把 `prefabworldmod-2.0.0.jar` 放入 `mods` 文件夹
3. 需要 Java 25

## 使用

### 直接放文件夹

首次启动后，游戏目录下会生成 `prefab_saves` 文件夹。把想作为模板的世界存档**整个文件夹**复制进去，然后：

- 如果里面**只有一个**预制文件夹，它会自动启用
- 如果里面有**多个**，用管理界面或 `/prefab use <名字>` 选择一个

之后新建的每个世界都会以该预制地图为基础。

### 管理界面

在配置里把 `showManageButton` 设为 `true` 后，主界面「多人游戏」下方会出现「预制世界」按钮。打开后是一个存档列表：

- 每条显示地图图标、名称、上次游玩时间和存档 ID
- **普通存档**行右侧有「加入」按钮 → 复制为预制
- **预制**行右侧有「启用 / 删除」按钮
- 当前启用的预制名称显示为绿色并标记「启用中」

### 命令（管理员）

| 命令 | 说明 |
|---|---|
| `/prefab list` | 列出所有预制，标记当前启用的 |
| `/prefab use <名字>` | 启用指定预制 |
| `/prefab add <存档名>` | 把 `saves/` 里的存档复制为预制（同名覆盖） |
| `/prefab remove <名字>` | 删除预制（无法撤销） |

需要权限等级 2（OP）。参数支持 Tab 补全，带空格的中文名也能用。

## 配置

配置文件位于 `config/prefabworldmod-local.toml`，也可在游戏内「模组 → PrefabWorld → 配置」中修改。

| 配置项 | 默认 | 说明 |
|---|---|---|
| `folderName` | `prefab_saves` | 预制文件夹名称（相对游戏目录） |
| `showManageButton` | `false` | 是否在主界面显示管理入口 |
| `overwriteLevelDat` | `false` | 是否同时复制预制的 `level.dat`（种子、难度等） |
| `copyPlayerData` | `false` | 是否复制 `players/`（背包、坐标、末影箱） |
| `copyPlayerProgress` | `false` | 是否复制统计与进度（`data/stats`、`data/advancements`） |
| `copyIcon` | `true` | 是否复制世界图标 |
| `defaultGameMode` | `KEEP` | 强制游戏模式：`KEEP`/`SURVIVAL`/`CREATIVE`/`ADVENTURE`/`SPECTATOR` |
| `allowCommands` | `KEEP` | 是否允许命令：`KEEP`/`ENABLED`/`DISABLED` |

**默认值的设计意图**：预制图作为地图模板，玩家进入时应该是干净的开局，所以玩家数据和进度默认不复制；图标默认复制，这样新世界会显示与预制相同的预览图；游戏模式和命令默认跟随新建世界时的选择。

> 注意：复制 `level.dat` 会让新建世界沿用预制世界的地图名称、时间和上次游玩信息。运行中的服务器会在保存时重写 `level.dat`，所以只有新建那一刻生效。

## 工作原理

新建世界时，NeoForge 触发 `ServerAboutToStartEvent`，此时世界目录已经确定但尚未加载。模组在该时刻：

1. 读取 `prefab_saves/.active` 找到当前启用的预制
2. 按配置项把预制内容复制进世界目录（跳过 `session.lock`）
3. 可选地覆盖游戏模式和命令开关

已存在的世界不会触发复制——通过 `worldData.overworldData().isInitialized()` 判断。

## 从 1.x 升级

- 旧版本的 `preset_world` 文件夹会被自动识别为启用项
- 数据目录仍是 `prefab_saves`，配置项 `folderName` 可改
- 管理入口默认关闭，需要手动开启

## 构建

```bash
./gradlew build
```

产物在 `build/libs/`。需要 JDK 25。

构建依赖 `maven.neoforged.net`，中国大陆网络下可能需要代理。

## 许可

MIT，见 [LICENSE](LICENSE)。原项目由 jiangbai 开发。
