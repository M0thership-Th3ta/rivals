package net.anemoia.rivals.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.anemoia.rivals.Rivals;
import net.anemoia.rivals.client.data.ClientChargeData;
import net.anemoia.rivals.common.data.Hero;
import net.anemoia.rivals.common.data.HeroDataManager;
import net.anemoia.rivals.common.handlers.AbilityReader;
import net.anemoia.rivals.common.handlers.abilities.AbilityCooldown;
import net.anemoia.rivals.common.handlers.abilities.ResourceAbilityHandler;
import net.anemoia.rivals.common.util.PlayerHeroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Rivals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AbilityHudRenderer {
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("minecraft", "textures/gui/widgets.png");
    private static final int SLOT_SIZE = 22;
    private static final int SPACING = 24;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || minecraft.options.hideGui) {
            return;
        }

        ResourceLocation currentHeroId = PlayerHeroUtil.getCurrentHero(player);
        if (currentHeroId == null) {
            return;
        }

        Hero hero = HeroDataManager.getInstance().getHero(currentHeroId);
        if (hero == null || hero.getAbilities() == null || hero.getAbilities().isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Position abilities to the right of the hotbar
        int hotbarCenterX = screenWidth / 2;
        int hotbarY = screenHeight - 22;
        int startX = hotbarCenterX + 91 + 6;

        int abilityCount = 0;
        for (Hero.Ability ability : hero.getAbilities()) {
            // Only render if the ability has an icon defined
            if (hasValidIcon(ability)) {
                int slotX = startX + (abilityCount * SPACING);
                int slotY = hotbarY;

                renderAbilitySlot(guiGraphics, ability, slotX, slotY);
                renderAbilityIcon(guiGraphics, ability, slotX + 3, slotY + 3);
                renderCooldownOverlay(guiGraphics, player, ability, slotX, slotY);
                renderChargeIndicator(guiGraphics, player, ability, slotX, slotY);

                abilityCount++;
            }
        }
    }

    private static boolean hasValidIcon(Hero.Ability ability) {
        return ability.getAbilityIcon() != null &&
                !ability.getAbilityIcon().isEmpty() &&
                !ability.getAbilityIcon().isBlank();
    }

    private static void renderAbilitySlot(GuiGraphics guiGraphics, Hero.Ability ability, int x, int y) {
        ResourceLocation hudTexture = getHudTexture(ability);
        RenderSystem.setShaderTexture(0, hudTexture);
        RenderSystem.enableBlend();
        guiGraphics.blit(hudTexture, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, 256, 256);
        RenderSystem.disableBlend();
    }

    private static ResourceLocation getHudTexture(Hero.Ability ability) {
        if (ability.getAbilityHud() != null && !ability.getAbilityHud().isEmpty()) {
            try {
                return new ResourceLocation(ability.getAbilityHud());
            } catch (Exception e) {
                // Fall through to return default texture
            }
        }
        return WIDGETS_TEXTURE;
    }

    private static void renderAbilityIcon(GuiGraphics guiGraphics, Hero.Ability ability, int x, int y) {
        String iconPath = ability.getAbilityIcon();
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                if (iconPath.contains("textures/")) {
                    ResourceLocation iconTexture = new ResourceLocation(iconPath);
                    RenderSystem.setShaderTexture(0, iconTexture);
                    guiGraphics.blit(iconTexture, x, y, 0, 0, 16, 16, 16, 16);
                } else {
                    ItemStack iconStack = getAbilityIcon(ability);
                    if (!iconStack.isEmpty()) {
                        guiGraphics.renderItem(iconStack, x, y);
                    }
                }
            } catch (Exception e) {
                // Invalid icon path, do not render icon
            }
        }
    }

    private static void renderCooldownOverlay(GuiGraphics guiGraphics, LocalPlayer player, Hero.Ability ability, int slotX, int slotY) {
        String abilityName = ability.getAbilityName();
        if (ResourceAbilityHandler.getMaxCharges(player, abilityName) > 0) {
            renderChargeRechargeOverlay(guiGraphics, player, abilityName, slotX, slotY);
            return;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownEndTime = AbilityCooldown.getCooldownEndTime(player, ability.getAbilityName());

        if (cooldownEndTime > currentTime) {
            // Calculate cooldown progress (1.0 = just started cooldown, 0.0 = cooldown finished)
            long totalCooldownTime = ability.getAbilityCooldown() * 50; // Convert ticks to milliseconds
            long remainingTime = cooldownEndTime - currentTime;
            float cooldownProgress = (float) remainingTime / totalCooldownTime;
            cooldownProgress = Mth.clamp(cooldownProgress, 0.0f, 1.0f);

            // INVERTED: Calculate overlay that shrinks from top to bottom
            float progressFromTop = 1.0f - cooldownProgress;
            int yOffset = Math.round(progressFromTop * (SLOT_SIZE - 2));
            int overlayHeight = (SLOT_SIZE - 2) - yOffset;

            if (overlayHeight > 0) {
                // Push matrix to ensure proper z-ordering
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200); // Move overlay forward in z-space

                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                // Render overlay that starts from top and shrinks downward
                guiGraphics.fill(slotX + 1, slotY + 1 + yOffset, slotX + SLOT_SIZE - 1, slotY + SLOT_SIZE - 1, 0x80FFFFFF);

                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();

                guiGraphics.pose().popPose();
            }
        }
    }

    private static void renderChargeRechargeOverlay(GuiGraphics guiGraphics, LocalPlayer player, String abilityName, int slotX, int slotY) {
        // Get charge data from ResourceAbilityHandler
        int currentCharges = ResourceAbilityHandler.getCharges(player, abilityName);
        int maxCharges = ResourceAbilityHandler.getMaxCharges(player, abilityName);

        // Only show overlay if not at full charges
        if (currentCharges >= maxCharges) {
            return;
        }

        // Get the next recharge time from NBT data
        long nextRechargeTime = getNextRechargeTime(player, abilityName);
        int cooldownTicks = getCooldownTicks(player, abilityName);

        if (nextRechargeTime > 0 && cooldownTicks > 0) {
            long currentTime = System.currentTimeMillis();
            long totalRechargeTime = cooldownTicks * 50L; // Convert ticks to milliseconds
            long timeElapsed = Math.max(0, totalRechargeTime - (nextRechargeTime - currentTime));

            float rechargeProgress = Math.min(1.0f, (float) timeElapsed / totalRechargeTime);

            // Calculate overlay that shrinks from top to bottom as charge recharges
            float progressFromTop = 1.0f - rechargeProgress;
            int yOffset = Math.round(progressFromTop * (SLOT_SIZE - 2));
            int overlayHeight = (SLOT_SIZE - 2) - yOffset;

            if (overlayHeight > 0) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);

                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                // Use a different color for charge recharge (blue-ish instead of white)
                guiGraphics.fill(slotX + 1, slotY + 1 + yOffset, slotX + SLOT_SIZE - 1, slotY + SLOT_SIZE - 1, 0x80FFFFFF);

                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();

                guiGraphics.pose().popPose();
            }
        }
    }

    private static long getNextRechargeTime(LocalPlayer player, String abilityName) {
        return ClientChargeData.getNextRechargeTime(abilityName);
    }

    private static int getCooldownTicks(LocalPlayer player, String abilityName) {
        return ClientChargeData.getCooldownTicks(abilityName);
    }

    private static void renderChargeIndicator(GuiGraphics guiGraphics, LocalPlayer player, Hero.Ability ability, int slotX, int slotY) {
        String abilityName = ability.getAbilityName();

        // Check if this ability is charge-based by looking at its definition
        boolean isChargeBased = isChargeBasedAbility(ability);

        if (isChargeBased) {
            int maxCharges = ClientChargeData.getMaxCharges(abilityName);
            int currentCharges = ClientChargeData.getCharges(abilityName);

            // If charges haven't been initialized yet, don't show indicator
            if (maxCharges == 0) {
                return;
            }

            // Just show current charges, not max
            String chargeText = String.valueOf(currentCharges);

            // Position charge text in bottom right corner of slot
            int textX = slotX + SLOT_SIZE - Minecraft.getInstance().font.width(chargeText) - 2;
            int textY = slotY + SLOT_SIZE - 10;

            // Add a small dark background circle for better readability
            int textWidth = Minecraft.getInstance().font.width(chargeText);
            int bgSize = Math.max(textWidth + 2, 8);
            int bgX = textX + (textWidth / 2) - (bgSize / 2);
            int bgY = textY - 1;

            guiGraphics.fill(bgX, bgY, bgX + bgSize, bgY + bgSize, 0xC0000000);

            // Use different colors based on charge availability
            int textColor = currentCharges > 0 ? 0xFFFFFF : 0xFF5555;
            guiGraphics.drawString(Minecraft.getInstance().font, chargeText, textX, textY, textColor, true);
        }
    }


    private static boolean isChargeBasedAbility(Hero.Ability ability) {
        // Check if this is a custom ability
        if ("rivals:custom_ability".equals(ability.getAbility().getAbilityType())) {
            String abilityPath = ability.getAbility().getAbilityPath();
            if (abilityPath != null) {
                // Load the custom ability data to check if it contains resource charges
                Object customAbilityData = AbilityReader.loadCustomAbility(abilityPath);
                if (customAbilityData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> abilityMap = (Map<String, Object>) customAbilityData;

                    // Check if this is a chainable ability
                    if ("chainable".equals(abilityMap.get("custom_ability"))) {
                        Object abilityChainObj = abilityMap.get("ability_chain");
                        if (abilityChainObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Object> abilityChain = (List<Object>) abilityChainObj;

                            // Check each element in the chain for resource abilities
                            for (Object chainElement : abilityChain) {
                                if (chainElement instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> elementMap = (Map<String, Object>) chainElement;

                                    if (elementMap.containsKey("ability")) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> abilityData = (Map<String, Object>) elementMap.get("ability");

                                        if ("rivals:resource".equals(abilityData.get("ability_type"))) {
                                            @SuppressWarnings("unchecked")
                                            Map<String, Object> attributes = (Map<String, Object>) abilityData.get("ability_attributes");
                                            if (attributes != null && "charge".equals(attributes.get("resource_type"))) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check if this is a direct resource ability
        if ("rivals:resource".equals(ability.getAbility().getAbilityType())) {
            Map<String, Object> attributes = ability.getAbility().getAbilityAttributes();
            if (attributes != null && "charge".equals(attributes.get("resource_type"))) {
                return true;
            }
        }

        return false;
    }

    private static ItemStack getAbilityIcon(Hero.Ability ability) {
        try {
            ResourceLocation iconLocation = new ResourceLocation(ability.getAbilityIcon());
            Item iconItem = ForgeRegistries.ITEMS.getValue(iconLocation);
            if (iconItem != null && iconItem != Items.AIR) {
                return new ItemStack(iconItem);
            }
        } catch (Exception e) {
            // Return empty stack instead of fallback for invalid icons
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }
}

