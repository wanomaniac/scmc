package com.maniake.scmc;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ServerEvents {
    public static void onServerStarted(ServerStartedEvent event) {
        Commons.init();
        SCMC.server = event.getServer();
        if(FMLEnvironment.getDist().isDedicatedServer()){
            Commons.initServer(SCMC.server);
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        if(FMLEnvironment.getDist().isDedicatedServer()) {
            Commons.stopServer(SCMC.server);
        }
    }
}
