package net.anemoia.rivals.core.registry;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.common.util.HeroSoulGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Rivals.MOD_ID);

    public static final RegistryObject<CreativeModeTab> HERO_SOULS_TAB = CREATIVE_MODE_TABS.register("hero_souls",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.HERO_SOUL.get()))
                    .title(Component.translatable("creativetab.rivals.hero_souls"))
                    .displayItems((parameters, output) -> {
                        HeroSoulGenerator.generateAllHeroSouls().forEach(output::accept);
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
