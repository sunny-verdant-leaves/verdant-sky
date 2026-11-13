[English](../../README.md) | 中文

# Verdant Sky

本模组是为 **Verdant Sky** 整合包制作的，但也可以独立使用，或添加到其他空岛整合包中。它添加了一些方块变体以及可配置的修改项。

本模组基于 [Architectury](https://docs.architectury.dev/api) 框架构建，同时支持 Fabric 和 Forge。

## 依赖要求

| 依赖项 | 版本 |
| :--- | :--- |
| Minecraft | 1.20.1 |
| 植物魔法 (Botania) | 1.20.1-453+ |
| Architectury API | 9.1.10+ |

## 使用方法

### 面向玩家

1. 安装适用于 1.20.1 的 Minecraft Forge 或 Fabric。
2. 从 Releases 页面下载最新的 `.jar` 文件。
3. 将本模组、植物魔法 (Botania) 和 Architectury API 的 `.jar` 文件放入你的 `mods` 文件夹。
4. 启动游戏，并在模组配置中启用你想要的修改项。

### 面向开发者

1. 克隆代码仓库。
2. 将项目作为 Gradle 项目导入到你的 IDE 中。
3. 项目结构分为 `common`、`fabric` 和 `forge` 三个模块。
4. 使用 `./gradlew build` 构建模组。最终的发布版 `.jar` 将生成在 `fabric/build/libs` 和 `forge/build/libs` 目录下。
5. 在 IDE 中进行测试时使用 `-dev-shadow.jar`，发布时使用标准的 `-*.jar`。

## 鸣谢

*   **作者**：[Sunny Verdant Leaves](https://github.com/sunny-verdant-leaves)
*   **植物魔法 (Botania)**：由 Vazkii 及贡献者开发

## 许可证

本模组基于 **MIT 许可证** 开源。详情请参见 [LICENSE](LICENSE) 文件。

植物魔法 (Botania) 基于 [Botania 许可证](https://botaniamod.net/license.php) 授权。在使用或修改本附属模组时，请尊重植物魔法的许可证。