package net.anemoia.rivals.common.handlers.abilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityCooldown {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbilityCooldown.class);
    private static final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();

    public static boolean isOnCooldown(Player player, String abilityName) {
        UUID playerId = player.getUUID();
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);

        if (cooldowns == null || !cooldowns.containsKey(abilityName)) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownEnd = cooldowns.get(abilityName);

        if (currentTime >= cooldownEnd) {
            cooldowns.remove(abilityName);
            if (cooldowns.isEmpty()) {
                playerCooldowns.remove(playerId);
            }
            return false;
        }

        return true;
    }

    public static void setCooldown(Player player, String abilityName, int cooldownTicks) {
        UUID playerId = player.getUUID();
        long cooldownDuration = cooldownTicks * 50; // Convert ticks to milliseconds (20 ticks = 1 second)
        long cooldownEnd = System.currentTimeMillis() + cooldownDuration;

        playerCooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(abilityName, cooldownEnd);

        LOGGER.debug("Set cooldown for player {} ability {} for {} ticks ({}ms)",
                player.getName().getString(), abilityName, cooldownTicks, cooldownDuration);
    }

    public static long getRemainingCooldown(Player player, String abilityName) {
        UUID playerId = player.getUUID();
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);

        if (cooldowns == null || !cooldowns.containsKey(abilityName)) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownEnd = cooldowns.get(abilityName); // Fix: was getting playerId instead of abilityName

        return Math.max(0, (cooldownEnd - currentTime) / 50); // Convert back to ticks
    }

    public static long getCooldownEndTime(Player player, String abilityName) {
        UUID playerId = player.getUUID();
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);

        if (cooldowns == null || !cooldowns.containsKey(abilityName)) {
            return 0L;
        }

        return cooldowns.get(abilityName);
    }

    public static void saveCooldownsToNBT(Player player) {
        UUID playerId = player.getUUID();
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) return;

        CompoundTag tag = player.getPersistentData();
        CompoundTag cooldownTag = new CompoundTag();
        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            cooldownTag.putLong(entry.getKey(), entry.getValue());
        }
        tag.put("rivals_ability_cooldowns", cooldownTag);
    }

    public static void loadCooldownsFromNBT(Player player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains("rivals_ability_cooldowns")) return;

        CompoundTag cooldownTag = tag.getCompound("rivals_ability_cooldowns");
        Map<String, Long> cooldowns = new HashMap<>();
        for (String key : cooldownTag.getAllKeys()) {
            cooldowns.put(key, cooldownTag.getLong(key));
        }
        playerCooldowns.put(player.getUUID(), cooldowns);
    }

    public static void clearPlayerCooldowns(Player player) {
        playerCooldowns.remove(player.getUUID());
        LOGGER.debug("Cleared all cooldowns for player {}", player.getName().getString());
    }

    public static void sendCooldownMessage(Player player, String abilityName) {
        long remainingTicks = getRemainingCooldown(player, abilityName);
        double remainingSeconds = remainingTicks / 20.0;

        Component message = Component.literal(String.format("This ability is on cooldown! (%.1fs remaining)", remainingSeconds));
        player.displayClientMessage(message, true); // true = actionbar

        LOGGER.debug("Sent cooldown message to player {} for ability {} ({}s remaining)",
                player.getName().getString(), abilityName, remainingSeconds);
    }
}
