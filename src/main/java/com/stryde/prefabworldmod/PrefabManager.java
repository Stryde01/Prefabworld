package com.stryde.prefabworldmod;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * File operations for the prefab folder. A prefab is a world save directory inside that folder;
 * the one to use is named in the {@value #ACTIVE_FILE} file.
 */
public final class PrefabManager {
    public static final String ACTIVE_FILE = ".active";
    /** Name older versions renamed the first prefab folder to. */
    private static final String LEGACY_PRESET_NAME = "preset_world";

    private PrefabManager() {
    }

    public static Path ensureFolder(Path prefabFolder) throws IOException {
        if (Files.isDirectory(prefabFolder)) {
            return prefabFolder;
        }
        Files.createDirectories(prefabFolder);
        prefabworldmod.LOGGER.info("Created prefab folder: {}", prefabFolder.toAbsolutePath());
        return prefabFolder;
    }

    /** Names of the prefab directories, sorted, ignoring the state file and other hidden entries. */
    public static List<String> listPrefabs(Path prefabFolder) throws IOException {
        if (!Files.isDirectory(prefabFolder)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(prefabFolder)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                if (Files.isDirectory(path) && !name.startsWith(".")) {
                    names.add(name);
                }
            }
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    /** Name of the active prefab, or {@code null} when none has been chosen. */
    public static String readActive(Path prefabFolder) throws IOException {
        Path file = prefabFolder.resolve(ACTIVE_FILE);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        String name = Files.readString(file).trim();
        return name.isEmpty() ? null : name;
    }

    public static void activate(Path prefabFolder, String name) throws IOException {
        if (!Files.isDirectory(prefabFolder.resolve(name))) {
            throw new IOException("Prefab does not exist: " + name);
        }
        Files.writeString(prefabFolder.resolve(ACTIVE_FILE), name);
    }

    /**
     * Resolves the prefab to use. When the state file is missing, older setups are migrated:
     * a {@code preset_world} directory wins, otherwise the prefab is used only if it is the sole one.
     *
     * @return the prefab directory, or {@code null} when no prefab should be applied
     */
    public static Path resolveActive(Path prefabFolder) throws IOException {
        String active = readActive(prefabFolder);
        if (active != null) {
            Path path = prefabFolder.resolve(active);
            if (Files.isDirectory(path)) {
                return path;
            }
            prefabworldmod.LOGGER.warn("Active prefab '{}' does not exist in {}", active, prefabFolder.toAbsolutePath());
            return null;
        }

        Path legacy = prefabFolder.resolve(LEGACY_PRESET_NAME);
        if (Files.isDirectory(legacy)) {
            activate(prefabFolder, LEGACY_PRESET_NAME);
            prefabworldmod.LOGGER.info("Migrated legacy prefab folder '{}'", LEGACY_PRESET_NAME);
            return legacy;
        }

        List<String> names = listPrefabs(prefabFolder);
        if (names.size() == 1) {
            activate(prefabFolder, names.get(0));
            prefabworldmod.LOGGER.info("Activated the only available prefab '{}'", names.get(0));
            return prefabFolder.resolve(names.get(0));
        }
        return null;
    }

    /**
     * What parts of a prefab are copied into a newly created world.
     * <p>
     * A prefab does not need to be a complete save: folders that only carry dimensions, saved data
     * or datapacks are valid prefabs too and are listed by the manager.
     */
    public record CopyOptions(boolean levelDat, boolean playerData, boolean playerProgress, boolean icon,
            boolean dimensions, boolean savedData, boolean datapacks) {
        /** Everything, used when a save is turned into a prefab so the copy stays faithful. */
        public static final CopyOptions ALL =
                new CopyOptions(true, true, true, true, true, true, true);
    }

    public static void apply(Path prefab, Path worldFolder, CopyOptions options) throws IOException {
        copyWorld(prefab, worldFolder, options);
    }

    /** {@return true when the prefab carries a level.dat, i.e. it can be read as a full save} */
    public static boolean hasLevelData(Path prefab) {
        return Files.isRegularFile(prefab.resolve("level.dat"));
    }

    public static void addPrefab(Path sourceWorld, Path prefabFolder, String name, boolean overwrite) throws IOException {
        Path target = prefabFolder.resolve(name);
        if (Files.exists(target)) {
            if (!overwrite) {
                throw new FileAlreadyExistsException(target.toString());
            }
            deleteRecursively(target);
        }
        copyWorld(sourceWorld, target, CopyOptions.ALL);
    }

    public static void removePrefab(Path prefabFolder, String name) throws IOException {
        deleteRecursively(prefabFolder.resolve(name));
        if (name.equals(readActive(prefabFolder))) {
            Files.deleteIfExists(prefabFolder.resolve(ACTIVE_FILE));
        }
    }

    /** Copies a world save tree, skipping the lock file and whatever the options exclude. */
    static void copyWorld(Path source, Path target, CopyOptions options) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            Iterator<Path> iterator = paths.iterator();
            while (iterator.hasNext()) {
                Path from = iterator.next();
                Path relative = source.relativize(from);
                if (relative.toString().isEmpty() || shouldSkip(relative, options)) {
                    continue;
                }
                Path to = target.resolve(relative.toString());
                if (Files.isDirectory(from)) {
                    Files.createDirectories(to);
                } else {
                    Files.createDirectories(to.getParent());
                    Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static boolean shouldSkip(Path relative, CopyOptions options) {
        String path = relative.toString().replace('\\', '/');
        String name = relative.getFileName() == null ? "" : relative.getFileName().toString();
        if (name.equals("session.lock")) {
            return true;
        }
        if (!options.levelDat() && (name.equals("level.dat") || name.equals("level.dat_old"))) {
            return true;
        }
        if (!options.icon() && path.equals("icon.png")) {
            return true;
        }
        if (!options.playerData() && path.startsWith("players/")) {
            return true;
        }
        if (!options.playerProgress() && (path.startsWith("data/stats/") || path.startsWith("data/advancements/"))) {
            return true;
        }
        if (!options.dimensions() && path.startsWith("dimensions/")) {
            return true;
        }
        if (!options.datapacks() && path.startsWith("datapacks/")) {
            return true;
        }
        // Everything else under data/ is saved data written by mods (for example level-wide ids).
        return !options.savedData() && path.startsWith("data/");
    }

    static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(root)) {
            paths = walk.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path path : paths) {
            Files.deleteIfExists(path);
        }
    }
}
