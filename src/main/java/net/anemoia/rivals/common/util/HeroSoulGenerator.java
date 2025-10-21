package net.anemoia.rivals.common.util;

import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.item.HeroSoul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HeroSoulGenerator {

    public static List<ItemStack> generateAllHeroSouls() {
        List<ItemStack> heroSouls = new ArrayList<>();

        HeroDataManager.getInstance().getAllHeroes().entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().getName().compareToIgnoreCase(e2.getValue().getName()))
                .forEach(entry -> {
                    ItemStack heroSoul = HeroSoul.createHeroSoul(entry.getKey());
                    heroSouls.add(heroSoul);
                });

        return heroSouls;
    }

    public static ItemStack generateHeroSoul(ResourceLocation heroId) {
        return HeroSoul.createHeroSoul(heroId);
    }
}
