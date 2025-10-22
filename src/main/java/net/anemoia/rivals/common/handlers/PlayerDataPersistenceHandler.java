package net.anemoia.rivals.common.handlers;

import net.anemoia.rivals.common.handlers.abilities.AbilityCooldown;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerDataPersistenceHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        AbilityCooldown.loadCooldownsFromNBT(player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        AbilityCooldown.saveCooldownsToNBT(player);
    }
}
