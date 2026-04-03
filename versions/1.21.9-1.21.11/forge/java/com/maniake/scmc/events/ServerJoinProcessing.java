package com.maniake.scmc.events;

import com.maniake.scmc.utils.Mods;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.maniake.scmc.Commons.PLAYERS;


// version mismatches would not allow the player to join the server on fab
public class ServerJoinProcessing {
public static void onEventRegister(Player player){
    Mods md = new Mods();
    md.loadMods("nul");
    String playerName = player.getName().getString();

    var data = PLAYERS.get(playerName);
    if (data == null) {
        player.displayClientMessage(Component.literal(
                "Unable to fetch your player data! Is your server mod menu installed?"
        ).withStyle(ChatFormatting.RED), true);
        return;
    }

    List<String> installedIDs = data.getMods().stream()
            .map(pm -> pm.id)
            .toList();

    List<String> requiredIDs = md.getMods().stream()
            .filter(m -> !m.isOptional)
            .map(m -> m.id)
            .toList();

    List<String> missing = requiredIDs.stream()
            .filter(id -> !installedIDs.contains(id))
            .toList();

    if (!missing.isEmpty()) {
        List<String> display = new ArrayList<>(missing);
        boolean hasMore = false;
        int extraCount = 0;

        if (display.size() > 7) {
            extraCount = display.size() - 7;
            display = display.subList(0, 7);
            hasMore = true;
        }

        MutableComponent msg = Component.literal("")
                .append(Component.literal("Hello " + playerName + "!\n")
                        .withStyle(ChatFormatting.RED))
                .append(Component.literal("You are missing required mods for this server.\n")
                        .withStyle(ChatFormatting.RED))
                .append(Component.literal("\nMissing mods:")
                        .withStyle(ChatFormatting.WHITE));

        for (String mod : display) {
            msg.append(Component.literal("\n")
                    .withStyle(ChatFormatting.GRAY));
            msg.append(Component.literal(mod)
                    .withStyle(ChatFormatting.YELLOW)
                    .withStyle(ChatFormatting.BOLD));
        }

        if (hasMore) {
            msg.append(Component.literal("\n  + " + extraCount + " more…")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        msg.append(Component.literal("\n\nPlease install these mods and rejoin if needed.")
                .withStyle(ChatFormatting.WHITE));

        player.displayClientMessage(msg, true);
    }
}
}
