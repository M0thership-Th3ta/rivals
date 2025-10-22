package net.anemoia.rivals.common.handlers.abilities;

import net.anemoia.rivals.common.data.Hero;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomAbilityHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAbilityHandler.class);
    private static final Map<Player, Hero.Ability> currentAbilityContext = new HashMap<>();

    public static void applyCustomAbility(Player player, Hero.Ability ability, Object customAbilityData) {
        if (!(customAbilityData instanceof Map)) {
            LOGGER.warn("Custom ability data is not a map for ability {}", ability.getAbilityName());
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> abilityMap = (Map<String, Object>) customAbilityData;

        currentAbilityContext.put(player, ability);
        AbilityExecutionState.setPlayerExecuting(player, true);

        String customAbilityType = (String) abilityMap.get("custom_ability");
        if ("chainable".equals(customAbilityType)) {
            // Find resource initialization in the chain
            int resourceAmount = 0;
            Object abilityChainObj = abilityMap.get("ability_chain");
            if (abilityChainObj instanceof List) {
                List<Object> abilityChain = (List<Object>) abilityChainObj;
                for (Object chainElement : abilityChain) {
                    if (chainElement instanceof Map) {
                        Map<String, Object> elementMap = (Map<String, Object>) chainElement;
                        if (elementMap.containsKey("ability")) {
                            Map<String, Object> abilityData = (Map<String, Object>) elementMap.get("ability");
                            if ("rivals:resource".equals(abilityData.get("ability_type"))) {
                                Map<String, Object> attributes = (Map<String, Object>) abilityData.get("ability_attributes");
                                if (attributes != null && "charge".equals(attributes.get("resource_type"))) {
                                    Number amountObj = (Number) attributes.get("resource_amount");
                                    if (amountObj != null) {
                                        resourceAmount = amountObj.intValue();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Only initialize charges if not already set
            if (resourceAmount > 0) {
                String abilityName = ability.getAbilityName();
                if (ResourceAbilityHandler.getMaxCharges(player, abilityName) == 0) {
                    ResourceAbilityHandler.initializeCharges(player, abilityName, resourceAmount);
                }
                // Only check for >0 charges and consume ONE charge per use
                if (!ResourceAbilityHandler.hasCharges(player, abilityName)) {
                    ResourceAbilityHandler.sendNoChargesMessage(player, abilityName);
                    AbilityExecutionState.setPlayerExecuting(player, false);
                    currentAbilityContext.remove(player);
                    return;
                }
                if (!ResourceAbilityHandler.consumeCharge(player, abilityName, ability.getAbilityCooldown())) {
                    ResourceAbilityHandler.sendNoChargesMessage(player, abilityName);
                    AbilityExecutionState.setPlayerExecuting(player, false);
                    currentAbilityContext.remove(player);
                    return;
                }
            }
            handleChainableAbility(player, abilityMap, () -> {
                AbilityExecutionState.setPlayerExecuting(player, false);
                currentAbilityContext.remove(player);
            });
        } else {
            LOGGER.warn("Unknown custom ability type: {}", customAbilityType);
            AbilityExecutionState.setPlayerExecuting(player, false);
            currentAbilityContext.remove(player);
        }
    }

    public static void removeCustomAbility(Player player, Hero.Ability ability) {
        LOGGER.info("Removing custom ability {} from player {}", ability.getAbilityName(), player.getName().getString());
        currentAbilityContext.remove(player);
    }

    public static Hero.Ability getCurrentAbilityContext(Player player) {
        return currentAbilityContext.get(player);
    }

    @SuppressWarnings("unchecked")
    private static void handleChainableAbility(Player player, Map<String, Object> abilityData, Runnable onComplete) {
        Object abilityChainObj = abilityData.get("ability_chain");

        if (!(abilityChainObj instanceof List)) {
            LOGGER.warn("Chainable ability missing ability_chain for player {}",
                    player.getName().getString());
            onComplete.run();
            return;
        }

        List<Object> abilityChain = (List<Object>) abilityChainObj;
        LOGGER.info("Executing chainable ability with {} elements for player {}",
                abilityChain.size(), player.getName().getString());

        executeAbilityChain(player, abilityChain, 0, onComplete);
    }

    @SuppressWarnings("unchecked")
    private static void executeAbilityChain(Player player, List<Object> abilityChain, int currentIndex, Runnable onComplete) {
        if (currentIndex >= abilityChain.size()) {
            LOGGER.debug("Ability chain execution completed for player {}",
                    player.getName().getString());
            onComplete.run();
            return;
        }

        Object chainElement = abilityChain.get(currentIndex);

        if (!(chainElement instanceof Map)) {
            LOGGER.warn("Invalid chain element at index {} for player {}",
                    currentIndex, player.getName().getString());
            executeAbilityChain(player, abilityChain, currentIndex + 1, onComplete);
            return;
        }

        Map<String, Object> elementMap = (Map<String, Object>) chainElement;

        if (elementMap.containsKey("ability")) {
            Map<String, Object> abilityData = (Map<String, Object>) elementMap.get("ability");
            executeChainElement(player, abilityData, () ->
                    executeAbilityChain(player, abilityChain, currentIndex + 1, onComplete));
        } else if (elementMap.containsKey("ability_chain")) {
            List<Object> nestedChain = (List<Object>) elementMap.get("ability_chain");
            executeAbilityChain(player, nestedChain, 0, () ->
                    executeAbilityChain(player, abilityChain, currentIndex + 1, onComplete));
        } else {
            LOGGER.warn("Chain element missing 'ability' or 'ability_chain' for player {}",
                    player.getName().getString());
            executeAbilityChain(player, abilityChain, currentIndex + 1, onComplete);
        }
    }

    @SuppressWarnings("unchecked")
    private static void executeChainElement(Player player, Map<String, Object> abilityData, Runnable callback) {
        String abilityType = (String) abilityData.get("ability_type");
        Map<String, Object> attributes = (Map<String, Object>) abilityData.get("ability_attributes");

        if (abilityType == null) {
            LOGGER.warn("Chain element missing ability_type for player {}",
                    player.getName().getString());
            callback.run();
            return;
        }

        LOGGER.debug("Executing chain element {} for player {}", abilityType,
                player.getName().getString());

        switch (abilityType) {
            case "rivals:resource":
                handleResourceAbility(player, attributes);
                callback.run();
                break;

            case "rivals:cosmetic_effect":
                handleCosmeticEffectWithCallback(player, attributes, callback);
                break;

            case "rivals:delay":
                handleDelayAbility(player, attributes, callback);
                break;

            case "rivals:dash":
                DashAbilityHandler.handleDashAbility(player, attributes);
                callback.run();
                break;

            default:
                LOGGER.warn("Unknown ability type in chain: {}", abilityType);
                callback.run();
        }
    }

    private static void handleResourceAbility(Player player, Map<String, Object> abilityData) {
        if (abilityData == null) {
            LOGGER.warn("Resource ability has null attributes for player {}", player.getName().getString());
            return;
        }

        String resourceType = (String) abilityData.get("resource_type");
        Number resourceAmountObj = (Number) abilityData.get("resource_amount");

        if (resourceType == null || resourceAmountObj == null) {
            LOGGER.warn("Resource ability missing required attributes for player {}", player.getName().getString());
            return;
        }

        int resourceAmount = resourceAmountObj.intValue();

        LOGGER.info("Executing resource ability for player {} with type {} and amount {}",
                player.getName().getString(), resourceType, resourceAmount);

        if ("charge".equals(resourceType)) {
            Hero.Ability currentAbility = getCurrentAbilityContext(player);
            if (currentAbility != null) {
                String abilityName = currentAbility.getAbilityName();
                // Only initialize if not already set
                if (ResourceAbilityHandler.getMaxCharges(player, abilityName) == 0) {
                    ResourceAbilityHandler.initializeCharges(player, abilityName, resourceAmount);
                    LOGGER.debug("Initialized {} charges for ability {}", resourceAmount, abilityName);
                }
            } else {
                LOGGER.warn("No current ability context for resource initialization");
            }
        }
    }

    private static void handleCosmeticEffectWithCallback(Player player, Map<String, Object> abilityData, Runnable callback) {
        if (abilityData == null) {
            callback.run();
            return;
        }

        Number delayDurationObj = (Number) abilityData.get("delay_duration");

        if (delayDurationObj != null && delayDurationObj.intValue() > 0) {
            int delayDuration = delayDurationObj.intValue();

            // Execute the cosmetic effect first
            CosmeticEffectAbilityHandler.handleCosmeticEffect(player, abilityData);

            // Then schedule the callback after the delay
            DelayAbilityHandler.scheduleDelayedExecution(player, delayDuration, callback);
        } else {
            // No delay, execute effect and continue immediately
            CosmeticEffectAbilityHandler.handleCosmeticEffect(player, abilityData);
            callback.run();
        }
    }

    private static void handleDelayAbility(Player player, Map<String, Object> abilityData, Runnable callback) {
        if (abilityData == null) {
            callback.run();
            return;
        }

        Number delayDurationObj = (Number) abilityData.get("delay_duration");

        if (delayDurationObj == null) {
            LOGGER.warn("Delay ability missing delay_duration for player {}",
                    player.getName().getString());
            callback.run();
            return;
        }

        int delayDuration = delayDurationObj.intValue();

        LOGGER.debug("Executing delay ability for player {} with {} tick delay",
                player.getName().getString(), delayDuration);

        if (delayDuration > 0) {
            DelayAbilityHandler.scheduleDelayedExecution(player, delayDuration, callback);
        } else {
            callback.run();
        }
    }
}


