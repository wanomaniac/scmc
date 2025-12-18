package com.maniake.scmc.interfaces;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public String getPlatformVersion(){
        return ModList.get()
                .getModContainerById("forge")
                .map(mod -> mod.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
}
