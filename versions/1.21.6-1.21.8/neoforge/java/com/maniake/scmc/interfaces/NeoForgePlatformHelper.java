package com.maniake.scmc.interfaces;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public String getPlatformVersion(){
        return ModList.get()
                .getModContainerById("neoforge")
                .map(mod -> mod.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
}
