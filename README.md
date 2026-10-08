# Verdant Sky

This mod is for the modpack **Verdant Sky**, but it can also be used standalone or added to sky block modpacks. It adds some block variants and configurable modifications.

Built with the [Architectury](https://docs.architectury.dev/api) framework, this mod supports both Fabric and Forge.

## Requirements

| Dependency | Version |
| :--- | :--- |
| Minecraft | 1.20.1 |
| Botania | 1.20.1-453+ |
| Architectury API | 9.2.14+ |

## Usage

### For Players

1. Install Minecraft Forge or Fabric for 1.20.1.
2. Download the latest jar from the Releases page.
3. Place the mod jar, Botania, and Architectury API into your `mods` folder.
4. Launch the game and enable the desired modifications in the mod config.

### For Developers

1. Clone the repository.
2. Import the project into your IDE as a Gradle project.
3. The project is structured into `common`, `fabric`, and `forge` modules.
4. Use `./gradlew build` to build the mod. The final production jars will be generated in `fabric/build/libs` and `forge/build/libs`.
5. Use the `-dev-shadow.jar` for testing in the IDE, and the standard `-*.jar` for publishing.

## Credits

*   **Author**: [Sunny Verdant Leaves](https://github.com/sunny-verdant-leaves)
*   **Botania**: by Vazkii and contributors

## License

This mod is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

Botania is licensed under the [Botania License](https://botaniamod.net/license.php). Please respect Botania's license when using or modifying this addon.