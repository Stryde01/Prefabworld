package com.stryde.prefabworldmod;

import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@EventBusSubscriber(modid = prefabworldmod.MODID)
public class WorldEventHandler {

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Path prefabFolder;
        try {
            prefabFolder = PrefabManager.ensureFolder(FMLPaths.GAMEDIR.get().resolve(PrefabConfig.folderName()));
        } catch (IOException e) {
            prefabworldmod.LOGGER.error("Failed to create the prefab folder", e);
            return;
        }

        MinecraftServer server = event.getServer();
        // Only a world that has never picked a spawn position is new; see MinecraftServer#createLevels.
        // The check runs before loadLevel(), so the world folder is still empty apart from level.dat.
        if (server.getWorldData().overworldData().isInitialized()) {
            return;
        }

        Path active;
        try {
            active = PrefabManager.resolveActive(prefabFolder);
        } catch (IOException e) {
            prefabworldmod.LOGGER.error("Failed to read the active prefab", e);
            return;
        }
        if (active == null) {
            prefabworldmod.LOGGER.info("No prefab is active; {} candidate(s) in {}",
                    listCount(prefabFolder), prefabFolder.toAbsolutePath());
            return;
        }

        try {
            PrefabManager.apply(active, server.getWorldPath(LevelResource.ROOT), PrefabConfig.copyOptions());
            prefabworldmod.LOGGER.info("Applied prefab '{}' to the new world", active.getFileName());
        } catch (IOException e) {
            prefabworldmod.LOGGER.error("Failed to apply the active prefab", e);
            return;
        }

        applyWorldOverrides(server.getWorldData());
    }

    /** Applies the optional game mode and command overrides to the freshly created world. */
    private static void applyWorldOverrides(WorldData worldData) {
        GameType gameType = PrefabConfig.defaultGameMode().toGameType();
        if (gameType != null) {
            worldData.setGameType(gameType);
            prefabworldmod.LOGGER.info("Set game mode to {}", gameType.getName());
        }
        switch (PrefabConfig.allowCommands()) {
            case ENABLED -> {
                worldData.setAllowCommands(true);
                prefabworldmod.LOGGER.info("Enabled commands");
            }
            case DISABLED -> {
                worldData.setAllowCommands(false);
                prefabworldmod.LOGGER.info("Disabled commands");
            }
            case KEEP -> {
            }
        }
    }

    private static int listCount(Path prefabFolder) {
        try {
            return PrefabManager.listPrefabs(prefabFolder).size();
        } catch (IOException e) {
            return 0;
        }
    }
}
