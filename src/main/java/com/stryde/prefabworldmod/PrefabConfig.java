package com.stryde.prefabworldmod;

import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PrefabConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> FOLDER_NAME = BUILDER
            .comment("Folder that holds prefab world saves, relative to the game/instance directory.")
            .define("folderName", "prefab_saves");

    public static final ModConfigSpec.BooleanValue SHOW_MANAGE_BUTTON = BUILDER
            .comment(
                    "Show the Prefabs button on the title screen.",
                    "Off by default so players never see the manager; pack and map authors turn it on",
                    "while setting up prefabs. Server admins can also use the /prefab commands instead.")
            .define("showManageButton", false);

    public static final ModConfigSpec.BooleanValue OVERWRITE_LEVEL_DAT = BUILDER
            .comment(
                    "Copy the prefab's level.dat into new worlds as well.",
                    "Leave this false to keep the seed, game mode and difficulty chosen when creating the world.",
                    "A copied level.dat is written again by the running server when the world saves.")
            .define("overwriteLevelDat", false);

    public static final ModConfigSpec.BooleanValue COPY_PLAYER_DATA = BUILDER
            .comment(
                    "Copy the prefab's players folder (player inventories, positions, ender chests) into new worlds.",
                    "Leave this false so players start with an empty inventory.")
            .define("copyPlayerData", false);

    public static final ModConfigSpec.BooleanValue COPY_PLAYER_PROGRESS = BUILDER
            .comment(
                    "Copy statistics and advancements (data/stats and data/advancements) from the prefab.",
                    "Leave this false so players start with no progress.")
            .define("copyPlayerProgress", false);

    public static final ModConfigSpec.BooleanValue COPY_ICON = BUILDER
            .comment("Copy the prefab's world icon (icon.png) into new worlds.")
            .define("copyIcon", true);

    public static final ModConfigSpec.BooleanValue COPY_DIMENSIONS = BUILDER
            .comment(
                    "Copy the prefab's dimensions folder (chunks of dimensions added by mods) into new worlds.",
                    "This is what carries a mod dimension's contents, such as a built structure that",
                    "lives in its own dimension.")
            .define("copyDimensions", true);

    public static final ModConfigSpec.BooleanValue COPY_SAVED_DATA = BUILDER
            .comment(
                    "Copy the prefab's data folder (level-wide saved data written by mods) into new worlds.",
                    "Statistics and advancements are controlled separately by copyPlayerProgress.")
            .define("copySavedData", true);

    public static final ModConfigSpec.BooleanValue COPY_DATAPACKS = BUILDER
            .comment(
                    "Copy the prefab's datapacks folder into new worlds.",
                    "Note that a datapack still has to be enabled in the new world's level.dat to load,",
                    "which this mod does not change unless overwriteLevelDat is on.")
            .define("copyDatapacks", true);

    public enum GameModeOption {
        KEEP,
        SURVIVAL,
        CREATIVE,
        ADVENTURE,
        SPECTATOR;

        public GameType toGameType() {
            return switch (this) {
                case SURVIVAL -> GameType.SURVIVAL;
                case CREATIVE -> GameType.CREATIVE;
                case ADVENTURE -> GameType.ADVENTURE;
                case SPECTATOR -> GameType.SPECTATOR;
                case KEEP -> null;
            };
        }
    }

    public enum TriState {
        KEEP,
        ENABLED,
        DISABLED
    }

    public static final ModConfigSpec.EnumValue<GameModeOption> DEFAULT_GAME_MODE = BUILDER
            .comment(
                    "Force a game mode for worlds created from a prefab.",
                    "KEEP uses the game mode picked when creating the world.")
            .defineEnum("defaultGameMode", GameModeOption.KEEP);

    public static final ModConfigSpec.EnumValue<TriState> ALLOW_COMMANDS = BUILDER
            .comment(
                    "Whether new worlds created from a prefab allow commands.",
                    "KEEP uses the choice made when creating the world.")
            .defineEnum("allowCommands", TriState.KEEP);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private PrefabConfig() {
    }

    public static String folderName() {
        return FOLDER_NAME.get();
    }

    public static boolean showManageButton() {
        return SHOW_MANAGE_BUTTON.get();
    }

    public static boolean overwriteLevelDat() {
        return OVERWRITE_LEVEL_DAT.get();
    }

    public static boolean copyPlayerData() {
        return COPY_PLAYER_DATA.get();
    }

    public static boolean copyPlayerProgress() {
        return COPY_PLAYER_PROGRESS.get();
    }

    public static boolean copyIcon() {
        return COPY_ICON.get();
    }

    public static boolean copyDimensions() {
        return COPY_DIMENSIONS.get();
    }

    public static boolean copySavedData() {
        return COPY_SAVED_DATA.get();
    }

    public static boolean copyDatapacks() {
        return COPY_DATAPACKS.get();
    }

    public static GameModeOption defaultGameMode() {
        return DEFAULT_GAME_MODE.get();
    }

    public static TriState allowCommands() {
        return ALLOW_COMMANDS.get();
    }

    public static PrefabManager.CopyOptions copyOptions() {
        return new PrefabManager.CopyOptions(
                overwriteLevelDat(), copyPlayerData(), copyPlayerProgress(), copyIcon(),
                copyDimensions(), copySavedData(), copyDatapacks());
    }
}
