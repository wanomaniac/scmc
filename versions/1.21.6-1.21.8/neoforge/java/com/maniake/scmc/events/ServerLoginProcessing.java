package com.maniake.scmc.events;

import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.db.PlayerMods;
import com.maniake.scmc.utils.Mods;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.maniake.scmc.Commons.PLAYERS;
import static com.maniake.scmc.SCMC.server;

public class ServerLoginProcessing {
    public static void onEventRegister(Player player){
        ServerPlayer serverPlayer = (ServerPlayer)player;
        String username = serverPlayer.connection.getOwner().getName();

        if(PLAYERS.get(username) != null){
            Mods md = new Mods();
            md.loadMods("null");
            List<PlayerMods> clientMods = PLAYERS.get(username).getMods();

            Map<String, String> clientModMap = clientMods.stream()
                    .collect(Collectors.toMap(pm -> pm.id, pm -> pm.version));

            Map<String, String> serverModMap = md.getMods()
                    .stream()
                    .filter(ra -> !ra.isOptional)
                    .collect(Collectors.toMap(ra -> ra.id, ra -> ra.version));

            List<String> missingMods = new ArrayList<>();

            for (String modId : serverModMap.keySet()) {
                if (!clientModMap.containsKey(modId)) {
                    missingMods.add(modId);
                }
            }

            List<String> outdatedMods = new ArrayList<>();

            for (String modId : serverModMap.keySet()) {
                if (clientModMap.containsKey(modId)) {
                    String clientVer = clientModMap.get(modId);
                    String serverVer = serverModMap.get(modId);

                    if (!clientVer.equals(serverVer)) {
                        outdatedMods.add(modId + " (client: " + clientVer + ", server: " + serverVer + ")");
                    }
                }
            }

            if(ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
                if (!missingMods.isEmpty() || !outdatedMods.isEmpty()) {
                    MutableComponent msg = Component.literal("You cannot join this server:\n\n")
                            .withStyle(ChatFormatting.RED);

                    if (!missingMods.isEmpty()) {
                        msg.append("Uninstalled mods:\n");
                        for (String id : missingMods) {
                            msg.append("  - ").append(Component.literal(id).withStyle(ChatFormatting.YELLOW)).append("\n");
                        }
                        msg.append("\n");
                    }

                    if (!outdatedMods.isEmpty()) {
                        msg.append("Out-of-date mods:\n");
                        for (String id : outdatedMods) {
                            msg.append("  - ").append(Component.literal(id).withStyle(ChatFormatting.GOLD)).append("\n");
                        }
                        msg.append("\n");
                    }

                    msg.append(Component.literal("Please update/install these mods and restart your game.")
                            .withStyle(ChatFormatting.WHITE));

                    serverPlayer.connection.disconnect(msg);
                }
            }

            List<String> disallowedMods = new ArrayList<>();
            ModMenuConfig.DISALLOWEDMODS.getValue().forEach(disallowedMod -> {
                if (clientModMap.containsKey(disallowedMod)) {
                    disallowedMods.add(disallowedMod);
                }
            });

            if(!disallowedMods.isEmpty()){
                MutableComponent msg = Component.literal("You cannot join this server:\n\n")
                        .withStyle(ChatFormatting.RED);

                msg.append("Disallowed mods installed:\n");
                for (String id : disallowedMods) {
                    msg.append("- ")
                            .append(Component.literal(id).withStyle(ChatFormatting.DARK_RED))
                            .append("\n");
                }

                msg.append("\nRemove these mods and restart your game.")
                        .withStyle(ChatFormatting.WHITE);

                serverPlayer.connection.disconnect(msg);

                if(ModMenuConfig.ANNONCEDISALLOWEDMODS.getValue()){
                    MutableComponent announcementMsg = Component.literal(username+" has attempted to join the server with disallowed mods installed!\n The mods are: \n\n")
                            .withStyle(ChatFormatting.RED);

                    for (String id : disallowedMods) {
                        announcementMsg.append("- ")
                                .append(Component.literal(id).withStyle(ChatFormatting.DARK_RED))
                                .append("\n");
                    }

                    server.sendSystemMessage(
                            announcementMsg
                    );


                    for (ServerPlayer playerObj : server.getPlayerList().getPlayers()) {
                        playerObj.sendSystemMessage(announcementMsg);
                    }
                }
            }

            if(ModMenuConfig.ENFORCESAMEMODS.getValue()){
                // ensure the player has the same exact mods, nothing extra!
                List<String> extraMods = new ArrayList<>();

                for (String clientModId : clientModMap.keySet()) {
                    if (!serverModMap.containsKey(clientModId)) {
                        extraMods.add(clientModId);
                    }
                }

                if (!extraMods.isEmpty()) {
                    MutableComponent msg = Component.literal("You cannot join this server:\n\n")
                            .withStyle(ChatFormatting.RED);

                    msg.append("Extra mods installed:\n");
                    for (String id : extraMods) {
                        msg.append("  - ")
                                .append(Component.literal(id).withStyle(ChatFormatting.AQUA))
                                .append("\n");
                    }

                    msg.append("\nRemove these mods and restart your game.")
                            .withStyle(ChatFormatting.WHITE);


                    serverPlayer.connection.disconnect(msg);
                }
            }
        } else {
            serverPlayer.connection.disconnect(Component.literal("You require the latest version of servermodmenu to join this server! Please install that mod to your client or update it if you have it already"));
        }
    }
}
