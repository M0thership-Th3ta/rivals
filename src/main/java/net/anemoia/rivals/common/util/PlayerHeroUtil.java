package net.anemoia.rivals.common.util;

import net.anemoia.rivals.common.capabilities.PlayerHeroDataCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerHeroUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerHeroUtil.class);

    public static ResourceLocation getCurrentHero(Player player) {
        return player.getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA)
                .map(cap -> cap.getCurrentHero()) // Remove the logging and Optional.of() wrapping
                .orElse(null);
    }

    public static void setCurrentHero(Player player, ResourceLocation heroId) {
        player.getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA)
                .ifPresent(data -> {
                    data.setCurrentHero(heroId);
                    LOGGER.debug("Set current hero for {} to: {}", player.getName().getString(), heroId);
                });

        if (!player.getCapability(PlayerHeroDataCapability.PLAYER_HERO_DATA).isPresent()) {
            LOGGER.warn("Failed to set hero - PlayerHeroDataCapability not found for player {}", player.getName().getString());
        }
    }

    public static boolean hasHero(Player player) {
        return getCurrentHero(player) != null;
    }
}



