package net.anemoia.rivals.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class PlayerGeoAnimatable implements GeoAnimatable {
    private final AbstractClientPlayer player;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final @Nullable ResourceLocation animationResource;

    public PlayerGeoAnimatable(@NotNull AbstractClientPlayer player) {
        this(player, null);
    }

    public PlayerGeoAnimatable(@NotNull AbstractClientPlayer player, @Nullable ResourceLocation animationResource) {
        this.player = player;
        this.animationResource = animationResource;
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk_idle", 0, state -> {
            String walkAnimation = buildAnimationId("walk");
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().then(walkAnimation, Animation.LoopType.LOOP));
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public double getTick(Object ignored) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return 0.0;
        return mc.level.getGameTime() + mc.getFrameTime();
    }

    private String buildAnimationId(String key) {
        String base = extractBaseNameFromAnimationResource();
        if (base == null || base.isBlank()) {
            return "move." + key;
        }
        return "animation." + base + "." + key;
    }

    private @Nullable String extractBaseNameFromAnimationResource() {
        if (animationResource == null) return null;
        String path = animationResource.getPath();
        int lastSlash = path.lastIndexOf('/');
        String fileName = (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
        if (fileName.endsWith(".animation.json")) {
            return fileName.substring(0, fileName.length() - ".animation.json".length());
        } else if (fileName.endsWith(".json")) {
            return fileName.substring(0, fileName.length() - ".json".length());
        } else {
            return fileName;
        }
    }
}
