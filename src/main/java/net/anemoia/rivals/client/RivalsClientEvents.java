package net.anemoia.rivals.client;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.client.render.HeroGeoModel;
import net.anemoia.rivals.client.render.HeroGeoRenderer;
import net.anemoia.rivals.client.render.PlayerGeoAdapter;
import net.anemoia.rivals.client.render.RenderHooks;
import net.anemoia.rivals.core.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RivalsClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.PLAYER_PROXY.get(), (context) -> {
            RenderHooks.setRenderContext(context);
            return new HeroGeoRenderer<>(context, null); // Model is set dynamically
        });
    }
}
