package net.anemoia.rivals.common.item;

import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.handlers.HeroHandler;
import net.anemoia.rivals.core.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public class HeroSoul extends Item implements ICurioItem {

    public HeroSoul(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        ResourceLocation heroId = getHeroId(stack);
        if (heroId != null) {
            Hero hero = HeroDataManager.getInstance().getHero(heroId);
            if (hero != null) {
                tooltipComponents.add(Component.literal(hero.getName()).withStyle(ChatFormatting.GOLD));
            }
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag unused) {
        return CuriosApi.createCurioProvider(new ICurio() {

            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public boolean canEquip(SlotContext slotContext) {
                return "soul".equals(slotContext.identifier());
            }

            @Override
            public void onEquip(SlotContext slotContext, ItemStack prevStack) {
                LivingEntity entity = slotContext.entity();
                if (entity instanceof ServerPlayer player) {
                    ResourceLocation heroId = getHeroId(stack);
                    if (heroId != null) {
                        HeroHandler.removeHero(player);
                        HeroHandler.applyHero(player, heroId);
                        Hero hero = HeroDataManager.getInstance().getHero(heroId);
                        if (hero != null) {
                            player.sendSystemMessage(Component.literal("You have become " + hero.getName() + "!")
                                    .withStyle(ChatFormatting.GOLD));
                        }
                    }
                }
            }

            @Override
            public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                LivingEntity entity = slotContext.entity();
                if (entity instanceof ServerPlayer player) {
                    ResourceLocation heroId = getHeroId(stack);
                    if (heroId != null) {
                        HeroHandler.removeHero(player);

                        Hero hero = HeroDataManager.getInstance().getHero(heroId);
                        if (hero != null) {
                            player.sendSystemMessage(Component.literal("You are no longer " + hero.getName())
                                    .withStyle(ChatFormatting.GRAY));
                        }
                    }
                }
            }

            @Override
            public void curioTick(SlotContext slotContext) {

            }
        });
    }

    public static ItemStack createHeroSoul(ResourceLocation heroId) {
        ItemStack stack = new ItemStack(ModItems.HERO_SOUL.get());
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putString("hero_id", heroId.toString());
        return stack;
    }

    public static ResourceLocation getHeroId(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("hero_id")) {
            return new ResourceLocation(nbt.getString("hero_id"));
        }
        return null;
    }

    public static int getHeroColor(ItemStack stack) {
        ResourceLocation heroId = getHeroId(stack);
        if (heroId != null) {
            Hero hero = HeroDataManager.getInstance().getHero(heroId);
            if (hero != null) {
                return hero.getColor();
            }
        }
        return 0xFFFFFF;
    }
}
