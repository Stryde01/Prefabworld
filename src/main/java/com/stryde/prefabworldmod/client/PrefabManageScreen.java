package com.stryde.prefabworldmod.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.neoforged.fml.loading.FMLPaths;
import org.jspecify.annotations.Nullable;

import com.stryde.prefabworldmod.PrefabConfig;
import com.stryde.prefabworldmod.PrefabManager;
import com.stryde.prefabworldmod.prefabworldmod;

/**
 * A list of all saves, laid out like the vanilla world selection list. Saves that are prefabs get
 * Use/Delete buttons; every other save gets an Add button.
 */
public class PrefabManageScreen extends Screen {
    private static final int ENTRY_HEIGHT = 36;
    private static final int ICON_SIZE = 32;
    private static final int LIST_PADDING = 20;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 32, 36);
    private final Screen parent;
    private WorldList list;
    private StringWidget titleWidget;
    private String activeName;

    public PrefabManageScreen(Screen parent) {
        super(Component.translatable("prefabworldmod.manage.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        try {
            PrefabManager.ensureFolder(prefabFolder());
        } catch (IOException e) {
            prefabworldmod.LOGGER.error("Failed to create the prefab folder", e);
        }

        LinearLayout header = this.layout.addToHeader(LinearLayout.vertical());
        header.defaultCellSetting().alignHorizontallyCenter();
        this.titleWidget = header.addChild(new StringWidget(this.title, this.font));

        this.list = this.addRenderableWidget(new WorldList(this.minecraft, this.width - LIST_PADDING * 2,
                this.layout.getContentHeight()));
        this.list.setPosition(LIST_PADDING, this.layout.getHeaderHeight());

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.defaultCellSetting().alignHorizontallyCenter();
        footer.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(120).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(120).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.layout.arrangeElements();
        this.reload();
    }

    @Override
    protected void repositionElements() {
        if (this.list != null) {
            this.list.children().forEach(WorldEntry::dispose);
        }
        this.layout.arrangeElements();
        if (this.list != null) {
            this.list.updateSizeAndPosition(this.width - LIST_PADDING * 2, this.layout.getContentHeight(),
                    LIST_PADDING, this.layout.getHeaderHeight());
        }
        this.reload();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (this.list == null || !this.list.children().isEmpty()) {
            return;
        }
        int centerX = this.list.getX() + this.list.getWidth() / 2;
        int centerY = this.list.getY() + 40;
        graphics.centeredText(this.font, Component.translatable("prefabworldmod.empty.saves"), centerX, centerY, 0xFFA0A0A0);
        graphics.centeredText(this.font, Component.translatable("prefabworldmod.empty.saves.hint"), centerX, centerY + 14, 0xFF6E6E6E);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private static Path prefabFolder() {
        return FMLPaths.GAMEDIR.get().resolve(PrefabConfig.folderName());
    }

    private void reload() {
        Minecraft minecraft = this.minecraft;
        Path prefabFolder = prefabFolder();
        CompletableFuture.supplyAsync(() -> loadData(minecraft.getLevelSource(), prefabFolder), Util.ioPool())
                .whenComplete((data, error) -> minecraft.execute(() -> {
                    if (this.list == null) {
                        return;
                    }
                    if (error != null) {
                        prefabworldmod.LOGGER.error("Failed to load saves", error);
                        return;
                    }
                    this.activeName = data.active();
                    this.titleWidget.setMessage(this.activeName == null
                            ? Component.translatable("prefabworldmod.manage.title")
                            : Component.translatable("prefabworldmod.manage.title.active", this.activeName));
                    this.list.setEntries(data.entries());
                }));
    }

    private static LoadedData loadData(LevelStorageSource saveSource, Path prefabFolder) {
        Set<String> prefabs = new HashSet<>();
        String active = null;
        try {
            PrefabManager.ensureFolder(prefabFolder);
            prefabs.addAll(PrefabManager.listPrefabs(prefabFolder));
            active = PrefabManager.readActive(prefabFolder);
        } catch (Exception e) {
            prefabworldmod.LOGGER.error("Failed to read prefabs", e);
        }

        List<WorldInfo> entries = new ArrayList<>();
        Set<String> listed = new HashSet<>();
        try {
            for (LevelSummary summary : summaries(saveSource)) {
                String id = summary.getLevelId();
                listed.add(id);
                entries.add(WorldInfo.ofSave(summary, prefabs.contains(id)));
            }
        } catch (Exception e) {
            prefabworldmod.LOGGER.error("Failed to read saves", e);
        }

        // Prefabs whose original save is gone usually still parse, because they carry level.dat.
        if (!prefabs.isEmpty()) {
            try {
                for (LevelSummary summary : summaries(LevelStorageSource.createDefault(prefabFolder))) {
                    String id = summary.getLevelId();
                    if (prefabs.contains(id) && !listed.contains(id)) {
                        entries.add(WorldInfo.ofPartial(summary));
                        listed.add(id);
                    }
                }
            } catch (Exception e) {
                prefabworldmod.LOGGER.error("Failed to read prefab saves", e);
            }
        }

        // Anything left is a data-only prefab: dimensions, saved data or datapacks without a level.dat.
        for (String name : prefabs) {
            if (!listed.contains(name)) {
                entries.add(WorldInfo.ofDataOnly(name));
            }
        }

        entries.sort(Comparator
                .comparing((WorldInfo info) -> !info.prefab())
                .thenComparing(info -> -info.lastPlayed())
                .thenComparing(WorldInfo::displayName));
        return new LoadedData(entries, active);
    }

    private static List<LevelSummary> summaries(LevelStorageSource source) throws Exception {
        LevelStorageSource.LevelCandidates candidates = source.findLevelCandidates();
        return source.loadLevelSummaries(candidates).join();
    }

    private void addPrefab(String levelId) {
        Path prefabFolder = prefabFolder();
        String name = levelId;
        if (Files.exists(prefabFolder.resolve(name))) {
            this.minecraft.gui.setScreen(new ConfirmScreen(
                    confirmed -> {
                        this.minecraft.gui.setScreen(this);
                        if (confirmed) {
                            startCopy(prefabFolder, name);
                        }
                    },
                    Component.translatable("prefabworldmod.confirm.overwrite.title"),
                    Component.translatable("prefabworldmod.confirm.overwrite.message", name)));
            return;
        }
        startCopy(prefabFolder, name);
    }

    private void startCopy(Path prefabFolder, String name) {
        Path source = this.minecraft.getLevelSource().getLevelPath(name);
        Minecraft minecraft = this.minecraft;
        CompletableFuture.runAsync(() -> {
            try {
                PrefabManager.addPrefab(source, prefabFolder, name, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, Util.ioPool()).whenComplete((unused, error) -> minecraft.execute(() -> {
            if (error != null) {
                prefabworldmod.LOGGER.error("Failed to add prefab '{}'", name, error);
            }
            this.reload();
        }));
    }

    private void activatePrefab(String name) {
        try {
            PrefabManager.activate(prefabFolder(), name);
        } catch (IOException e) {
            prefabworldmod.LOGGER.error("Failed to activate prefab '{}'", name, e);
        }
        this.reload();
    }

    private void deletePrefab(String name) {
        this.minecraft.gui.setScreen(new ConfirmScreen(
                confirmed -> {
                    this.minecraft.gui.setScreen(this);
                    if (confirmed) {
                        startDelete(name);
                    }
                },
                Component.translatable("prefabworldmod.confirm.delete.title"),
                Component.translatable("prefabworldmod.confirm.delete.message", name)));
    }

    private void startDelete(String name) {
        Minecraft minecraft = this.minecraft;
        CompletableFuture.runAsync(() -> {
            try {
                PrefabManager.removePrefab(prefabFolder(), name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, Util.ioPool()).whenComplete((unused, error) -> minecraft.execute(() -> {
            if (error != null) {
                prefabworldmod.LOGGER.error("Failed to delete prefab '{}'", name, error);
            }
            this.reload();
        }));
    }

    private static String lastPlayedText(long played) {
        return played <= 0L ? "-" : DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(played));
    }

    private static String clip(Font font, String text, int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int available = maxWidth - font.width(ellipsis);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (font.width(result.toString() + c) > available) {
                break;
            }
            result.append(c);
        }
        return result + ellipsis;
    }

    /** Loads a world's icon, falling back to the vanilla missing-icon texture. */
    private static FaviconTexture loadIcon(Minecraft minecraft, WorldInfo info) {
        FaviconTexture icon = FaviconTexture.forWorld(minecraft.getTextureManager(), info.levelId());
        Path iconFile = info.iconPath();
        if (iconFile != null && Files.isRegularFile(iconFile)) {
            try (InputStream stream = Files.newInputStream(iconFile)) {
                icon.upload(NativeImage.read(stream));
            } catch (Throwable t) {
                prefabworldmod.LOGGER.warn("Invalid icon for world {}", info.levelId(), t);
                icon.clear();
            }
        }
        return icon;
    }

    /**
     * A row in the manager list. {@code summary} is null for prefabs that only carry data folders
     * (dimensions, saved data, datapacks) and no level.dat, so they cannot be read as a save.
     */
    private record WorldInfo(@Nullable LevelSummary summary, String levelId, String displayName, boolean prefab) {
        boolean partial() {
            return this.summary == null;
        }

        long lastPlayed() {
            return this.summary == null ? 0L : this.summary.getLastPlayed();
        }

        @Nullable
        Path iconPath() {
            return this.summary == null ? null : this.summary.getIcon();
        }

        static WorldInfo ofSave(LevelSummary summary, boolean prefab) {
            return new WorldInfo(summary, summary.getLevelId(), summary.getLevelName(), prefab);
        }

        static WorldInfo ofPartial(LevelSummary summary) {
            return new WorldInfo(summary, summary.getLevelId(), summary.getLevelName(), true);
        }

        static WorldInfo ofDataOnly(String name) {
            return new WorldInfo(null, name, name, true);
        }
    }

    private record LoadedData(List<WorldInfo> entries, String active) {
    }

    private final class WorldList extends ObjectSelectionList<WorldEntry> {
        WorldList(Minecraft minecraft, int width, int height) {
            super(minecraft, width, height, 0, ENTRY_HEIGHT);
        }

        @Override
        public int getRowWidth() {
            return this.getWidth() - 12;
        }

        @Override
        protected int scrollBarX() {
            return this.getRight() - 8;
        }

        @Override
        protected boolean entriesCanBeSelected() {
            return false;
        }

        void setEntries(List<WorldInfo> entries) {
            this.children().forEach(WorldEntry::dispose);
            this.clearEntries();
            for (WorldInfo info : entries) {
                this.addEntry(new WorldEntry(info, info.prefab() && info.levelId().equals(activeName)));
            }
        }
    }

    private final class WorldEntry extends ObjectSelectionList.Entry<WorldEntry> {
        private static final int BUTTON_HEIGHT = 20;
        private static final int ACTION_WIDTH = 50;

        private final WorldInfo info;
        private final boolean prefab;
        private final boolean active;
        private final FaviconTexture icon;
        private final Button addButton;
        private final Button useButton;
        private final Button deleteButton;

        WorldEntry(WorldInfo info, boolean active) {
            this.info = info;
            this.prefab = info.prefab();
            this.active = active;
            this.icon = loadIcon(Minecraft.getInstance(), info);

            this.addButton = Button.builder(Component.translatable("prefabworldmod.add"),
                            button -> addPrefab(info.levelId()))
                    .width(ACTION_WIDTH)
                    .tooltip(Tooltip.create(Component.translatable("prefabworldmod.tooltip.add")))
                    .build();
            this.useButton = Button.builder(Component.translatable("prefabworldmod.activate"),
                            button -> activatePrefab(info.levelId()))
                    .width(ACTION_WIDTH)
                    .tooltip(Tooltip.create(Component.translatable("prefabworldmod.tooltip.activate")))
                    .build();
            this.deleteButton = Button.builder(Component.translatable("prefabworldmod.delete"),
                            button -> deletePrefab(info.levelId()))
                    .width(ACTION_WIDTH)
                    .tooltip(Tooltip.create(Component.translatable("prefabworldmod.tooltip.delete")))
                    .build();
        }

        void dispose() {
            if (!this.icon.isClosed()) {
                this.icon.close();
            }
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int x = this.getContentX();
            int y = this.getContentY();
            graphics.blit(RenderPipelines.GUI_TEXTURED, this.icon.textureLocation(), x, y, 0.0F, 0.0F,
                    ICON_SIZE, ICON_SIZE, 32, 32);

            int buttonsX;
            if (this.prefab) {
                buttonsX = this.getContentRight() - ACTION_WIDTH * 2 - 4;
                this.useButton.setX(buttonsX);
                this.useButton.setY(y + 8);
                this.useButton.active = !this.active;
                this.deleteButton.setX(buttonsX + ACTION_WIDTH + 4);
                this.deleteButton.setY(y + 8);
            } else {
                buttonsX = this.getContentRight() - ACTION_WIDTH;
                this.addButton.setX(buttonsX);
                this.addButton.setY(y + 8);
            }

            int textX = x + ICON_SIZE + 4;
            int textWidth = buttonsX - textX - 8;
            String title = clip(font, this.info.displayName(), textWidth);
            graphics.text(font, title, textX, y + 1, this.active ? 0xFF55FF55 : 0xFFFFFFFF);
            String meta = this.info.partial()
                    ? Component.translatable("prefabworldmod.meta.dataOnly", this.info.levelId()).getString()
                    : lastPlayedText(this.info.lastPlayed()) + "  ·  " + this.info.levelId();
            graphics.text(font, clip(font, meta, textWidth), textX, y + 11, 0xFF8B8B8B);
            if (this.active) {
                graphics.text(font, Component.translatable("prefabworldmod.badge.active"), textX, y + 21, 0xFF55FF55);
            } else if (this.prefab) {
                graphics.text(font, Component.translatable("prefabworldmod.badge.prefab"), textX, y + 21, 0xFFA0A0A0);
            }

            if (this.prefab) {
                this.useButton.extractRenderState(graphics, mouseX, mouseY, a);
                this.deleteButton.extractRenderState(graphics, mouseX, mouseY, a);
            } else {
                this.addButton.extractRenderState(graphics, mouseX, mouseY, a);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (this.prefab) {
                if (this.useButton.mouseClicked(event, doubleClick) || this.deleteButton.mouseClicked(event, doubleClick)) {
                    return true;
                }
            } else if (this.addButton.mouseClicked(event, doubleClick)) {
                return true;
            }
            return super.mouseClicked(event, doubleClick);
        }

        @Override
        public Component getNarration() {
            return Component.literal(this.info.displayName());
        }
    }
}
