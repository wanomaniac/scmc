package com.maniake.scmc.utils;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

public class ModFinder {
    public static IModInfo findModById(String modId) {
        for (IModInfo mod : ModList.get().getMods()) {
            if (mod.getModId().equals(modId)) {
                return mod;
            }
        }
        return null; // mod not found
    }
}
