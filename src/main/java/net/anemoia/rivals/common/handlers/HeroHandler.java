package net.anemoia.rivals.common.handlers;

import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.handlers.abilities.AbilityCooldown;
import net.anemoia.rivals.common.handlers.attributes.HeroHealthHandler;
import net.anemoia.rivals.common.util.PlayerHeroUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeroHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeroHandler.class);

    public static void applyHero(Player player, ResourceLocation heroId) {
        Hero hero = HeroDataManager.getInstance().getHero(heroId);
        if (hero == null) {
            LOGGER.warn("Attempted to apply unknown hero: {}", heroId);
            return;
        }

        LOGGER.info("Applying hero {} to player {}", heroId, player.getName().getString());

        // Store the current hero
        PlayerHeroUtil.setCurrentHero(player, heroId);

        // Apply all hero properties
        HeroHealthHandler.applyHeroHealth(player, heroId);
        AbilityReader.applyAbilities(player, hero);
        // Future handlers can be added here:
        // HeroSpeedHandler.applySpeed(player, heroId);
        // HeroAnimationHandler.applyAnimations(player, heroId);
        LOGGER.info("Successfully applied hero {} to player {}", hero.getName(), player.getName().getString());
    }

    public static void removeHero(Player player) {
        ResourceLocation currentHeroId = null;

        try {
            currentHeroId = PlayerHeroUtil.getCurrentHero(player);
        } catch (Exception e) {
            LOGGER.warn("Error getting current hero for player {}: {}", player.getName().getString(), e.getMessage());
        }

        // Always try to remove hero effects, even if currentHeroId is null
        if (currentHeroId != null) {
            Hero hero = HeroDataManager.getInstance().getHero(currentHeroId);
            if (hero != null) {
                LOGGER.info("Removing hero {} from player {}", currentHeroId, player.getName().getString());
                try {
                    AbilityReader.removeAbilities(player, hero);
                } catch (Exception e) {
                    LOGGER.error("Error removing abilities for player {}: {}", player.getName().getString(), e.getMessage());
                }
            }
        } else {
            LOGGER.info("No hero found for player {}, but cleaning up anyway", player.getName().getString());
        }

        // Clear the current hero regardless
        try {
            PlayerHeroUtil.setCurrentHero(player, null);
        } catch (Exception e) {
            LOGGER.warn("Error clearing hero for player {}: {}", player.getName().getString(), e.getMessage());
        }

        try {
            AbilityCooldown.clearPlayerCooldowns(player);
        } catch (Exception e) {
            LOGGER.warn("Error clearing cooldowns for player {}: {}", player.getName().getString(), e.getMessage());
        }

        // Remove all hero properties
        try {
            HeroHealthHandler.removeHeroHealth(player);
        } catch (Exception e) {
            LOGGER.warn("Error removing hero health for player {}: {}", player.getName().getString(), e.getMessage());
        }

        LOGGER.info("Successfully removed hero from player {}", player.getName().getString());
    }
}
