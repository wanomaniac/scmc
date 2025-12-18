package com.maniake.scmc;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ServerEvents {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        Commons.init();
        SCMC.server = event.getServer();
        if(FMLEnvironment.dist.isDedicatedServer()){
            Commons.initServer(SCMC.server);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        Commons.stopServer(SCMC.server);
    }
}
