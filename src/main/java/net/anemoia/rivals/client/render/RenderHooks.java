package net.anemoia.rivals.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RenderHooks {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderHooks.class);
    private static final Map<ResourceLocation, HeroGeoModel<PlayerGeoAdapter>> MODEL_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, HeroGeoRenderer<PlayerGeoAdapter>> RENDERER_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerGeoAdapter> ADAPTER_CACHE = new ConcurrentHashMap<>();
    private static EntityRendererProvider.Context RENDER_CONTEXT;

    private RenderHooks() {}

    public static void setRenderContext(EntityRendererProvider.Context context) {
        RENDER_CONTEXT = Objects.requireNonNull(context, "render context");
    }

    public static boolean renderPlayerGeo(AbstractClientPlayer player, PlayerGeoAnimatable animatable, ResourceLocation heroId, PoseStack poseStack, MultiBufferSource buffers, int packedLight, float partialTicks) {
        if (heroId == null || animatable == null) return false;

        if (RENDER_CONTEXT == null) {
            LOGGER.error("RENDER_CONTEXT is null! Call RenderHooks.setRenderContext(...) during client setup (FMLClientSetupEvent) before attempting to render Geo models.");
            return false;
        }

        Hero hero = HeroDataManager.getInstance().getHero(heroId);
        if (hero == null) {
            LOGGER.debug("RenderHooks: no Hero data found for {}", heroId);
            return false;
        }

        var playerModel = hero.getPlayerModel().get(0);
        ResourceLocation modelResource = tryCreate(playerModel.getModelPath());
        ResourceLocation textureResource = tryCreate(playerModel.getTexturePath());
        ResourceLocation animationResource = tryCreate(playerModel.getAnimationPath());

        if (modelResource == null || textureResource == null || animationResource == null) {
            LOGGER.debug("RenderHooks: incomplete resource paths for hero {} (model={},tex={},anim={})", heroId, modelResource, textureResource, animationResource);
            return false;
        }

        // Cache model by the model resource (geo file)
        MODEL_CACHE.computeIfAbsent(modelResource, id -> {
            LOGGER.debug("RenderHooks: caching HeroGeoModel for model resource {}", id);
            return new HeroGeoModel<>(modelResource, animationResource, textureResource);
        });

        // Ensure we have a renderer for this heroId (use model from MODEL_CACHE)
        RENDERER_CACHE.computeIfAbsent(heroId, id -> {
            HeroGeoModel<PlayerGeoAdapter> model = MODEL_CACHE.get(modelResource);
            if (model == null) {
                // Defensive: should not happen because we just computed the model above
                LOGGER.error("RenderHooks: missing model when creating renderer for {} (model resource {}).", id, modelResource);
                return null;
            }
            LOGGER.debug("RenderHooks: caching HeroGeoRenderer for {}", id);
            return new HeroGeoRenderer<>(RENDER_CONTEXT, model);
        });

        try {
            // Remove stale adapters for players that are gone
            ADAPTER_CACHE.entrySet().removeIf(e -> {
                PlayerGeoAdapter a = e.getValue();
                return a == null || a.getPlayer() == null || a.getPlayer().isRemoved();
            });

            // Cache adapter per player UUID, create with this hero's animation resource
            PlayerGeoAdapter adapter = ADAPTER_CACHE.computeIfAbsent(player.getUUID(), uuid -> new PlayerGeoAdapter(player, animationResource));
            // If the cached adapter's player differs (shouldn't normally), replace it
            if (!Objects.equals(adapter.getPlayer(), player)) {
                adapter = new PlayerGeoAdapter(player, animationResource);
                ADAPTER_CACHE.put(player.getUUID(), adapter);
            }

            adapter.tick(); // Sync position/rotation

            HeroGeoRenderer<PlayerGeoAdapter> playerGeoRenderer = RENDERER_CACHE.get(heroId);
            if (playerGeoRenderer == null) {
                LOGGER.error("RenderHooks: renderer missing for hero {} (model {})", heroId, modelResource);
                return false;
            }

            playerGeoRenderer.render(adapter, player.getYRot(), partialTicks, poseStack, buffers, packedLight);
            return true;
        } catch (Exception e) {
            LOGGER.error("RenderHooks: failed to render GeoModel for hero {}: {}", heroId, e.toString());
            return false;
        }
    }

    private static ResourceLocation tryCreate(String path) {
        if (path == null || path.isBlank()) return null;
        try {
            return new ResourceLocation(path);
        } catch (Exception e) {
            LOGGER.debug("Invalid resource location: {}", path);
            return null;
        }
    }
}
