package net.anemoia.rivals.common.handlers.abilities;

import net.anemoia.rivals.common.data.Hero;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DelayAbilityHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayAbilityHandler.class);
    private static final Map<UUID, DelayedTask> delayedTasks = new HashMap<>();

    public static void handleDelayAbility(Player player, Hero.Ability ability) {
        handleDelayAbility(player, ability, null);
    }

    public static void handleDelayAbility(Player player, Hero.Ability ability, Runnable callback) {
        Map<String, Object> attributes = ability.getAbility().getAbilityAttributes();
        if (attributes == null) {
            if (callback != null) callback.run();
            return;
        }

        Number delayDurationObj = (Number) attributes.get("delay_duration");
        if (delayDurationObj == null) {
            LOGGER.warn("Delay ability missing delay_duration");
            if (callback != null) callback.run();
            return;
        }

        int delayDuration = delayDurationObj.intValue();

        LOGGER.info("Scheduling delay for player {} with duration: {} ticks",
                player.getName().getString(), delayDuration);

        if (player.level() instanceof ServerLevel serverLevel) {
            UUID taskId = UUID.randomUUID();
            long targetTick = serverLevel.getServer().getTickCount() + delayDuration;

            delayedTasks.put(taskId, new DelayedTask(targetTick, callback, player.getUUID()));

            LOGGER.debug("Scheduled delay task {} for player {} to execute at tick {}",
                    taskId, player.getName().getString(), targetTick);
        } else {
            LOGGER.warn("Cannot schedule delay on client side for player {}", player.getName().getString());
            if (callback != null) callback.run();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        long currentTick = event.getServer().getTickCount();
        Iterator<Map.Entry<UUID, DelayedTask>> iterator = delayedTasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, DelayedTask> entry = iterator.next();
            DelayedTask task = entry.getValue();

            if (currentTick >= task.targetTick) {
                LOGGER.debug("Executing delayed task {} at tick {}", entry.getKey(), currentTick);

                if (task.callback != null) {
                    try {
                        task.callback.run();
                    } catch (Exception e) {
                        LOGGER.error("Error executing delayed task callback: {}", e.getMessage(), e);
                    }
                }

                iterator.remove();
            }
        }
    }

    private static class DelayedTask {
        final long targetTick;
        final Runnable callback;
        final UUID playerId;

        DelayedTask(long targetTick, Runnable callback, UUID playerId) {
            this.targetTick = targetTick;
            this.callback = callback;
            this.playerId = playerId;
        }
    }

    // Register the event handler
    static {
        MinecraftForge.EVENT_BUS.register(DelayAbilityHandler.class);
    }
}


