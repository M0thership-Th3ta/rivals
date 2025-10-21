package net.anemoia.rivals.core.registry;

import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.common.item.HeroSoul;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Rivals.MOD_ID);

    public static final RegistryObject<Item> HERO_SOUL = ITEMS.register("hero_soul",
            () -> new HeroSoul(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
