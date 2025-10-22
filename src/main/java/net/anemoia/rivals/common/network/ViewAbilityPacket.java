package net.anemoia.rivals.common.network;

import net.anemoia.rivals.client.CameraLockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ViewAbilityPacket {
    private final String viewType;
    private final boolean viewLocked;

    public ViewAbilityPacket(String viewType, boolean viewLocked) {
        this.viewType = viewType;
        this.viewLocked = viewLocked;
    }

    public ViewAbilityPacket(FriendlyByteBuf buf) {
        this.viewType = buf.readUtf();
        this.viewLocked = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(viewType);
        buf.writeBoolean(viewLocked);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Change camera view
            switch (viewType) {
                case "third_person_back":
                    mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                    break;
                case "first_person":
                    mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                    break;
                // Add other view types as needed
            }

            // Lock camera if requested (simple implementation: disable F5 key)
            CameraLockManager.setCameraLocked(viewLocked);
        });
        return true;
    }
}
