package com.maniake.scmc.events;

import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.db.PlayerMods;
import com.maniake.scmc.utils.Mods;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerNegotiationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.maniake.scmc.Commons.PLATFORM;
import static com.maniake.scmc.Commons.PLAYERS;
import static com.maniake.scmc.SCMC.server;

public class ServerLoginProcessing {
    private static void disconnectPlayer(Connection connection, Component message){
        connection.send(new ClientboundDisconnectPacket(message));
    }
    public static void onEventRegister(String username, Connection connection){
        if(PLAYERS.get(username) != null){
            com.maniake.scmc.db.Player playerDB = PLAYERS.get(username);
            if(!Objects.equals(playerDB.gameVersion, SharedConstants.getCurrentVersion().id())){
                MutableComponent message = Component.literal("You cannot join this server:\n\n\n")
                        .withStyle(ChatFormatting.RED);

                message.append(Component.literal("Your game version is out of date!\n\n").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_RED));
                message.append(Component.literal("Please update your game to version "+SharedConstants.getCurrentVersion().name()+" to join this server!").withStyle(ChatFormatting.WHITE));
                disconnectPlayer(connection, message);
                return;
            }
            if(!Objects.equals(playerDB.protocal, SharedConstants.getProtocolVersion())){
                MutableComponent message = Component.literal("You cannot join this server:\n")
                        .withStyle(ChatFormatting.RED);

                message.append(Component.literal("Your game's internal protocal is out of date!\n").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_RED));
                message.append(Component.literal("Please update your game to a version that supports protocal "+SharedConstants.getProtocolVersion()+" to join this server!").withStyle(ChatFormatting.WHITE));
                disconnectPlayer(connection, message);
                return;
            }
            if(!Objects.equals(playerDB.modLoader, PLATFORM.getPlatformName())){
                MutableComponent message = Component.empty();
                message.append(
                        Component.literal("You are using ")
                                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                );
                message.append(
                        Component.literal(playerDB.modLoader)
                                .withStyle(ChatFormatting.WHITE)
                );
                message.append(
                        Component.literal(" when this server is using ")
                                .withStyle(ChatFormatting.GREEN)
                );
                message.append(
                        Component.literal(PLATFORM.getPlatformName())
                                .withStyle(ChatFormatting.WHITE)
                );
                message.append("\n");
                message.append(
                        Component.literal("Please switch to " + PLATFORM.getPlatformName() + " to play on this server.")
                                .withStyle(ChatFormatting.WHITE)
                );
                disconnectPlayer(connection, message);
                return;
            }
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

                    disconnectPlayer(connection, msg);
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

                disconnectPlayer(connection, msg);

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


                    disconnectPlayer(connection, msg);
                }
            }
        } else {
            disconnectPlayer(connection, Component.literal("You require the latest version of servermodmenu to join this server! Please install that mod to your client or update it if you have it already"));
        }
    }
}
