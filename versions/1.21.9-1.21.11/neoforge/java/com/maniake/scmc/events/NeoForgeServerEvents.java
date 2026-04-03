package com.maniake.scmc.events;

import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.interfaces.IServerEvents;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerNegotiationEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

public class NeoForgeServerEvents implements IServerEvents {

    @Override
    public void Register() {
        if (!ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
            // Equivalent to Fabric's ServerPlayConnectionEvents.JOIN
            NeoForge.EVENT_BUS.register(this);
        }
    }

    // Player fully joined world (Fabric: ServerPlayConnectionEvents.JOIN)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
            ServerJoinProcessing.onEventRegister(event.getEntity());
        }
    }

    // This is the closest Forge equivalent to Fabric's login QUERY_START
    // Fires before the player is fully in the world
    public static void onPlayerLogin(final RegisterConfigurationTasksEvent event){
        if (ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
            PacketListener packetListener = event.getListener();
            if (!(packetListener instanceof ServerConfigurationPacketListenerImpl)) {
                return;
            }
            GameProfile gameProfile = ((ServerConfigurationPacketListenerImpl) packetListener).getOwner();
            String username = gameProfile.name();
            ServerLoginProcessing.onEventRegister(username, event.getListener().getConnection());
        }
    }
}
