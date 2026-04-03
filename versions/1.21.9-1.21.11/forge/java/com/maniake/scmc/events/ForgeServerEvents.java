package com.maniake.scmc.events;

import com.maniake.scmc.Constants;
import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.interfaces.IServerEvents;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.network.GatherLoginConfigurationTasksEvent;

public class ForgeServerEvents implements IServerEvents {
    @Override
    public void Register() {
        if (!ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
            // Equivalent to Fabric's ServerPlayConnectionEvents.JOIN
            MinecraftForge.EVENT_BUS.register(this);
        } else {
            // Pre-join login checking equivalent
            MinecraftForge.EVENT_BUS.register(this);
        }
    }



    // Player fully joined world (Fabric: ServerPlayConnectionEvents.JOIN)
    @SubscribeEvent
        public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
            ServerJoinProcessing.onEventRegister(event.getEntity());
        }
    }

    // This is the closest Forge equivalent to Fabric's login QUERY_START
    // Fires before the player is fully in the world

    @SubscribeEvent
    public void onPlayerPreLogin(GatherLoginConfigurationTasksEvent event) {
        if (ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
            PacketListener packetListener = event.getConnection().getPacketListener();
            if (!(packetListener instanceof ServerConfigurationPacketListenerImpl)) {
                return;
            }
            GameProfile gameProfile = ((ServerConfigurationPacketListenerImpl) packetListener).getOwner();
            String username = gameProfile.name();
            Connection connection = event.getConnection();

            ServerLoginProcessing.onEventRegister(username, connection);
        }
    }
}
