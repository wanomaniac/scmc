package com.maniake.scmc.utils;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

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
