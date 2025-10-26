package net.anemoia.rivals.client.render;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class HeroModelCache {
    private static final Map<ResourceLocation, HeroModelEntry> CACHE = new ConcurrentHashMap<>();

    public record HeroModelEntry(ResourceLocation model, ResourceLocation animation, ResourceLocation texture) {}

    private HeroModelCache() {}

    public static void register(ResourceLocation heroId, HeroModelEntry entry) {
        CACHE.put(Objects.requireNonNull(heroId), Objects.requireNonNull(entry));
    }

    @Nullable
    public static HeroModelEntry get(ResourceLocation heroId) {
        return CACHE.get(heroId);
    }

    public static HeroModelEntry getOrCreateFromConventions(ResourceLocation heroId) {
        var cached = get(heroId);
        if (cached != null) return cached;

        String namespace = heroId.getNamespace();
        String path = heroId.getPath();

        ResourceLocation model = new ResourceLocation(namespace, "geo/" + path + ".geo.json");
        ResourceLocation texture = new ResourceLocation(namespace, "textures/heroes" + path + ".png");
        ResourceLocation animation = new ResourceLocation(namespace, "animations/heroes" + path + "/" + path + ".animation.json");

        HeroModelEntry entry = new HeroModelEntry(model, animation, texture);
        register(heroId, entry);
        return entry;
    }
}
