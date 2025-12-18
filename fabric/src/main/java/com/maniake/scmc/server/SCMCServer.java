package com.maniake.scmc.server;

import com.maniake.scmc.Commons;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class SCMCServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            Commons.init();
            Commons.initServer(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(Commons::stopServer);
    }
}
