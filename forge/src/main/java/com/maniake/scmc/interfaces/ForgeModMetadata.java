package com.maniake.scmc.interfaces;

import com.maniake.scmc.Constants;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.io.File;
import java.util.Optional;

public class ForgeModMetadata implements IModMetadata {
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
                .map(c -> c.getModInfo())
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
