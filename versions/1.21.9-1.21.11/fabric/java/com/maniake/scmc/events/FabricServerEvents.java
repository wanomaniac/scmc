package com.maniake.scmc.events;

import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.interfaces.IServerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricServerEvents implements IServerEvents {
    @Override
    public void Register() {
        if (!ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
            ServerPlayConnectionEvents.JOIN.register(ServerJoinProcessing::onEventRegister);
        } else ServerLoginConnectionEvents.QUERY_START.register(ServerLoginProcessing::onEventRegister);
    }
}
