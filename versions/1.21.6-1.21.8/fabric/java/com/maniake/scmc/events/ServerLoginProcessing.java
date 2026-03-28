package com.maniake.scmc.events;
import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.db.Player;
import com.maniake.scmc.db.PlayerMods;
import com.maniake.scmc.utils.Mods;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.maniake.scmc.Commons.*;

public class ServerLoginProcessing {
    private static final int MAX_LISTED_MODS = 8;
    private static void appendCappedModList(
            MutableComponent msg,
            List<String> mods,
            ChatFormatting color
    ) {
        int shown = Math.min(mods.size(), MAX_LISTED_MODS);

        for (int i = 0; i < shown; i++) {
            msg.append("  - ")
                    .append(Component.literal(mods.get(i)).withStyle(color))
                    .append("\n");
        }

        int remaining = mods.size() - shown;
        if (remaining > 0) {
            msg.append(Component.literal(
                    "... and " + remaining + " more mods\n"
            ).withStyle(ChatFormatting.GRAY));
        }
    }

    public static void onEventRegister(ServerLoginPacketListenerImpl handler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer){
        String username = handler.getUserName().substring(0, handler.getUserName().indexOf(' '));
        if(PLAYERS.get(username) != null){
            Player player = PLAYERS.get(username);
            if(!Objects.equals(player.gameVersion, SharedConstants.getCurrentVersion().id())){
                MutableComponent message = Component.literal("You cannot join this server:\n\n\n")
                        .withStyle(ChatFormatting.RED);

                message.append(Component.literal("Your game version is out of date!\n\n").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_RED));
                message.append(Component.literal("Please update your game to version "+SharedConstants.getCurrentVersion().name()+" to join this server!").withStyle(ChatFormatting.WHITE));
                handler.disconnect(message);
                return;
            }
            if(!Objects.equals(player.protocal, SharedConstants.getProtocolVersion())){
                MutableComponent message = Component.literal("You cannot join this server:\n")
                        .withStyle(ChatFormatting.RED);

                message.append(Component.literal("Your game's internal protocal is out of date!\n").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_RED));
                message.append(Component.literal("Please update your game to a version that supports protocal "+SharedConstants.getProtocolVersion()+" to join this server!").withStyle(ChatFormatting.WHITE));
                handler.disconnect(message);
                return;
            }
            if(!Objects.equals(player.modLoader, PLATFORM.getPlatformName())){
                MutableComponent message = Component.empty();
                message.append(
                        Component.literal("You are using ")
                                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                );
                message.append(
                        Component.literal(player.modLoader)
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
                handler.disconnect(message);
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
                        outdatedMods.add(modId + " (your version: " + clientVer + ", server: " + serverVer + ")");
                    }
                }
            }

            if(ModMenuConfig.CANKICKPLAYERSWITHNOMODS.getValue()) {
                if (!missingMods.isEmpty() || !outdatedMods.isEmpty()) {
                    MutableComponent msg = Component.literal("You cannot join this server:\n\n")
                            .withStyle(ChatFormatting.RED);

                    if (!missingMods.isEmpty()) {
//                        msg.append("Uninstalled mods:\n");
//                        for (String id : missingMods) {
//                            msg.append("  - ").append(Component.literal(id).withStyle(ChatFormatting.YELLOW)).append("\n");
//                        }
//                        msg.append("\n");
                        appendCappedModList(msg, missingMods, ChatFormatting.YELLOW);
                    }

                    if (!outdatedMods.isEmpty()) {
                        msg.append("Out-of-date mods:\n");
                        appendCappedModList(msg, outdatedMods, ChatFormatting.YELLOW);
                    }

                    msg.append(Component.literal("Please update/install these mods and restart your game.")
                            .withStyle(ChatFormatting.WHITE));

                    handler.disconnect(msg);
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

//                msg.append("Disallowed mods installed:\n");
//                for (String id : disallowedMods) {
//                    msg.append("- ")
//                            .append(Component.literal(id).withStyle(ChatFormatting.DARK_RED))
//                            .append("\n");
//                }

                appendCappedModList(msg, disallowedMods, ChatFormatting.DARK_RED);

                msg.append("\nRemove these mods and restart your game.")
                        .withStyle(ChatFormatting.WHITE);

                handler.disconnect(msg);

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

                    for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                        serverPlayer.sendSystemMessage(announcementMsg);
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
                    appendCappedModList(msg, extraMods, ChatFormatting.YELLOW);

                    msg.append("\nRemove these mods and restart your game.")
                            .withStyle(ChatFormatting.WHITE);

                    handler.disconnect(msg);
                }
            }
        } else {
            handler.disconnect(Component.literal("You require the latest version of servermodmenu to join this server! Please install that mod to your client or update it if you have it already"));
        }
    }
}
