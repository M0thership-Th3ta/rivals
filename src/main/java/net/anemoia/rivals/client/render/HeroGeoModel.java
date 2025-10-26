package net.anemoia.rivals.client.render;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class HeroGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeroGeoModel.class);
    private final ResourceLocation modelLocation;
    private final ResourceLocation animationLocation;
    private final ResourceLocation textureLocation;

    public HeroGeoModel(@NotNull ResourceLocation modelLocation, @NotNull ResourceLocation animationLocation, @NotNull ResourceLocation textureLocation) {
        this.modelLocation = modelLocation;
        this.animationLocation = animationLocation;
        this.textureLocation = textureLocation;
    }

    @Override
    public @NotNull ResourceLocation getModelResource(T animatable) {
        return modelLocation;
    }

    @Override
    public @NotNull ResourceLocation getTextureResource(T animatable) {
        return textureLocation;
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(T animatable) {
        return animationLocation;
    }

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        if (!(animatable instanceof PlayerGeoAdapter adapter)) return;

        CoreGeoBone headBone = this.getAnimationProcessor().getBone("head");
        if (headBone == null) return;

        CoreGeoBone bodyBone = this.getAnimationProcessor().getBone("body");
        CoreGeoBone upperBodyBone = this.getAnimationProcessor().getBone("upper_body");

        // partial tick for interpolation
        float partial = (float) animationState.getPartialTick();

        // Interpolate head and body yaw from adapter
        float headYaw = adapter.yHeadRotO + (adapter.yHeadRot - adapter.yHeadRotO) * partial;
        float bodyYaw = adapter.yBodyRotO + (adapter.yBodyRot - adapter.yBodyRotO) * partial;

        // Compute head relative to body (normalize to -180..180)
        float relativeHeadYaw = wrapDegrees(headYaw - bodyYaw);

        // Clamp head yaw so the model doesn't do an impossible neck twist
        float maxNeckYaw = 70f; // degrees
        float clampedHeadYaw = clamp(relativeHeadYaw, -maxNeckYaw, maxNeckYaw);

        // Apply head yaw (relative) and pitch (player x-rot).
        // Invert sign so the head rotates the correct direction for this model coord system.
        headBone.setRotY((float) Math.toRadians(-clampedHeadYaw));
        headBone.setRotX((float) Math.toRadians(-adapter.getXRot()));

        // Body follow behaviour: only start rotating torso when head turns beyond threshold
        if (bodyBone != null) {
            float followThreshold = 30f; // degrees before torso starts turning
            float maxTorsoTwist = 40f;   // maximum degrees torso can twist
            float followStrength = 0.5f; // fraction of extra head yaw applied to torso

            float torsoYawDeg = 0f;
            if (Math.abs(relativeHeadYaw) > followThreshold) {
                float extra = Math.abs(relativeHeadYaw) - followThreshold;
                torsoYawDeg = Math.signum(relativeHeadYaw) * clamp(extra * followStrength, 0f, maxTorsoTwist);
            }
            // use same sign convention as head (invert) so the applied relative torso twist matches head direction
            bodyBone.setRotY((float) Math.toRadians(-torsoYawDeg));
        }

        // Optionally apply a smaller twist to the upper body for smoother distribution
        if (upperBodyBone != null) {
            float upperFollowStrength = 0.35f;
            float upperThreshold = 15f;
            float upperMax = 25f;

            float upperYawDeg = 0f;
            if (Math.abs(relativeHeadYaw) > upperThreshold) {
                float extra = Math.abs(relativeHeadYaw) - upperThreshold;
                upperYawDeg = Math.signum(relativeHeadYaw) * clamp(extra * upperFollowStrength, 0f, upperMax);
            }
            upperBodyBone.setRotY((float) Math.toRadians(-upperYawDeg));
        }
    }

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }

    private static float wrapDegrees(float deg) {
        float res = ((deg + 180.0F) % 360.0F + 360.0F) % 360.0F - 180.0F;
        return res;
    }
}
