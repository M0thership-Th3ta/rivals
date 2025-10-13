package net.anemoia.rivals.common.handlers;

import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.handlers.abilities.AbilityCooldown;
import net.anemoia.rivals.common.handlers.abilities.CustomAbilityHandler;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbilityExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbilityExecutor.class);

    public static boolean executeAbilityWithCooldown(Player player, Hero.Ability ability) {
        String abilityName = ability.getAbilityName();
        int cooldown = ability.getAbilityCooldown();

        // Check if ability is on cooldown
        if (AbilityCooldown.isOnCooldown(player, abilityName)) {
            AbilityCooldown.sendCooldownMessage(player, abilityName);
            return false;
        }

        // Execute the ability
        boolean success = executeAbility(player, ability);

        // Set cooldown if ability executed successfully and has a cooldown
        if (success && cooldown > 0) {
            AbilityCooldown.setCooldown(player, abilityName, cooldown);
        }

        return success;
    }

    private static boolean executeAbility(Player player, Hero.Ability ability) {
        String abilityType = ability.getAbility().getAbilityType();

        try {
            switch (abilityType) {
                case "rivals:custom_ability":
                    Object customAbilityData = AbilityReader.loadCustomAbility(ability.getAbility().getAbilityPath());
                    if (customAbilityData != null) {
                        CustomAbilityHandler.applyCustomAbility(player, ability, customAbilityData);
                        return true;
                    }
                    break;
                case "rivals:dash":
                    // Handle other ability types as needed
                    LOGGER.info("Executing dash ability for player {}", player.getName().getString());
                    return true;
                case "rivals:flight":
                    LOGGER.info("Executing flight ability for player {}", player.getName().getString());
                    return true;
                case "rivals:cosmetic_effect":
                    LOGGER.info("Executing cosmetic effect for player {}", player.getName().getString());
                    return true;
                default:
                    LOGGER.warn("Unknown ability type: {}", abilityType);
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
