package com.stryde.prefabworldmod;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(prefabworldmod.MODID)
public class prefabworldmod {
    public static final String MODID = "prefabworldmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public prefabworldmod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.LOCAL, PrefabConfig.SPEC);
        LOGGER.info("PrefabWorld loaded");
    }
}
