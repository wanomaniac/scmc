package com.maniake.scmc.interfaces;

import com.maniake.scmc.Constants;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;


import java.io.File;
import java.util.Optional;

public class NeoForgeModMetadata implements IModMetadata {
    @Override
    public String getVersionName() {
        IModInfo info = ModList.get().getModContainerById(Constants.MOD_ID)
                .map(ModContainer::getModInfo)
                .orElse(null);

        return info == null ? "" : info.getVersion().toString();
    }

    @Override
    public String getConfigVersion() {
        IModInfo info = ModList.get().getModContainerById(Constants.MOD_ID)
                .map(ModContainer::getModInfo)
                .orElse(null);

        if (info == null) return "";
        // Reads from mods.toml
        Optional<String> element = info.getConfig().getConfigElement("configV");
        return element.orElse("");
    }

    @Override
    public File getConfigDir() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }
}
