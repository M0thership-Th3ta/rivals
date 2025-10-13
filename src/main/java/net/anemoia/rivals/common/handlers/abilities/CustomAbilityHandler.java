package net.anemoia.rivals.common.handlers.abilities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.anemoia.rivals.common.data.Hero;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CustomAbilityHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAbilityHandler.class);
    private static final Gson GSON = new Gson();

    public static void applyCustomAbility(Player player, Hero.Ability ability, Object customAbilityData) {
        LOGGER.info("Applying custom ability {} to player {}",
                ability.getAbilityName(), player.getName().getString());

        if (!(customAbilityData instanceof Map)) {
            LOGGER.error("Custom ability data is not a valid JSON object");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> abilityMap = (Map<String, Object>) customAbilityData;

        String customAbilityType = (String) abilityMap.get("custom_ability");

        if ("chainable".equals(customAbilityType)) {
            handleChainableAbility(player, abilityMap);
        } else {
            LOGGER.warn("Unknown custom ability type: {}", customAbilityType);
        }
    }

    public static void removeCustomAbility(Player player, Hero.Ability ability) {
        LOGGER.info("Removing custom ability {} from player {}",
                ability.getAbilityName(), player.getName().getString());
    }

    @SuppressWarnings("unchecked")
    private static void handleChainableAbility(Player player, Map<String, Object> abilityData) {
        Object abilityChainObj = abilityData.get("ability_chain");

        if (!(abilityChainObj instanceof List)) {
            LOGGER.error("ability_chain is not a valid list");
            return;
        }

        List<Object> abilityChain = (List<Object>) abilityChainObj;
        executeAbilityChainSequential(player, abilityChain, 0);
    }

    @SuppressWarnings("unchecked")
    private static void executeAbilityChainSequential(Player player, List<Object> abilityChain, int index) {
        if (index >= abilityChain.size()) return;

        Object chainElement = abilityChain.get(index);

        if (!(chainElement instanceof Map)) {
            LOGGER.error("Chain element {} is not a valid object", index);
            executeAbilityChainSequential(player, abilityChain, index + 1);
            return;
        }

        Map<String, Object> elementMap = (Map<String, Object>) chainElement;

        if (elementMap.containsKey("ability_chain")) {
            LOGGER.debug("Executing nested ability chain in parallel");
            Object nestedChainObj = elementMap.get("ability_chain");

            if (nestedChainObj instanceof List) {
                List<Object> nestedChain = (List<Object>) nestedChainObj;
                executeParallelAbilities(player, nestedChain);
            }
            // Continue to next element immediately for parallel chains
            executeAbilityChainSequential(player, abilityChain, index + 1);

        } else if (elementMap.containsKey("ability")) {
            executeAbilityElementWithCallback(player, elementMap, () -> {
                // Continue to next element after this one completes
                executeAbilityChainSequential(player, abilityChain, index + 1);
            });
        } else {
            LOGGER.warn("Chain element {} contains neither 'ability' nor 'ability_chain'", index);
            executeAbilityChainSequential(player, abilityChain, index + 1);
        }
    }

    @SuppressWarnings("unchecked")
    private static void executeParallelAbilities(Player player, List<Object> abilities) {
        for (Object abilityObj : abilities) {
            if (!(abilityObj instanceof Map)) {
                LOGGER.error("Parallel ability element is not a valid object");
                continue;
            }

            Map<String, Object> abilityMap = (Map<String, Object>) abilityObj;

            CompletableFuture.runAsync(() -> {
                executeAbilityElementWithCallback(player, abilityMap, null);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static void executeAbilityElementWithCallback(Player player, Map<String, Object> elementMap, Runnable callback) {
        Object abilityObj = elementMap.get("ability");

        if (!(abilityObj instanceof Map)) {
            LOGGER.error("Ability element is not a valid object");
            if (callback != null) callback.run();
            return;
        }

        Map<String, Object> abilityData = (Map<String, Object>) abilityObj;
        String abilityType = (String) abilityData.get("ability_type");

        if (abilityType == null) {
            LOGGER.error("Ability missing ability_type");
            if (callback != null) callback.run();
            return;
        }

        Hero.Ability tempAbility = createTempAbility(elementMap, abilityData);

        // Route to appropriate handler based on ability type
        switch (abilityType) {
            case "rivals:cosmetic_effect":
                CosmeticEffectAbilityHandler.handleCosmeticEffect(player, tempAbility);
                if (callback != null) callback.run();
                break;
            case "rivals:delay":
                DelayAbilityHandler.handleDelayAbility(player, tempAbility, callback);
                break;
            case "rivals:dash":
                DashAbilityHandler.handleDashAbility(player, tempAbility);
                if (callback != null) callback.run();
                break;
            case "rivals:flight":
                FlightHandler.handleFlightAbility(player, tempAbility);
                if (callback != null) callback.run();
                break;
            default:
                LOGGER.warn("Unknown ability type in chain: {}", abilityType);
                if (callback != null) callback.run();
        }
    }

    @SuppressWarnings("unchecked")
    private static Hero.Ability createTempAbility(Map<String, Object> elementMap, Map<String, Object> abilityData) {
        JsonObject tempAbilityJson = new JsonObject();

        if (elementMap.containsKey("ability_name")) {
            tempAbilityJson.addProperty("ability_name", (String) elementMap.get("ability_name"));
        }

        if (elementMap.containsKey("ability_trigger")) {
            JsonElement triggerElement = GSON.toJsonTree(elementMap.get("ability_trigger"));
            tempAbilityJson.add("ability_trigger", triggerElement);
        }

        if (elementMap.containsKey("ability_cooldown")) {
            Number cooldown = (Number) elementMap.get("ability_cooldown");
            tempAbilityJson.addProperty("ability_cooldown", cooldown.intValue());
        }

        JsonElement abilityElement = GSON.toJsonTree(abilityData);
        tempAbilityJson.add("ability", abilityElement);

        return GSON.fromJson(tempAbilityJson, Hero.Ability.class);
    }
}

