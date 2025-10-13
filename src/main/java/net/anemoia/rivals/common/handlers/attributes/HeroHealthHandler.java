package net.anemoia.rivals.common.handlers.attributes;
import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class HeroHealthHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeroHealthHandler.class);
    private static final UUID HERO_HEALTH_MODIFIER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final String HERO_HEALTH_MODIFIER_NAME = "Hero Health Modifier";

    public static void applyHeroHealth(Player player, ResourceLocation heroId) {
        Hero hero = HeroDataManager.getInstance().getHero(heroId);
        if (hero == null) {
            LOGGER.warn("Attempted to apply health for unknown hero: {}", heroId);
            return;
        }

        AttributeInstance maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            LOGGER.error("Player {} has no have a MAX_HEALTH attribute", player.getName().getString());
            return;
        }

        removeHeroHealth(player);

        double healthDifference = hero.getHealth() - player.getMaxHealth();
        if (healthDifference != 0) {
            AttributeModifier healthModifier = new AttributeModifier(
                    HERO_HEALTH_MODIFIER_UUID,
                    HERO_HEALTH_MODIFIER_NAME,
                    healthDifference,
                    AttributeModifier.Operation.ADDITION
            );

            maxHealthAttribute.addPermanentModifier(healthModifier);
            LOGGER.info("Applied {} health to player {} (total: {})",
                    healthDifference, player.getName().getString(), hero.getHealth());
        }

        player.setHealth(player.getMaxHealth());
    }

    public static void removeHeroHealth(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null && maxHealthAttribute.getModifier(HERO_HEALTH_MODIFIER_UUID) != null) {
            maxHealthAttribute.removeModifier(HERO_HEALTH_MODIFIER_UUID);

            float currentHealth = player.getHealth();
            float maxHealth = (float) maxHealthAttribute.getValue();

            if (currentHealth > maxHealth) {
                player.setHealth(maxHealth);
            }

            LOGGER.info("Removed hero health modifier from player {}, health set to {}/{}", player.getName().getString(), player.getHealth(), maxHealth);
        }
    }

    public static int getCurrentHeroHealth(Player player) {
        return (int) player.getMaxHealth();
    }
}
