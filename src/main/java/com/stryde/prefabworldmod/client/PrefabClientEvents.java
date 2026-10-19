package com.stryde.prefabworldmod.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import com.stryde.prefabworldmod.PrefabConfig;
import com.stryde.prefabworldmod.prefabworldmod;

@EventBusSubscriber(modid = prefabworldmod.MODID, value = Dist.CLIENT)
public final class PrefabClientEvents {
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_SPACING = 24;
    private static final String MULTIPLAYER_KEY = "menu.multiplayer";

    private PrefabClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }
        if (!PrefabConfig.showManageButton()) {
            return;
        }

        Button multiplayer = findButton(event.getListenersList(), Component.translatable(MULTIPLAYER_KEY).getString());
        if (multiplayer == null) {
            return;
        }

        int insertY = multiplayer.getY() + ROW_SPACING;
        // Push everything below the multiplayer button down one row so the new button fits between it
        // and the buttons that were there. init() runs again on resize, so this never accumulates.
        for (GuiEventListener listener : event.getListenersList()) {
            if (listener instanceof AbstractWidget widget && widget.getY() >= insertY) {
                widget.setY(widget.getY() + ROW_SPACING);
            }
        }

        Button manageButton = Button.builder(
                        Component.translatable("prefabworldmod.manage"),
                        button -> Minecraft.getInstance().gui.setScreen(new PrefabManageScreen(screen)))
                .bounds(multiplayer.getX(), insertY, multiplayer.getWidth(), BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("prefabworldmod.manage.tooltip")))
                .build();
        event.addListener(manageButton);
    }

    private static Button findButton(List<GuiEventListener> listeners, String label) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof Button button && button.getMessage().getString().equals(label)) {
                return button;
            }
        }
        return null;
    }
}
