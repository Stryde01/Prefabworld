package com.stryde.prefabworldmod.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import com.stryde.prefabworldmod.prefabworldmod;

// Adds an in-game config screen so pack authors can flip showManageButton without editing TOML.
@Mod(value = prefabworldmod.MODID, dist = Dist.CLIENT)
public class PrefabClientMod {
    public PrefabClientMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
