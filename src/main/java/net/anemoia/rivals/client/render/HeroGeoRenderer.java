package net.anemoia.rivals.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HeroGeoRenderer<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeroGeoRenderer.class);

    public HeroGeoRenderer(@NotNull EntityRendererProvider.Context renderManager, @NotNull HeroGeoModel<T> model) {
        super(renderManager, model);
        this.shadowRadius = 0.5F;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T animatable) {
        return this.model.getTextureResource(animatable);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        if (entity instanceof PlayerGeoAdapter adapter) {
            float bodyYawInterp = adapter.yBodyRotO + (adapter.yBodyRot - adapter.yBodyRotO) * partialTick;
            poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYawInterp));
        }

        super.render(entity, 0f, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
