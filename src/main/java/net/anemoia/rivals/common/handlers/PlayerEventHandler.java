package net.anemoia.rivals.common.handlers;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.common.handlers.abilities.ResourceAbilityHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            ResourceAbilityHandler.tickRecharge(event.player);
        }
    }
}
