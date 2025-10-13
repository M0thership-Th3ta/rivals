package net.anemoia.rivals.common.capabilities;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.common.util.PlayerHeroUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityEvents.class);

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            // Don't try to access player name during entity construction
            LOGGER.debug("Attaching PlayerHeroDataCapability to player entity");
            event.addCapability(new ResourceLocation(Rivals.MOD_ID, "player_hero_data"),
                    new PlayerHeroDataCapability.Provider());
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        // Safe to access player name here since the player is fully constructed
        LOGGER.debug("Cloning player data from {} to {}",
                event.getOriginal().getName().getString(),
                event.getEntity().getName().getString());

        event.getOriginal().getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA).ifPresent(oldData -> {
            event.getEntity().getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA).ifPresent(newData -> {
                ResourceLocation currentHero = oldData.getCurrentHero();
                LOGGER.debug("Transferring hero {} from old player to new player", currentHero);

                newData.setCurrentHero(currentHero);
                for (String ability : oldData.getActiveAbilities()) {
                    newData.addActiveAbility(ability);
                }
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            LOGGER.debug("Player {} logged in, checking hero data persistence",
                    event.getEntity().getName().getString());

            // Log the current hero state for debugging
            ResourceLocation currentHero = PlayerHeroUtil.getCurrentHero(event.getEntity());
            LOGGER.debug("Player {} current hero after login: {}",
                    event.getEntity().getName().getString(), currentHero);
        } catch (Exception e) {
            LOGGER.warn("Error checking hero data for player {}: {}",
                    event.getEntity().getName().getString(), e.getMessage());
        }
    }


    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        String playerName = "Unknown";
        try {
            playerName = event.getEntity().getName().getString();
            LOGGER.debug("Player {} logged out", playerName);

            // Log the hero state before logout for debugging
            ResourceLocation currentHero = PlayerHeroUtil.getCurrentHero(event.getEntity());
            LOGGER.debug("Player {} had hero {} before logout", playerName, currentHero);
        } catch (Exception e) {
            LOGGER.warn("Error during player logout for {}: {}", playerName, e.getMessage());
        }
    }

}

