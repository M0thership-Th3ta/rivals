package net.anemoia.rivals.common.network;

import net.anemoia.rivals.client.data.ClientChargeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ChargeSyncPacket {
    private final Map<String, ChargeData> chargeDataMap;

    public ChargeSyncPacket(Map<String, ChargeData> chargeDataMap) {
        this.chargeDataMap = chargeDataMap;
    }

    public ChargeSyncPacket(FriendlyByteBuf buf) {
        this.chargeDataMap = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            String abilityName = buf.readUtf();
            int charges = buf.readInt();
            int maxCharges = buf.readInt();
            long nextRechargeTime = buf.readLong();
            int cooldownTicks = buf.readInt();
            chargeDataMap.put(abilityName, new ChargeData(charges, maxCharges, nextRechargeTime, cooldownTicks));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(chargeDataMap.size());
        for (Map.Entry<String, ChargeData> entry : chargeDataMap.entrySet()) {
            buf.writeUtf(entry.getKey());
            ChargeData data = entry.getValue();
            buf.writeInt(data.charges);
            buf.writeInt(data.maxCharges);
            buf.writeLong(data.nextRechargeTime);
            buf.writeInt(data.cooldownTicks);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                // Store charge data on client side
                ClientChargeData.updateChargeData(chargeDataMap);
            }
        });
        return true;
    }

    public static class ChargeData {
        public final int charges;
        public final int maxCharges;
        public final long nextRechargeTime;
        public final int cooldownTicks;

        public ChargeData(int charges, int maxCharges, long nextRechargeTime, int cooldownTicks) {
            this.charges = charges;
            this.maxCharges = maxCharges;
            this.nextRechargeTime = nextRechargeTime;
            this.cooldownTicks = cooldownTicks;
        }
    }
}

