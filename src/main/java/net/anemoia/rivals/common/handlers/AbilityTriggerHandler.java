package net.anemoia.rivals.common.handlers;

import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.handlers.abilities.*;
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
            PlayerHeroUtil.setCurrentHero(player, null);
            return;
        }

        if (hero.getAbilities() == null) {
            LOGGER.debug("Hero {} has no abilities defined", heroId);
            return;
        }

        for (Hero.Ability ability : hero.getAbilities()) {
            if (shouldTriggerAbility(ability, keyPressed)) {
                String abilityName = ability.getAbilityName();

                // --- Move this check up so it applies to all abilities ---
                if (AbilityExecutionState.isPlayerExecuting(player)) {
                    LOGGER.debug("Player {} already has an ability executing, ignoring trigger for {}",
                            player.getName().getString(), abilityName);
                    return;
                }

                // Charge/cooldown logic follows...
                if (ResourceAbilityHandler.getMaxCharges(player, abilityName) > 0) {
                    // Charge-based: only check charges
                } else {
                    if (AbilityCooldown.isOnCooldown(player, abilityName)) {
                        AbilityCooldown.sendCooldownMessage(player, abilityName);
                        return;
                    }
                }

                boolean success = executeTriggeredAbility(player, ability);

                if (success && ability.getAbilityCooldown() > 0 && ResourceAbilityHandler.getMaxCharges(player, abilityName) == 0) {
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
        String abilityName = ability.getAbilityName();

        // Charge-based abilities
        if (ResourceAbilityHandler.getMaxCharges(player, abilityName) > 0) {
            if (!ResourceAbilityHandler.hasCharges(player, abilityName)) {
                ResourceAbilityHandler.sendNoChargesMessage(player, abilityName);
                return false;
            }
            if ("rivals:custom_ability".equals(abilityType)) {
                // Only check charges, let CustomAbilityHandler consume
                String abilityPath = ability.getAbility().getAbilityPath();
                if (abilityPath != null) {
                    Object customAbilityData = AbilityReader.loadCustomAbility(abilityPath);
                    if (customAbilityData != null) {
                        CustomAbilityHandler.applyCustomAbility(player, ability, customAbilityData);
                        return true;
                    }
                }
            } else {
                // For direct abilities, consume charge here
                if (!ResourceAbilityHandler.consumeCharge(player, abilityName, ability.getAbilityCooldown())) {
                    ResourceAbilityHandler.sendNoChargesMessage(player, abilityName);
                    return false;
                }
                // Execute direct ability logic
                switch (abilityType) {
                    case "rivals:dash":
                        DashAbilityHandler.handleDashAbility(player, ability);
                        return true;
                    case "rivals:flight":
                        // Implement flight logic here
                        return true;
                    case "rivals:cosmetic_effect":
                        CosmeticEffectAbilityHandler.handleCosmeticEffect(player, ability.getAbility().getAbilityAttributes());
                        return true;
                    default:
                        return false;
                }
            }
            return true;
        } else {
            // Non-charge abilities: check cooldown
            if (AbilityCooldown.isOnCooldown(player, abilityName)) {
                AbilityCooldown.sendCooldownMessage(player, abilityName);
                return false;
            }
            boolean success = false;
            switch (abilityType) {
                case "rivals:custom_ability":
                    String abilityPath = ability.getAbility().getAbilityPath();
                    if (abilityPath != null) {
                        Object customAbilityData = AbilityReader.loadCustomAbility(abilityPath);
                        if (customAbilityData != null) {
                            CustomAbilityHandler.applyCustomAbility(player, ability, customAbilityData);
                            success = true;
                        }
                    }
                    break;
                case "rivals:dash":
                    DashAbilityHandler.handleDashAbility(player, ability);
                    success = true;
                    break;
                case "rivals:flight":
                    // Implement flight logic here
                    success = true;
                    break;
                case "rivals:cosmetic_effect":
                    CosmeticEffectAbilityHandler.handleCosmeticEffect(player, ability.getAbility().getAbilityAttributes());
                    success = true;
                    break;
                default:
                    return false;
            }
            if (success && ability.getAbilityCooldown() > 0) {
                AbilityCooldown.setCooldown(player, abilityName, ability.getAbilityCooldown());
            }
            return success;
        }
    }
}

