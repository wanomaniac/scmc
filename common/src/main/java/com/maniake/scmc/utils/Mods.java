package com.maniake.scmc.utils;


import com.maniake.scmc.interfaces.IModLoader;
import com.maniake.scmc.services.ServiceKey;
import com.maniake.scmc.services.ServicesManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mods {
    List<IModLoader.Mod> ModL = new ArrayList<>();
    static IModLoader loader = ServicesManager.get(ServiceKey.of(IModLoader.class));
    public List<IModLoader.Mod> getMods() {
        return ModL;
    }

    public void loadMods(String username){
        ModL = loader.loadMods(username);
    }
}