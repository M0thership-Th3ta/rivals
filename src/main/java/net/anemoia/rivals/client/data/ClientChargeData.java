package net.anemoia.rivals.client.data;

import net.anemoia.rivals.common.network.ChargeSyncPacket;

import java.util.HashMap;
import java.util.Map;

public class ClientChargeData {
    private static final Map<String, ChargeSyncPacket.ChargeData> chargeData = new HashMap<>();

    public static void updateChargeData(Map<String, ChargeSyncPacket.ChargeData> newData) {
        chargeData.clear();
        chargeData.putAll(newData);
    }

    public static int getCharges(String abilityName) {
        ChargeSyncPacket.ChargeData data = chargeData.get(abilityName);
        return data != null ? data.charges : 0;
    }

    public static int getMaxCharges(String abilityName) {
        ChargeSyncPacket.ChargeData data = chargeData.get(abilityName);
        return data != null ? data.maxCharges : 0;
    }

    public static long getNextRechargeTime(String abilityName) {
        ChargeSyncPacket.ChargeData data = chargeData.get(abilityName);
        return data != null ? data.nextRechargeTime : 0;
    }

    public static int getCooldownTicks(String abilityName) {
        ChargeSyncPacket.ChargeData data = chargeData.get(abilityName);
        return data != null ? data.cooldownTicks : 0;
    }

    public static void clear() {
        chargeData.clear();
    }
}

