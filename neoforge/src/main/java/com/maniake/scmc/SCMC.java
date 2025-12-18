package com.maniake.scmc;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class SCMC {
    public static MinecraftServer server;
    public SCMC(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ServerEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(ServerEvents::onServerStopping);
    }
}

