package net.anemoia.rivals.client.render;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.anemoia.rivals.Rivals;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HeroPlayerRenderHandler {
    private static final ResourceLocation ANIMATION_KEY = new ResourceLocation("rivals", "animation");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ANIMATION_KEY, 42, HeroPlayerRenderHandler::createPlayerAnimation);
    }

    private static IAnimation createPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static ModifierLayer<IAnimation> getPlayerAnimationLayer(AbstractClientPlayer player) {
        var data = PlayerAnimationAccess.getPlayerAssociatedData(player);
        var animation = data.get(ANIMATION_KEY);
        if (animation instanceof ModifierLayer) {
            return (ModifierLayer<IAnimation>) animation;
        }
        return null;
    }

    public static void playAnimation(AbstractClientPlayer player, ResourceLocation animationId) {
        var layer = getPlayerAnimationLayer(player);
        if (layer == null) return;

        var animation = PlayerAnimationRegistry.getAnimation(animationId);
        if (animation == null) return;

        layer.setAnimation(new KeyframeAnimationPlayer(animation));
    }

    public static void replaceAnimationWithFade(AbstractClientPlayer player, ResourceLocation animationId, int fadeTicks) {
        var layer = getPlayerAnimationLayer(player);
        if (layer == null) return;

        var animation = PlayerAnimationRegistry.getAnimation(animationId);
        if (animation == null) return;

        try {
            var method = ModifierLayer.class.getMethod("replaceAnimationWithFade", KeyframeAnimationPlayer.class, int.class);
            method.invoke(layer, new KeyframeAnimationPlayer(animation), fadeTicks);
        } catch (ReflectiveOperationException ignored) {
            layer.setAnimation(new KeyframeAnimationPlayer(animation));
        }
    }

    private HeroPlayerRenderHandler() {
        /* utility class */
    }
}
