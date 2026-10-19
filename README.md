<div align="center">

<img src="docs/logo.png" alt="" width="96">

# Prefab World

**把一张地图放进文件夹，之后新建的每一个世界都会按它自动生成。**<br>
为地图作者、整合包作者和服务器主设计。不需要命令方块，不需要数据包。

[![Modrinth](https://img.shields.io/modrinth/dt/prefabworld?label=Modrinth&color=00AF5C)](https://modrinth.com/mod/prefabworld)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-62B47A)](#兼容性)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.3.0.92-ED8D36)](#兼容性)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-lightgrey)](LICENSE)

**[中文](README.md) | [English](README_EN.md)**

### [⬇ 在 Modrinth 下载](https://modrinth.com/mod/prefabworld)

<img src="docs/manager-screen.png" alt="预制世界管理界面" width="640">

<sub>管理界面：每行显示图标、名称与上次游玩时间，右侧给出「启用 / 删除」或「加入」</sub>

</div>

## 它能解决什么问题

做地图和整合包的人都有一个麻烦：每开一个测试世界、每让玩家开一个新局，都要从零开始搭同一个出生区、同一座起始岛、同一个新手大厅。

Prefab World 把这件事变成一次操作：**把你做好的地图放好，之后所有新世界都从它开始。**

## 怎么用

1. 安装模组（客户端和服务端都需要）
2. 把你准备好的模板地图（一个普通的存档文件夹）放进实例目录下的 `prefab_saves` 文件夹
3. 正常点「新建世界」

新世界就会直接带着你的模板地形和建筑生成。想换开局布局，换掉文件夹里的模板就行。

```text
.minecraft/
└── prefab_saves/
    ├── .active            # 记录当前启用的模板
    ├── 古城/
    │   ├── level.dat
    │   └── region/
    └── 新手岛/
        ├── level.dat
        └── region/
```

放着**一个**模板时会自动启用；放多个时，用管理界面或 `/prefab use <名字>` 选一个。

## 适合谁

| 身份 | 用来做什么 |
|------|-----------|
| 地图作者 | 每次开新游戏，起始区域保持一致，方便测试和发布 |
| 整合包作者 | 让每个新世界都带自定义出生点、新手岛或教程区 |
| 服务器主 | 开服前直接备好大厅、起始地块、活动场地 |

## 功能

| 功能 | 说明 |
|------|------|
| **模板即插即用** | 放一个存档文件夹就能生效，不用写配置 |
| **图形化管理** | 在游戏里把存档加入预制列表，一键启用或删除 |
| **服务器命令** | 专用服务器用 `/prefab` 完成同样的操作 |
| **可选复制内容** | 玩家背包、统计进度、世界图标、`level.dat` 都能单独开关 |
| **世界规则覆盖** | 可强制新世界的游戏模式和是否允许命令 |
| **玩家无感** | 管理入口默认隐藏，普通玩家看不到任何额外界面 |
| **无依赖** | 单独安装即可运行 |

## 管理界面

在配置里把 `showManageButton` 设为 `true`，主界面「多人游戏」下方就会出现「预制世界」按钮。

<div align="center">
<img src="docs/title-screen.png" alt="主界面上的预制世界入口" width="560">
</div>

打开后是一个存档列表：

- 每条显示地图图标、名称、上次游玩时间和存档 ID
- **普通存档**行右侧有「加入」按钮 → 复制为预制
- **预制**行右侧有「启用 / 删除」按钮
- 当前启用的预制名称显示为绿色并标记「启用中」

<div align="center">
<img src="docs/manager-screen.png" alt="预制世界管理界面" width="720">
</div>

## 命令

管理员（权限等级 2）可用：

| 命令 | 说明 |
|------|------|
| `/prefab list` | 列出所有预制，标记当前启用的 |
| `/prefab use <名字>` | 启用指定预制 |
| `/prefab add <存档名>` | 把 `saves/` 里的存档复制为预制 |
| `/prefab remove <名字>` | 删除预制 |

参数支持 Tab 补全，带空格的中文名也能用。典型流程：

```text
/prefab add 古城        # 把存档「古城」加入预制
/prefab use 古城        # 启用它
/prefab list            # 确认当前启用
```

## 配置

配置文件位于 `config/prefabworldmod-local.toml`，也可在游戏内「模组 → Prefab World → 配置」中修改。

| 配置项 | 默认 | 说明 |
|--------|------|------|
| `folderName` | `prefab_saves` | 预制文件夹名称 |
| `showManageButton` | `false` | 是否在主界面显示管理入口 |
| `overwriteLevelDat` | `false` | 是否复制模板的 `level.dat`（世界名、时间等） |
| `copyPlayerData` | `false` | 是否复制 `players/`（背包、坐标、末影箱） |
| `copyPlayerProgress` | `false` | 是否复制统计与进度 |
| `copyIcon` | `true` | 是否复制世界图标 |
| `defaultGameMode` | `KEEP` | 强制游戏模式：`KEEP` / `SURVIVAL` / `CREATIVE` / `ADVENTURE` / `SPECTATOR` |
| `allowCommands` | `KEEP` | 是否允许命令：`KEEP` / `ENABLED` / `DISABLED` |

默认值的设计意图：模板是地图，玩家进去应该是干净的开局，所以玩家数据默认不复制；图标默认复制，新世界会显示同一张预览图；游戏模式和命令默认跟随新建世界时的选择。

## 兼容性

| 版本 | 加载器 | 状态 |
|------|--------|------|
| 26.3 | NeoForge 26.3.0.92+ | ✅ 支持（当前版本） |
| 1.20.1 | Forge | 旧版本，不再更新 |
| 1.18.2 | Forge | 旧版本，不再更新 |

**安装**：安装 Minecraft 26.3 和 NeoForge 26.3.0.92 或更高版本，把 `.jar` 放进 `mods` 文件夹，启动游戏（需要 Java 25）。

> 目前只支持 NeoForge。Fabric 暂无计划——如果你需要，在 Issue 里说，需求够多会加。

## 常见问题

<details>
<summary><b>新世界还是普通地形？</b></summary>

检查模板文件夹是否正好在实例目录下的 `prefab_saves` 里，且里面是一整个存档文件夹，不是压缩包。可以用 `/prefab list` 或管理界面确认是否已启用。

</details>

<details>
<summary><b>多人服务器能用吗？</b></summary>

能。服务端和客户端都要装同一个版本。

</details>

<details>
<summary><b>会覆盖我原来世界的存档吗？</b></summary>

不会。它只影响之后新建的世界，已有世界不受影响。

</details>

<details>
<summary><b>为什么主界面没有「预制世界」按钮？</b></summary>

为了让普通玩家看不到，这个入口默认关闭。在配置里打开 `showManageButton` 即可。

</details>

<details>
<summary><b>玩家进来会带着模板作者的背包吗？</b></summary>

不会，玩家数据默认不复制。需要的话把 `copyPlayerData` 设为 `true`。

</details>

<details>
<summary><b>能只让部分世界用模板吗？</b></summary>

所有新建世界都会按当前启用的模板生成，已有世界不受影响。

</details>

## 从 1.x 升级

- 旧版本的 `preset_world` 文件夹会被自动识别为启用项
- 数据目录仍是 `prefab_saves`，可通过 `folderName` 修改
- 管理入口默认关闭，需要手动开启

## 反馈

在 [Issues](https://github.com/Stryde01/Prefabworld/issues) 提问，中文英文都可以。附上游戏版本和崩溃日志（如果有）会更快定位。

## 许可证

All Rights Reserved，见 [LICENSE](LICENSE)。二次分发或整合进你自己的包里请先开 Issue 联系。

---

<div align="center">
<sub>Made for mapmakers and modpack authors.</sub>
</div>
