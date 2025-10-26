package net.anemoia.rivals.client.render;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHeroState {
    private static final Map<UUID, ResourceLocation> HERO_MAP = new ConcurrentHashMap<>();

    public static void setHeroFor(UUID playerUuid, ResourceLocation heroId) {
        if (heroId == null) HERO_MAP.remove(playerUuid);
        else HERO_MAP.put(playerUuid, heroId);
    }

    public static ResourceLocation getHeroFor(UUID playerUuid) {
        return HERO_MAP.get(playerUuid);
    }

    public static void clear() {
        HERO_MAP.clear();
    }

    public static Map<UUID, ResourceLocation> snapshot() {
        return Map.copyOf(HERO_MAP);
    }

    private ClientHeroState() {}
}
