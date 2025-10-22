package net.anemoia.rivals.common.handlers.abilities;

import net.anemoia.rivals.common.data.Hero;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DelayAbilityHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayAbilityHandler.class);

    public static void handleDelayAbility(Player player, Hero.Ability ability) {
        Map<String, Object> attributes = ability.getAbility().getAbilityAttributes();
        if (attributes == null) return;

        Object delayObj = attributes.get("delay_duration");
        if (delayObj == null) {
            LOGGER.warn("Delay ability missing delay_duration for player {}", player.getName().getString());
            return;
        }

        int delayTicks = ((Number) delayObj).intValue();
        LOGGER.info("Executing delay ability for player {} with {} tick delay",
                player.getName().getString(), delayTicks);

        // For standalone delay abilities, we just log the delay
        // The actual delay logic is handled in CustomAbilityHandler for chains
    }

    public static void scheduleDelayedExecution(Player player, int delayTicks, Runnable callback) {
        if (delayTicks <= 0) {
            callback.run();
            return;
        }

        LOGGER.debug("Scheduling delayed execution for player {} in {} ticks",
                player.getName().getString(), delayTicks);

        // Use CompletableFuture with the server's scheduled executor
        CompletableFuture.delayedExecutor(delayTicks * 50L, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    try {
                        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                        if (server != null) {
                            server.execute(callback);
                        } else {
                            LOGGER.warn("Server not available for delayed execution");
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error executing delayed ability for player {}: {}",
                                player.getName().getString(), e.getMessage(), e);
                    }
                });
    }
}



