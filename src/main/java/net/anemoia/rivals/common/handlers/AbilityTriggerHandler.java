package net.anemoia.rivals.common.handlers;

import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.handlers.abilities.AbilityCooldown;
import net.anemoia.rivals.common.handlers.abilities.CustomAbilityHandler;
import net.anemoia.rivals.common.util.PlayerHeroUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbilityTriggerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbilityTriggerHandler.class);

    public static void triggerAbility(Player player, String keyPressed) {
        ResourceLocation heroId = PlayerHeroUtil.getCurrentHero(player);

        if (heroId == null) {
            LOGGER.warn("No hero or abilities found for player {}", player.getName().getString());
            return;
        }

        Hero hero = HeroDataManager.getInstance().getHero(heroId);
        if (hero == null) {
            LOGGER.warn("Player {} has invalid hero {}", player.getName().getString(), heroId);
            // Clear invalid hero reference
            PlayerHeroUtil.setCurrentHero(player, null);
            return;
        }

        if (hero.getAbilities() == null) {
            LOGGER.debug("Hero {} has no abilities defined", heroId);
            return;
        }

        // Find abilities that match the key trigger
        for (Hero.Ability ability : hero.getAbilities()) {
            if (shouldTriggerAbility(ability, keyPressed)) {
                LOGGER.info("Attempting to trigger ability {} for player {}",
                        ability.getAbilityName(), player.getName().getString());

                // Check cooldown before executing
                if (AbilityCooldown.isOnCooldown(player, ability.getAbilityName())) {
                    AbilityCooldown.sendCooldownMessage(player, ability.getAbilityName());
                    return;
                }

                // Execute the ability
                boolean success = executeTriggeredAbility(player, ability);

                // Set cooldown if ability executed successfully and has a cooldown
                if (success && ability.getAbilityCooldown() > 0) {
                    AbilityCooldown.setCooldown(player, ability.getAbilityName(), ability.getAbilityCooldown());
                    LOGGER.debug("Set cooldown for ability {} ({} ticks)",
                            ability.getAbilityName(), ability.getAbilityCooldown());
                }
            }
        }
    }

    private static boolean shouldTriggerAbility(Hero.Ability ability, String keyPressed) {
        Hero.AbilityTrigger trigger = ability.getAbilityTrigger();
        if (trigger == null) return false;

        return "keybind".equals(trigger.getType()) && keyPressed.equals(trigger.getKey());
    }

    private static boolean executeTriggeredAbility(Player player, Hero.Ability ability) {
        String abilityType = ability.getAbility().getAbilityType();

        try {
            switch (abilityType) {
                case "rivals:custom_ability":
                    String abilityPath = ability.getAbility().getAbilityPath();
                    if (abilityPath != null) {
                        Object customAbilityData = AbilityReader.loadCustomAbility(abilityPath);
                        if (customAbilityData != null) {
                            CustomAbilityHandler.applyCustomAbility(player, ability, customAbilityData);
                            return true;
                        }
                    }
                    break;
                // Add other ability types as needed
                default:
                    LOGGER.warn("Triggered ability type {} not yet implemented", abilityType);
                    return false;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute ability {} for player {}: {}",
                    ability.getAbilityName(), player.getName().getString(), e.getMessage(), e);
            return false;
        }

        return false;
    }
}

