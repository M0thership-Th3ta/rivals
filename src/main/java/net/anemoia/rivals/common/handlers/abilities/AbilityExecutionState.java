package net.anemoia.rivals.common.handlers.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

public class AbilityExecutionState {
    private static final Map<UUID, Boolean> playerExecutionStates = new HashMap<>();

    public static boolean isPlayerExecuting(Player player) {
        return playerExecutionStates.getOrDefault(player.getUUID(), false);
    }

    public static void setPlayerExecuting(Player player, boolean executing) {
        if (executing) {
            playerExecutionStates.put(player.getUUID(), true);
        } else {
            playerExecutionStates.remove(player.getUUID());
        }
    }

    public static void clearPlayerStates(Player player) {
        playerExecutionStates.remove(player.getUUID());
    }

    public static void clearAllStates() {
        playerExecutionStates.clear();
    }
}

