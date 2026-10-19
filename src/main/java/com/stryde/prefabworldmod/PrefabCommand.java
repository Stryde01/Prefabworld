package com.stryde.prefabworldmod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Admin commands for servers that have no access to the in-game manager screen.
 * Works on both the integrated server and a dedicated server.
 */
@EventBusSubscriber(modid = prefabworldmod.MODID)
public final class PrefabCommand {
    private static final String SAVES_DIR = "saves";

    private PrefabCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("prefab")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("list").executes(context -> list(context.getSource())))
                        .then(Commands.literal("use")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(prefabNames(), builder))
                                        .executes(context -> use(context.getSource(),
                                                StringArgumentType.getString(context, "name")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("save", StringArgumentType.greedyString())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(saveNames(), builder))
                                        .executes(context -> add(context.getSource(),
                                                StringArgumentType.getString(context, "save")))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(prefabNames(), builder))
                                        .executes(context -> remove(context.getSource(),
                                                StringArgumentType.getString(context, "name"))))));
    }

    private static int list(CommandSourceStack source) {
        Path prefabFolder = prefabFolder();
        try {
            PrefabManager.ensureFolder(prefabFolder);
            List<String> names = PrefabManager.listPrefabs(prefabFolder);
            String active = PrefabManager.readActive(prefabFolder);
            if (names.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("prefabworldmod.command.list.empty"), false);
                return 0;
            }
            source.sendSuccess(() -> Component.translatable("prefabworldmod.command.list.header", names.size()), false);
            for (String name : names) {
                boolean isActive = name.equals(active);
                source.sendSuccess(() -> isActive
                        ? Component.translatable("prefabworldmod.command.list.active", name)
                        : Component.translatable("prefabworldmod.command.list.item", name), false);
            }
            if (active == null) {
                source.sendSuccess(() -> Component.translatable("prefabworldmod.command.list.none"),
                        false);
            }
            return names.size();
        } catch (Exception e) {
            prefabworldmod.LOGGER.error("Failed to list prefabs", e);
            source.sendFailure(Component.translatable("prefabworldmod.command.failed"));
            return 0;
        }
    }

    private static int use(CommandSourceStack source, String name) {
        Path prefabFolder = prefabFolder();
        try {
            if (!Files.isDirectory(prefabFolder.resolve(name))) {
                source.sendFailure(Component.translatable("prefabworldmod.command.missing", name));
                return 0;
            }
            PrefabManager.activate(prefabFolder, name);
            source.sendSuccess(() -> Component.translatable("prefabworldmod.command.activated", name), true);
            return 1;
        } catch (Exception e) {
            prefabworldmod.LOGGER.error("Failed to activate prefab '{}'", name, e);
            source.sendFailure(Component.translatable("prefabworldmod.command.failed"));
            return 0;
        }
    }

    private static int add(CommandSourceStack source, String saveName) {
        Path prefabFolder = prefabFolder();
        Path savePath = gameDir().resolve(SAVES_DIR).resolve(saveName);
        if (!Files.isDirectory(savePath)) {
            source.sendFailure(Component.translatable("prefabworldmod.command.saveMissing", saveName));
            return 0;
        }
        try {
            boolean overwrite = Files.isDirectory(prefabFolder.resolve(saveName));
            PrefabManager.addPrefab(savePath, prefabFolder, saveName, overwrite);
            source.sendSuccess(() -> Component.translatable("prefabworldmod.command.added", saveName), true);
            return 1;
        } catch (Exception e) {
            prefabworldmod.LOGGER.error("Failed to add prefab '{}'", saveName, e);
            source.sendFailure(Component.translatable("prefabworldmod.command.failed"));
            return 0;
        }
    }

    private static int remove(CommandSourceStack source, String name) {
        Path prefabFolder = prefabFolder();
        try {
            if (!Files.isDirectory(prefabFolder.resolve(name))) {
                source.sendFailure(Component.translatable("prefabworldmod.command.missing", name));
                return 0;
            }
            PrefabManager.removePrefab(prefabFolder, name);
            source.sendSuccess(() -> Component.translatable("prefabworldmod.command.removed", name), true);
            return 1;
        } catch (Exception e) {
            prefabworldmod.LOGGER.error("Failed to remove prefab '{}'", name, e);
            source.sendFailure(Component.translatable("prefabworldmod.command.failed"));
            return 0;
        }
    }

    private static Path gameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    private static Path prefabFolder() {
        return gameDir().resolve(PrefabConfig.folderName());
    }

    private static List<String> prefabNames() {
        try {
            return PrefabManager.listPrefabs(prefabFolder());
        } catch (Exception e) {
            return List.of();
        }
    }

    private static List<String> saveNames() {
        Path savesDir = gameDir().resolve(SAVES_DIR);
        if (!Files.isDirectory(savesDir)) {
            return List.of();
        }
        try {
            LevelStorageSource source = LevelStorageSource.createDefault(savesDir);
            return source.loadLevelSummaries(source.findLevelCandidates()).join().stream()
                    .map(LevelSummary::getLevelId)
                    .toList();
        } catch (Exception e) {
            prefabworldmod.LOGGER.warn("Failed to read save names", e);
            return List.of();
        }
    }
}
