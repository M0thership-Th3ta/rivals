package net.anemoia.rivals.common.capabilities;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.common.util.PlayerHeroUtil;
import net.minecraft.nbt.CompoundTag;
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
    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Rivals.MOD_ID, "player_hero_data");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            LOGGER.debug("Attaching PlayerHeroDataCapability to player entity: {}", player.getUUID());
            event.addCapability(CAPABILITY_ID, new PlayerHeroDataCapability.Provider(player));
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        if (oldPlayer == null || newPlayer == null) return;

        LOGGER.debug("Cloning player data from {} to {}", oldPlayer.getUUID(), newPlayer.getUUID());

        oldPlayer.getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA).ifPresent(oldCap -> {
            CompoundTag nbt = oldCap.serializeNBT();
            newPlayer.getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA).ifPresent(newCap -> {
                // Deserialize into new capability then ensure owner is set to the new player
                newCap.deserializeNBT(nbt);
                newCap.setOwner(newPlayer);
                LOGGER.debug("Transferred capability data and set owner for {}", newPlayer.getUUID());
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        player.getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA).ifPresent(cap -> {
            cap.setOwner(player);
            LOGGER.debug("Set capability owner on login for {}", player.getUUID());
        });
    }


    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        var opt = player.getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA);
        if (opt.isPresent()) {
            opt.ifPresent(cap -> {
                ResourceLocation currentHero = PlayerHeroUtil.getCurrentHero(player);
                LOGGER.debug("Player {} logged out with hero {}", player.getUUID(), currentHero);
            });
        } else {
            LOGGER.debug("Player {} logged out with no PlayerHeroDataCapability attached", player.getUUID());
        }
    }
}

