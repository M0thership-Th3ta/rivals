package net.anemoia.rivals.common.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Hero {
    private String name;
    private int color;
    private int health;
    private int defense;
    @SerializedName("safe_fall_distance")
    private int safeFallDistance;
    private List<Ability> abilities;

    public String getName() { return name; }
    public int getColor() { return color; }
    public int getHealth() { return health; }
    public int getDefense() { return defense; }
    public int getSafeFallDistance() { return safeFallDistance; }
    public List<Ability> getAbilities() { return abilities; }

    public static class Ability {
        @SerializedName("ability_name")
        private String abilityName;
        @SerializedName("ability_trigger")
        private AbilityTrigger abilityTrigger;
        private AbilityData ability;
        @SerializedName("ability_cooldown")
        private int abilityCooldown;

        public String getAbilityName() { return abilityName; }
        public AbilityTrigger getAbilityTrigger() { return abilityTrigger; }
        public AbilityData getAbility() { return ability; }
        public int getAbilityCooldown() { return abilityCooldown; }
    }

    public static class AbilityTrigger {
        private String type;
        private String key;

        public String getType() { return type; }
        public String getKey() { return key; }
    }

    public static class AbilityData {
        @SerializedName("ability_type")
        private String abilityType;
        @SerializedName("ability_path")
        private String abilityPath;
        @SerializedName("ability_attributes")
        private Map<String, Object> abilityAttributes;

        public String getAbilityType() { return abilityType; }
        public String getAbilityPath() { return abilityPath; }
        public Map<String, Object> getAbilityAttributes() { return abilityAttributes; }
    }
}
