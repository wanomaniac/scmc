package com.maniake.scmc.interfaces;

import com.maniake.scmc.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.File;

public class ModMetadataFabric implements IModMetadata {
    @Override
    public String getVersionName() {
        ModContainer mod = FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElse(null);
        if(mod == null) return "";
        return mod.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public String getConfigVersion() {
        ModContainer mod = FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElse(null);
        if(mod == null) return "";
        return mod.getMetadata().getCustomValue("configV").getAsString();
    }

    @Override
    public File getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }
}
