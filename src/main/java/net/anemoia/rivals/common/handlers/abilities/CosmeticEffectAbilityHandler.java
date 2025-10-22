package net.anemoia.rivals.common.handlers.abilities;

import net.anemoia.rivals.common.data.Hero;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CosmeticEffectAbilityHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmeticEffectAbilityHandler.class);
    private static final Map<UUID, DelayedEffect> delayedEffects = new HashMap<>();

    public static void handleCosmeticEffect(Player player, Map<String, Object> abilityData) {
        if (abilityData == null) return;

        LOGGER.info("Executing cosmetic effect for player {} with attributes: {}",
                player.getName().getString(), abilityData);

        String effect = (String) abilityData.get("effect");
        Number delayDurationObj = (Number) abilityData.get("delay_duration");

        if (delayDurationObj != null && delayDurationObj.intValue() > 0) {
            int delayDuration = delayDurationObj.intValue();

            LOGGER.debug("Scheduling cosmetic effect {} with delay {} ticks for player {}",
                    effect, delayDuration, player.getName().getString());

            if (player.level() instanceof ServerLevel serverLevel) {
                UUID effectId = UUID.randomUUID();
                long targetTick = serverLevel.getServer().getTickCount() + delayDuration;

                delayedEffects.put(effectId, new DelayedEffect(targetTick, effect, player.getUUID()));

                LOGGER.debug("Scheduled delayed effect {} for player {} to execute at tick {}",
                        effectId, player.getName().getString(), targetTick);
            } else {
                LOGGER.warn("Cannot schedule delayed cosmetic effect on client side for player {}",
                        player.getName().getString());
                executeEffect(player, effect);
            }
        } else {
            executeEffect(player, effect);
        }
    }


    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        long currentTick = event.getServer().getTickCount();
        Iterator<Map.Entry<UUID, DelayedEffect>> iterator = delayedEffects.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, DelayedEffect> entry = iterator.next();
            DelayedEffect delayedEffect = entry.getValue();

            if (currentTick >= delayedEffect.targetTick) {
                LOGGER.debug("Executing delayed effect {} at tick {}", entry.getKey(), currentTick);

                // Find the player and execute the effect
                Player player = event.getServer().getPlayerList().getPlayer(delayedEffect.playerId);
                if (player != null) {
                    executeEffect(player, delayedEffect.effect);
                } else {
                    LOGGER.warn("Player {} not found for delayed effect execution", delayedEffect.playerId);
                }

                iterator.remove();
            }
        }
    }

    private static void executeEffect(Player player, String effect) {
        if (effect == null) {
            LOGGER.warn("Cannot execute null effect for player {}", player.getName().getString());
            return;
        }

        LOGGER.debug("Executing effect {} for player {}", effect, player.getName().getString());

        switch (effect) {
            case "rivals:test_effect":
                executeTestEffect(player);
                break;
            default:
                LOGGER.warn("Unknown cosmetic effect: {}", effect);
        }
    }

    private static void executeTestEffect(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            LOGGER.warn("Cannot execute test effect on client side for player {}", player.getName().getString());
            return;
        }

        LOGGER.info("Executing test effect (firework) for player {}", player.getName().getString());

        // Create a simple firework effect
        ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag fireworkTag = fireworkStack.getOrCreateTag();
        CompoundTag fireworksTag = new CompoundTag();
        ListTag explosionsList = new ListTag();

        CompoundTag explosion = new CompoundTag();
        explosion.putByte("Type", (byte) 1); // Large ball
        explosion.putIntArray("Colors", new int[]{0xFF0000, 0x00FF00, 0x0000FF}); // RGB colors
        explosionsList.add(explosion);

        fireworksTag.put("Explosions", explosionsList);
        fireworksTag.putByte("Flight", (byte) 1);
        fireworkTag.put("Fireworks", fireworksTag);

        // Spawn firework at player position
        Vec3 playerPos = player.position();
        FireworkRocketEntity firework = new FireworkRocketEntity(
                player.level(),
                playerPos.x,
                playerPos.y + 1,
                playerPos.z,
                fireworkStack
        );

        serverLevel.addFreshEntity(firework);

        LOGGER.debug("Spawned firework at position {} for player {}", playerPos, player.getName().getString());
    }

    private static class DelayedEffect {
        final long targetTick;
        final String effect;
        final UUID playerId;

        DelayedEffect(long targetTick, String effect, UUID playerId) {
            this.targetTick = targetTick;
            this.effect = effect;
            this.playerId = playerId;
        }
    }

    // Register the event handler
    static {
        MinecraftForge.EVENT_BUS.register(CosmeticEffectAbilityHandler.class);
    }
}



