package net.anemoia.rivals.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.anemoia.rivals.client.render.ClientHeroState;
import net.anemoia.rivals.client.render.PlayerGeoAnimatableCache;
import net.anemoia.rivals.client.render.RenderHooks;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerRendererMixin.class);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(AbstractClientPlayer player, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo) {
        var heroId = ClientHeroState.getHeroFor(player.getUUID());
        if (heroId == null) {
            LOGGER.debug("No heroId for player {}, falling back to vanilla renderer", player.getUUID());
            return;
        }

        var animatable = PlayerGeoAnimatableCache.getInstance().getOrCreate(player, heroId);

        boolean handled = RenderHooks.renderPlayerGeo(player, animatable, heroId, poseStack, bufferSource, packedLight, partialTicks);
        if (handled) {
            callbackInfo.cancel();
        } else {
            LOGGER.debug("Hero renderer did not handle rendering for player {}, falling back to vanilla", player.getName().getString());
        }
    }
}
