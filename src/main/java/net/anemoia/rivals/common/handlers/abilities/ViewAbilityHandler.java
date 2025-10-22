package net.anemoia.rivals.common.handlers.abilities;

import net.anemoia.rivals.common.network.NetworkHandler;
import net.anemoia.rivals.common.network.ViewAbilityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class ViewAbilityHandler {
    public static void handleViewAbility(Player player, Map<String, Object> abilityData, Runnable callback) {
        if (abilityData == null) {
            callback.run();
            return;
        }

        String viewType = (String) abilityData.get("view_type");
        Boolean viewLocked = abilityData.get("view_locked") instanceof Boolean
                ? (Boolean) abilityData.get("view_locked")
                : Boolean.FALSE;

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new ViewAbilityPacket(viewType, viewLocked), serverPlayer);
        }

        callback.run();
    }
}
