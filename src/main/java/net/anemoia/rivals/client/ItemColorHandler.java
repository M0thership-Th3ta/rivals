package net.anemoia.rivals.client;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.common.item.HeroSoul;
import net.anemoia.rivals.core.registry.ModItems;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ItemColorHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new ItemColor() {
            @Override
            public int getColor(ItemStack stack, int tintIndex) {
                if (tintIndex == 0) {
                    return HeroSoul.getHeroColor(stack);
                }
                return 0xFFFFFF;
            }
        }, ModItems.HERO_SOUL.get());
    }
}
