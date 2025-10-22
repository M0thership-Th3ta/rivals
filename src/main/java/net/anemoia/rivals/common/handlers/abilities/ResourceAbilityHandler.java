package net.anemoia.rivals.common.handlers.abilities;

import net.anemoia.rivals.common.network.ChargeSyncPacket;
import net.anemoia.rivals.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ResourceAbilityHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceAbilityHandler.class);
    private static final String NBT_KEY = "rivals_resource_ability";

    public static void initializeCharges(Player player, String abilityName, int maxCharges) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag resourceData = playerData.getCompound(NBT_KEY);

        CompoundTag abilityData = new CompoundTag();
        abilityData.putInt("charges", maxCharges);
        abilityData.putInt("maxCharges", maxCharges);
        abilityData.putLong("nextRechargeTime", 0);
        abilityData.putInt("cooldownTicks", 0);

        resourceData.put(abilityName, abilityData);
        playerData.put(NBT_KEY, resourceData);

        LOGGER.info("Initialized charges for {} with {}/{} charges", abilityName, maxCharges, maxCharges);
    }

    public static int getCharges(Player player, String abilityName) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag resourceData = playerData.getCompound(NBT_KEY);

        if (resourceData.contains(abilityName)) {
            return resourceData.getCompound(abilityName).getInt("charges");
        }
        return 0;
    }

    public static int getMaxCharges(Player player, String abilityName) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag resourceData = playerData.getCompound(NBT_KEY);

        if (resourceData.contains(abilityName)) {
            return resourceData.getCompound(abilityName).getInt("maxCharges");
        }
        return 0;
    }

    public static boolean hasCharges(Player player, String abilityName) {
        return getCharges(player, abilityName) > 0;
    }

    public static boolean consumeCharge(Player player, String abilityName, int cooldownTicks) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag resourceData = playerData.getCompound(NBT_KEY);

        if (!resourceData.contains(abilityName)) {
            return false;
        }

        CompoundTag abilityData = resourceData.getCompound(abilityName);
        int currentCharges = abilityData.getInt("charges");
        int maxCharges = abilityData.getInt("maxCharges");

        if (currentCharges > 0) {
            abilityData.putInt("charges", currentCharges - 1);
            abilityData.putInt("cooldownTicks", cooldownTicks);

            long currentTime = System.currentTimeMillis();
            // Always set nextRechargeTime when a charge is consumed
            long rechargeTime = currentTime + (cooldownTicks * 50);
            abilityData.putLong("nextRechargeTime", rechargeTime);

            resourceData.put(abilityName, abilityData);
            playerData.put(NBT_KEY, resourceData);
            syncChargeData(player);

            LOGGER.debug("Consumed charge for {} - remaining: {}/{}", abilityName, currentCharges - 1, maxCharges);
            return true;
        }
        return false;
    }

    public static void tickRecharge(Player player) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag resourceData = playerData.getCompound(NBT_KEY);

        long currentTime = System.currentTimeMillis();
        boolean dataChanged = false;

        for (String abilityName : resourceData.getAllKeys()) {
            CompoundTag abilityData = resourceData.getCompound(abilityName);
            long nextRechargeTime = abilityData.getLong("nextRechargeTime");
            int charges = abilityData.getInt("charges");
            int maxCharges = abilityData.getInt("maxCharges");
            int cooldownTicks = abilityData.getInt("cooldownTicks");

            if (charges < maxCharges && nextRechargeTime > 0 && currentTime >= nextRechargeTime) {
                charges++;
                abilityData.putInt("charges", charges);

                if (charges < maxCharges) {
                    long nextRecharge = currentTime + (cooldownTicks * 50);
                    abilityData.putLong("nextRechargeTime", nextRecharge);
                } else {
                    abilityData.putLong("nextRechargeTime", 0);
                }

                resourceData.put(abilityName, abilityData);
                dataChanged = true;

                LOGGER.debug("Recharged {} - charges: {}/{}", abilityName, charges, maxCharges);
            }
        }

        if (dataChanged) {
            playerData.put(NBT_KEY, resourceData);
            syncChargeData(player);
        }
    }

    public static void syncChargeData(Player player) {
        if (player.level().isClientSide()) return;

        CompoundTag playerData = player.getPersistentData();
        CompoundTag resourceData = playerData.getCompound(NBT_KEY);

        Map<String, ChargeSyncPacket.ChargeData> chargeDataMap = new HashMap<>();
        for (String abilityName : resourceData.getAllKeys()) {
            CompoundTag abilityData = resourceData.getCompound(abilityName);
            int charges = abilityData.getInt("charges");
            int maxCharges = abilityData.getInt("maxCharges");
            long nextRechargeTime = abilityData.getLong("nextRechargeTime");
            int cooldownTicks = abilityData.getInt("cooldownTicks");

            chargeDataMap.put(abilityName, new ChargeSyncPacket.ChargeData(charges, maxCharges, nextRechargeTime, cooldownTicks));
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new ChargeSyncPacket(chargeDataMap), serverPlayer);
        }
    }

    public static void sendNoChargesMessage(Player player, String abilityName) {
        int currentCharges = getCharges(player, abilityName);
        int maxCharges = getMaxCharges(player, abilityName);

        Component message = Component.literal(String.format("%s has no charges remaining! (%d/%d)", abilityName, currentCharges, maxCharges));
        player.displayClientMessage(message, true);
    }
}
