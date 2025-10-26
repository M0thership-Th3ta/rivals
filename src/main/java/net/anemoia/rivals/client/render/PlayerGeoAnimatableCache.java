package net.anemoia.rivals.client.render;

import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerGeoAnimatableCache {
    private static final PlayerGeoAnimatableCache INSTANCE = new PlayerGeoAnimatableCache();
    private final Map<UUID, PlayerGeoAnimatable> cache = new ConcurrentHashMap<>();

    private PlayerGeoAnimatableCache() {}

    public static PlayerGeoAnimatableCache getInstance() {
        return INSTANCE;
    }

    public PlayerGeoAnimatable getOrCreate(AbstractClientPlayer player, ResourceLocation heroId) {
        var id = player.getUUID();
        return cache.compute(id, (uuid, existing) -> {
            if (existing != null) return existing;
            ResourceLocation animationResource = null;
            if (heroId != null) {
                Hero hero = HeroDataManager.getInstance().getHero(heroId);
                if (hero != null && hero.getPlayerModel() != null && !hero.getPlayerModel().isEmpty()) {
                    String animPath = hero.getPlayerModel().get(0).getAnimationPath();
                    if (animPath != null && !animPath.isBlank()) {
                        try {
                            animationResource = new ResourceLocation(animPath);
                        } catch (Exception ignored) {
                            // malformed resource location -> leave animationResource null to fall back
                        }
                    }
                }
            }

            return new PlayerGeoAnimatable(player, animationResource);
        });
    }

    public void invalidate(UUID playerUuid) {
        cache.remove(playerUuid);
    }

    public void clear() {
        cache.clear();
    }
}
