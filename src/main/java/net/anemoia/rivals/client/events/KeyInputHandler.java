package net.anemoia.rivals.client.events;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.common.network.AbilityTriggerPacket;
import net.anemoia.rivals.common.network.NetworkHandler;
import net.anemoia.rivals.client.RivalsKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyInputHandler.class);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Check if primary ability key was pressed
        if (RivalsKeybinds.PRIMARY_ABILITY.consumeClick()) {
            LOGGER.debug("Primary ability key pressed by player {}", mc.player.getName().getString());
            NetworkHandler.sendToServer(new AbilityTriggerPacket("key.rivals.primary"));
        }

        // Check if secondary ability key was pressed
        if (RivalsKeybinds.SECONDARY_ABILITY.consumeClick()) {
            LOGGER.debug("Secondary ability key pressed by player {}", mc.player.getName().getString());
            NetworkHandler.sendToServer(new AbilityTriggerPacket("key.rivals.secondary"));
        }

        // Check if tertiary ability key was pressed
        if (RivalsKeybinds.TERTIARY_ABILITY.consumeClick()) {
            LOGGER.debug("Tertiary ability key pressed by player {}", mc.player.getName().getString());
            NetworkHandler.sendToServer(new AbilityTriggerPacket("key.rivals.tertiary"));
        }
    }
}
