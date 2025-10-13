package net.anemoia.rivals.common.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.handlers.abilities.CustomAbilityHandler;
import net.anemoia.rivals.common.util.PlayerHeroUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class AbilityReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbilityReader.class);
    private static final Gson GSON = new Gson();
    private static final Map<String, Object> customAbilityCache = new HashMap<>();

    public static void applyAbilities(Player player, Hero hero) {
        if (hero.getAbilities() == null) return;

        for (Hero.Ability ability : hero.getAbilities()) {
            try {
                applyAbility(player, ability);
            } catch (Exception e) {
                LOGGER.error("Failed to apply ability {} to player {}", ability.getAbilityName(), player.getName().getString(), e);
            }
        }
    }

    public static void removeAbilities(Player player, Hero hero) {
        if (hero.getAbilities() == null) return;

        for (Hero.Ability ability : hero.getAbilities()) {
            try {
                removeAbility(player, ability);
            } catch (Exception e) {
                LOGGER.error("Failed to remove ability {} from player {}", ability.getAbilityName(), player.getName().getString(), e);
            }
        }
    }

    private static void applyAbility(Player player, Hero.Ability ability) {
        String abilityType = ability.getAbility().getAbilityType();

        // Check if this ability has a keybind trigger - if so, don't execute it immediately
        Hero.AbilityTrigger trigger = ability.getAbilityTrigger();
        if (trigger != null && "keybind".equals(trigger.getType())) {
            LOGGER.debug("Registering keybind ability {} for player {} (will trigger on key press)",
                    ability.getAbilityName(), player.getName().getString());
            return; // Don't execute keybind abilities immediately
        }

        // Only execute passive abilities or abilities without triggers immediately
        switch (abilityType) {
            case "rivals:custom_ability":
                handleCustomAbility(player, ability);
                break;
            case "rivals:dash":
                handleDashAbility(player, ability);
                break;
            case "rivals:flight":
                handleFlightAbility(player, ability);
                break;
            case "rivals:cosmetic_effect":
                handleCosmeticEffect(player, ability);
                break;
            case "rivals:delay":
                handleDelayAbility(player, ability);
                break;
            default:
                LOGGER.warn("Unknown ability type: {}", abilityType);
        }
    }

    private static void removeAbility(Player player, Hero.Ability ability) {
        String abilityType = ability.getAbility().getAbilityType();

        switch (abilityType) {
            case "rivals:custom_ability":
                removeCustomAbility(player, ability);
                break;
            case "rivals:dash":
                removeDashAbility(player, ability);
                break;
            case "rivals:flight":
                removeFlightAbility(player, ability);
                break;
            case "rivals:cosmetic_effect":
                removeCosmeticEffect(player, ability);
                break;
            default:
                LOGGER.warn("Unknown ability type for removal: {}", abilityType);
        }
    }

    private static void handleCustomAbility(Player player, Hero.Ability ability) {
        String abilityPath = ability.getAbility().getAbilityPath();
        if (abilityPath == null) {
            LOGGER.warn("Custom ability missing ability_path");
            return;
        }

        Object customAbilityData = loadCustomAbility(abilityPath);
        if (customAbilityData != null) {
            CustomAbilityHandler.applyCustomAbility(player, ability, customAbilityData);
        }
    }

    private static void handleDashAbility(Player player, Hero.Ability ability) {
        Map<String, Object> attributes = ability.getAbility().getAbilityAttributes();
        if (attributes == null) return;

        // TODO: Create DashAbilityHandler
        LOGGER.info("Applying dash ability to player {} with attributes: {}",
                player.getName().getString(), attributes);
    }

    private static void handleFlightAbility(Player player, Hero.Ability ability) {
        Map<String, Object> attributes = ability.getAbility().getAbilityAttributes();
        if (attributes == null) return;

        // TODO: Create FlightAbilityHandler
        LOGGER.info("Applying flight ability to player {} with attributes: {}",
                player.getName().getString(), attributes);
    }

    private static void handleCosmeticEffect(Player player, Hero.Ability ability) {
        Map<String, Object> attributes = ability.getAbility().getAbilityAttributes();
        if (attributes == null) return;

        // TODO: Create CosmeticEffectHandler
        LOGGER.info("Applying cosmetic effect to player {} with attributes: {}",
                player.getName().getString(), attributes);
    }

    private static void handleDelayAbility(Player player, Hero.Ability ability) {
        Map<String, Object> attributes = ability.getAbility().getAbilityAttributes();
        if (attributes == null) return;

        // TODO: Create DelayAbilityHandler
        LOGGER.info("Applying delay ability to player {} with attributes: {}",
                player.getName().getString(), attributes);
    }

    private static void removeCustomAbility(Player player, Hero.Ability ability) {
        CustomAbilityHandler.removeCustomAbility(player, ability);
    }

    private static void removeDashAbility(Player player, Hero.Ability ability) {
        // TODO: Implement dash removal
    }

    private static void removeFlightAbility(Player player, Hero.Ability ability) {
        // TODO: Implement flight removal
    }

    private static void removeCosmeticEffect(Player player, Hero.Ability ability) {
        // TODO: Implement cosmetic effect removal
    }

    public static Object loadCustomAbility(String abilityPath) {
        if (customAbilityCache.containsKey(abilityPath)) {
            return customAbilityCache.get(abilityPath);
        }

        try {
            ResourceManager resourceManager = ServerLifecycleHooks.getCurrentServer().getResourceManager();
            ResourceLocation location = new ResourceLocation("rivals", abilityPath);
            Resource resource = resourceManager.getResource(location).orElse(null);

            if (resource == null) {
                LOGGER.warn("Custom ability file not found: {}", abilityPath);
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                Object abilityData = GSON.fromJson(reader, Object.class);
                customAbilityCache.put(abilityPath, abilityData);
                return abilityData;
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to load custom ability from {}", abilityPath, e);
            return null;
        }
    }

    public static void clearCustomAbilityCache() {
        customAbilityCache.clear();
    }
}
